package com.ninchat.sdk.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.views.NinchatWebRTCView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends NinchatBaseActivity {

    static int REQUEST_CODE = NinchatChatActivity.class.hashCode() & 0xffff;

    protected static final int CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE = "WebRTCVideoAudio".hashCode() & 0xffff;
    protected static final int STORAGE_PERMISSION_REQUEST_CODE = "ExternalStorage".hashCode() & 0xffff;
    protected static final int PICK_PHOTO_VIDEO_REQUEST_CODE = "PickPhotoVideo".hashCode() & 0xffff;

    private NinchatMessageAdapter messageAdapter = new NinchatMessageAdapter();

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_chat;
    }

    protected void quit(Intent data) {
        if (data == null) {
            data = new Intent();
        }
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatReviewActivity.REQUEST_CODE) {
            quit(data);
        } else if (requestCode == NinchatChatActivity.PICK_PHOTO_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                final Uri uri = data.getData();
                String fileName = getFileName(uri);
                final InputStream inputStream = getContentResolver().openInputStream(uri);
                final int size = inputStream.available();
                final byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                NinchatSessionManager.getInstance().sendImage(fileName, buffer);
            } catch (final Exception e) {
                // TODO: show error?
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getFileName(Uri uri) {
        final Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null);
        String displayName = "";
        try {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return displayName;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE) {
            if (hasVideoCallPermissions()) {
                sendPickUpAnswer(true);
            } else {
                // Display error
                sendPickUpAnswer(false);
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (hasFileAccessPermissions()) {
                openImagePicker(null);
                //findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.VISIBLE);
            } else {
                // Display error
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected BroadcastReceiver channelClosedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.CHANNEL_CLOSED.equals(action)) {
                messageAdapter.close(NinchatChatActivity.this);
            }
        }
    };

    public void onCloseChat(final View view) {
        findViewById(R.id.ninchat_chat_close).setVisibility(View.GONE);
        findViewById(R.id.ninchat_chat_close_chat_dialog).setVisibility(View.VISIBLE);
    }

    public void onCloseChatConfirm(final View view) {
        chatClosed();
    }

    public void onContinueChat(final View view) {
        findViewById(R.id.ninchat_chat_close).setVisibility(View.VISIBLE);
        findViewById(R.id.ninchat_chat_close_chat_dialog).setVisibility(View.GONE);
    }

    public void chatClosed() {
        if (NinchatSessionManager.getInstance().showRating()) {
            startActivityForResult(NinchatReviewActivity.getLaunchIntent(NinchatChatActivity.this), NinchatReviewActivity.REQUEST_CODE);
        } else {
            quit(null);
        }
    }

    protected BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.NEW_MESSAGE.equals(action)) {
                final String message = intent.getStringExtra(NinchatSessionManager.Broadcast.MESSAGE_CONTENT);
                final String fileId = intent.getStringExtra(NinchatSessionManager.Broadcast.MESSAGE_FILE_ID);
                final String sender = intent.getStringExtra(NinchatSessionManager.Broadcast.MESSAGE_SENDER);
                final long timestamp = intent.getLongExtra(NinchatSessionManager.Broadcast.MESSAGE_TIMESTAMP, 0);
                final boolean isRemoteMessage = intent.getBooleanExtra(NinchatSessionManager.Broadcast.MESSAGE_IS_REMOTE, true);
                final boolean isWriting = intent.getBooleanExtra(NinchatSessionManager.Broadcast.MESSAGE_IS_WRITING, false);
                messageAdapter.add(message, fileId, sender, timestamp, isRemoteMessage, isWriting);
            }
        }
    };

    private boolean hasVideoCallPermissions() {
        return checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasFileAccessPermissions() {
        return checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendPickUpAnswer(final boolean answer) {
        NinchatSessionManager.getInstance().sendWebRTCCallAnswer(answer);
    }

    private View videoContainer;
    protected NinchatWebRTCView webRTCView;

    protected BroadcastReceiver webRTCMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.WEBRTC_MESSAGE.equals(action)) {
                final String messageType = intent.getStringExtra(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE_TYPE);
                if (NinchatSessionManager.MessageTypes.CALL.equals(messageType)) {
                    findViewById(R.id.ninchat_chat_close).setVisibility(View.GONE);
                    findViewById(R.id.ninchat_chat_video_call_consent_dialog).setVisibility(View.VISIBLE);
                    final ImageView userImage = findViewById(R.id.ninchat_video_call_consent_dialog_user_avatar);
                    // TODO: Set the user Image
                    final TextView userName = findViewById(R.id.ninchat_video_call_consent_dialog_user_name);
                    userName.setText(intent.getStringExtra(NinchatSessionManager.Broadcast.MESSAGE_SENDER));
                } else if (webRTCView.handleWebRTCMessage(messageType, intent.getStringExtra(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE_CONTENT))) {
                    if (NinchatSessionManager.MessageTypes.HANG_UP.equals(messageType)) {
                        findViewById(R.id.ninchat_chat_close).setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    };

    public void onAcceptVideoCall(final View view) {
        findViewById(R.id.ninchat_chat_video_call_consent_dialog).setVisibility(View.GONE);
        if (hasVideoCallPermissions()) {
            sendPickUpAnswer(true);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            sendPickUpAnswer(false);
        }
    }

    public void onRejectVideoCall(final View view) {
        findViewById(R.id.ninchat_chat_close).setVisibility(View.VISIBLE);
        findViewById(R.id.ninchat_chat_video_call_consent_dialog).setVisibility(View.GONE);
        sendPickUpAnswer(false);
    }

    public void onVideoHangUp(final View view) {
        webRTCView.hangUp();
        findViewById(R.id.ninchat_chat_close).setVisibility(View.VISIBLE);
    }

    public void onToggleMicrophone(final View view) {
        webRTCView.toggleMicrophone();
    }

    public void onToggleVideo(final View view) {
        webRTCView.toggleVideo();
    }


    public void onAttachmentClick(final View view) {
        if (hasFileAccessPermissions()) {
            openImagePicker(view);
            /*final Intent photos = new Intent(Intent.ACTION_PICK).setType("image/*");
            final Intent videos = new Intent(Intent.ACTION_PICK).setType("video/*");
            final Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final List<Intent> openIntents = new ArrayList<>();
            addIntentToList(openIntents, photos);
            addIntentToList(openIntents, camera);
            if (openIntents.size() > 0) {
                final Intent chooserIntent = Intent.createChooser(openIntents.remove(openIntents.size() -1), "foo");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, openIntents.toArray(new Parcelable[]{}));
                startActivityForResult(chooserIntent, PICK_PHOTO_VIDEO_REQUEST_CODE);
            }*/
            //findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    private void addIntentToList(final List<Intent> intents, final Intent intent) {
        for (final ResolveInfo resInfo : getPackageManager().queryIntentActivities(intent, 0)) {
            String packageName = resInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            intents.add(targetedIntent);
        }
    }

    public void openImagePicker(final View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_PHOTO_VIDEO_REQUEST_CODE);
    }

    public void onSendClick(final View view) {
        final TextView messageView = findViewById(R.id.message);
        final String message = messageView.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        NinchatSessionManager.getInstance().sendMessage(message);
        messageView.setText(null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoContainer = findViewById(R.id.videoContainer);
        webRTCView = new NinchatWebRTCView(videoContainer);
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(channelClosedReceiver, new IntentFilter(NinchatSessionManager.Broadcast.CHANNEL_CLOSED));
        localBroadcastManager.registerReceiver(messageReceiver, new IntentFilter(NinchatSessionManager.Broadcast.NEW_MESSAGE));
        localBroadcastManager.registerReceiver(webRTCMessageReceiver, new IntentFilter(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE));
        final RecyclerView messages = findViewById(R.id.message_list);
        messages.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messages.setAdapter(messageAdapter);
        final EditText message = findViewById(R.id.message);
        message.setHint(NinchatSessionManager.getInstance().getEnterMessage());
        final Button closeButton = findViewById(R.id.ninchat_chat_close);
        closeButton.setText(NinchatSessionManager.getInstance().getCloseChat());
        final String sendButtonText = NinchatSessionManager.getInstance().getSendButtonText();
        final ImageView sendButton = findViewById(R.id.send_button);
        if (sendButtonText != null) {
            findViewById(R.id.send_button_text).setVisibility(View.VISIBLE);
            sendButton.setVisibility(View.GONE);
        }
        if (NinchatSessionManager.getInstance().isAttachmentsEnabled()) {
            findViewById(R.id.attachment).setVisibility(View.VISIBLE);
        }
        if (NinchatSessionManager.getInstance().isVideoEnabled()) {
            // TODO: Show the video icon
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(channelClosedReceiver);
        localBroadcastManager.unregisterReceiver(messageReceiver);
        localBroadcastManager.unregisterReceiver(webRTCMessageReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final ViewGroup.LayoutParams layoutParams = videoContainer.getLayoutParams();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.height = (int) getResources().getDimension(R.dimen.ninchat_chat_activity_video_view_height);
        }
        videoContainer.setLayoutParams(layoutParams);
    }
}
