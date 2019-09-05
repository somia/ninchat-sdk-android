package com.ninchat.sdk.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.models.NinchatUser;
import com.ninchat.sdk.views.NinchatWebRTCView;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends NinchatBaseActivity {

    static int REQUEST_CODE = NinchatChatActivity.class.hashCode() & 0xffff;

    protected static final int CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE = "WebRTCVideoAudio".hashCode() & 0xffff;
    protected static final int PICK_PHOTO_VIDEO_REQUEST_CODE = "PickPhotoVideo".hashCode() & 0xffff;

    private NinchatMessageAdapter messageAdapter = sessionManager.getMessageAdapter();

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
                sessionManager.sendImage(fileName, buffer);
            } catch (final Exception e) {
                sessionManager.sessionError(e);
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
                sendPickUpAnswer(false);
                showError(R.id.ninchat_chat_error, R.string.ninchat_chat_error_no_video_call_permissions);

            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (hasFileAccessPermissions()) {
                openImagePicker(null);
                //findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.VISIBLE);
            } else {
                showError(R.id.ninchat_chat_error, R.string.ninchat_chat_error_no_file_permissions);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected boolean chatClosed = false;

    protected BroadcastReceiver channelClosedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.CHANNEL_CLOSED.equals(action)) {
                messageAdapter.close(NinchatChatActivity.this);
                chatClosed = true;
                hideKeyboard();
            }
        }
    };

    protected BroadcastReceiver transferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.AUDIENCE_ENQUEUED.equals(action)) {
                sessionManager.partChannel();
                quit(intent);
            }
        }
    };

    public void onCloseChat(final View view) {
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.NinchatTheme_Dialog)
                .setView(R.layout.dialog_close_chat)
                .setCancelable(true)
                .create();
        dialog.show();
        final TextView title = dialog.findViewById(R.id.ninchat_close_chat_dialog_title);
        title.setText(sessionManager.getCloseChat());
        final TextView description = dialog.findViewById(R.id.ninchat_close_chat_dialog_description);
        description.setText(sessionManager.getCloseChatDescription());
        final Button confirm = dialog.findViewById(R.id.ninchat_close_chat_dialog_confirm);
        confirm.setText(sessionManager.getCloseChat());
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatClosed();
                dialog.dismiss();
            }
        });
        final Button decline = dialog.findViewById(R.id.ninchat_close_chat_dialog_decline);
        decline.setText(sessionManager.getContinueChat());
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        hideKeyboard();
        if (chatClosed) {
            dialog.dismiss();
            chatClosed();
        }
    }

    public void chatClosed() {
        onVideoHangUp(null);
        sessionManager.partChannel();
        if (sessionManager.showRating()) {
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
                messageAdapter.messagesUpdated(intent.getIntExtra(NinchatSessionManager.Broadcast.MESSAGE_INDEX, -1),
                        intent.getBooleanExtra(NinchatSessionManager.Broadcast.MESSAGE_UPDATED, false),
                        intent.getBooleanExtra(NinchatSessionManager.Broadcast.MESSAGE_REMOVED, false));
            }
        }
    };

    private boolean hasVideoCallPermissions() {
        return checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendPickUpAnswer(final boolean answer) {
        sessionManager.sendWebRTCCallAnswer(answer);
        messageAdapter.addMetaMessage(answer ? sessionManager.getVideoCallAccepted() : sessionManager.getVideoCallRejected());
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
                    final AlertDialog dialog = new AlertDialog.Builder(NinchatChatActivity.this, R.style.NinchatTheme_Dialog)
                            .setView(R.layout.dialog_video_call_consent)
                            .setCancelable(true)
                            .create();
                    dialog.show();
                    final TextView title = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_title);
                    title.setText(sessionManager.getVideoChatTitle());
                    final ImageView userImage = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_user_avatar);
                    final NinchatUser user = sessionManager.getMember(intent.getStringExtra(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE_SENDER));
                    String avatar = user.getAvatar();
                    if (TextUtils.isEmpty(avatar)) {
                        avatar = sessionManager.getDefaultAvatar(true);
                    }
                    if (!TextUtils.isEmpty(avatar)) {
                        GlideApp.with(userImage.getContext())
                                .load(avatar)
                                .circleCrop()
                                .into(userImage);
                    }
                    final TextView userName = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_user_name);
                    userName.setText(user.getName());
                    final TextView description = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_description);
                    description.setText(sessionManager.getVideoChatDescription());
                    final Button accept = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_accept);
                    accept.setText(sessionManager.getVideoCallAccept());
                    accept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            if (hasVideoCallPermissions()) {
                                sendPickUpAnswer(true);
                            } else {
                                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE);
                            }
                        }
                    });
                    final Button decline = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_decline);
                    decline.setText(sessionManager.getVideoCallDecline());
                    decline.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendPickUpAnswer(false);
                            dialog.dismiss();
                        }
                    });
                    hideKeyboard();
                    messageAdapter.addMetaMessage(sessionManager.getVideoCallMetaMessage());
                } else if (webRTCView.handleWebRTCMessage(messageType, intent.getStringExtra(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE_CONTENT))) {
                    if (NinchatSessionManager.MessageTypes.HANG_UP.equals(messageType)) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }
                }
            }
        }
    };

    private void hideKeyboard() {
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (final Exception e) {
            // Ignore
        }
    }

    public void onVideoHangUp(final View view) {
        webRTCView.hangUp();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    public void onToggleFullScreen(final View view) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public void onToggleAudio(final View view) {
        webRTCView.toggleAudio();
    }

    public void onToggleMicrophone(final View view) {
        webRTCView.toggleMicrophone();
    }

    public void onToggleVideo(final View view) {
        webRTCView.toggleVideo();
    }

    public void onVideoCall(final View view) {
        if (chatClosed) {
            return;
        }
        webRTCView.call();
    }

    public void onAttachmentClick(final View view) {
        if (chatClosed) {
            return;
        }
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
        } else {
            requestFileAccessPermissions();
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

    public void onEditTextClick(final View view) {
        final EditText editText = findViewById(R.id.message);
        if(editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void onSendClick(final View view) {
        if (chatClosed) {
            return;
        }
        final TextView messageView = findViewById(R.id.message);
        final String message = messageView.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        sessionManager.sendMessage(message);
        writingMessageSent = false;
        messageView.setText(null);
    }

    private boolean writingMessageSent = false;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() != 0 && !writingMessageSent) {
                sessionManager.sendIsWritingUpdate(true);
                writingMessageSent = true;
            } else if (s.length() == 0) {
                sessionManager.sendIsWritingUpdate(false);
                writingMessageSent = false;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.ninchat_chat_background_not_tiled)) {
            findViewById(R.id.ninchat_chat_root).setBackgroundResource(R.drawable.ninchat_chat_background);
        }
        videoContainer = findViewById(R.id.videoContainer);
        webRTCView = new NinchatWebRTCView(videoContainer);
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(channelClosedReceiver, new IntentFilter(NinchatSessionManager.Broadcast.CHANNEL_CLOSED));
        localBroadcastManager.registerReceiver(transferReceiver, new IntentFilter(NinchatSessionManager.Broadcast.AUDIENCE_ENQUEUED));
        localBroadcastManager.registerReceiver(messageReceiver, new IntentFilter(NinchatSessionManager.Broadcast.NEW_MESSAGE));
        localBroadcastManager.registerReceiver(webRTCMessageReceiver, new IntentFilter(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE));
        final RecyclerView messages = findViewById(R.id.message_list);
        messages.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messages.setAdapter(messageAdapter);
        final EditText message = findViewById(R.id.message);
        message.setHint(sessionManager.getEnterMessage());
        message.addTextChangedListener(textWatcher);
        final Button closeButton = findViewById(R.id.ninchat_chat_close);
        closeButton.setText(sessionManager.getCloseChat());
        final String sendButtonText = sessionManager.getSendButtonText();
        final Button sendButton = findViewById(R.id.send_button);
        final RelativeLayout sendIcon = findViewById(R.id.send_button_icon);
        if (sendButtonText != null) {
            sendButton.setText(sendButtonText);
        } else {
            sendButton.setVisibility(View.GONE);
            sendIcon.setVisibility(View.VISIBLE);
        }
        if (sessionManager.isAttachmentsEnabled()) {
            findViewById(R.id.attachment).setVisibility(View.VISIBLE);
        }
        if (sessionManager.isVideoEnabled() && getResources().getBoolean(R.bool.ninchat_allow_user_initiated_video_calls)) {
            findViewById(R.id.video_call).setVisibility(View.VISIBLE);
        }
        sessionManager.loadChannelHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the message list, just in case
        messageAdapter.notifyDataSetChanged();
        webRTCView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webRTCView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webRTCView.hangUp();
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(channelClosedReceiver);
        localBroadcastManager.unregisterReceiver(transferReceiver);
        localBroadcastManager.unregisterReceiver(messageReceiver);
        localBroadcastManager.unregisterReceiver(webRTCMessageReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        webRTCView.onPause();
        final ViewGroup.LayoutParams layoutParams = videoContainer.getLayoutParams();
        final ImageView image = findViewById(R.id.fullscreen_on_off);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            image.setImageResource(R.drawable.ninchat_icon_video_toggle_normal);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.height = (int) getResources().getDimension(R.dimen.ninchat_chat_activity_video_view_height);
            image.setImageResource(R.drawable.ninchat_icon_video_toggle_full);
        }
        videoContainer.setLayoutParams(layoutParams);
        webRTCView.onResume();
    }
}
