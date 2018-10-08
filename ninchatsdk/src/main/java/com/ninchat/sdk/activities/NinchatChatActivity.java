package com.ninchat.sdk.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.views.NinchatWebRTCView;

import droidninja.filepicker.FilePickerBuilder;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends NinchatBaseActivity {

    static int REQUEST_CODE = NinchatChatActivity.class.hashCode() & 0xffff;

    protected static final int CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE = "WebRTCVideoAudio".hashCode() & 0xffff;
    protected static final int STORAGE_PERMISSION_REQUEST_CODE = "ExternalStorage".hashCode() & 0xffff;
    protected static final int PICK_PHOTO_VIDEO_REQUEST_CODE = "PickPhotoVideo".hashCode() & 0xffff;
    protected static final int PICK_PDF_REQUEST_CODE = "PickPDF".hashCode() & 0xffff;

    private NinchatMessageAdapter messageAdapter = new NinchatMessageAdapter();
    private boolean lastMessageWasRemote = false;
    private String lastSentMessage = null;

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
        }
        super.onActivityResult(requestCode, resultCode, data);
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
                findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.VISIBLE);
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
        chatClosed();
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
                final String sender = intent.getStringExtra(NinchatSessionManager.Broadcast.MESSAGE_SENDER);
                final long timestamp = intent.getLongExtra(NinchatSessionManager.Broadcast.MESSAGE_TIMESTAMP, 0);
                lastMessageWasRemote = !message.equals(lastSentMessage);
                messageAdapter.add(message, sender, timestamp, lastMessageWasRemote);
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

    protected NinchatWebRTCView webRTCView;

    protected BroadcastReceiver webRTCMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.WEBRTC_MESSAGE.equals(action)) {
                final String messageType = intent.getStringExtra(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE_TYPE);
                if (NinchatSessionManager.MessageTypes.CALL.equals(messageType)) {
                    findViewById(R.id.ninchat_chat_video_call_consent_dialog).setVisibility(View.VISIBLE);
                    final TextView userName = findViewById(R.id.ninchat_video_call_consent_dialog_user_name);
                    userName.setText("foo");
                } else {
                    webRTCView.handleWebRTCMessage(messageType, intent.getStringExtra(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE_CONTENT));
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
        findViewById(R.id.ninchat_chat_video_call_consent_dialog).setVisibility(View.GONE);
        sendPickUpAnswer(false);
    }

    public void onAttachmentClick(final View view) {
        if (hasFileAccessPermissions()) {
            findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    public void openImagePicker(final View view) {
        findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.GONE);
        FilePickerBuilder.getInstance()
                .setMaxCount(1)
                .setActivityTheme(R.style.LibAppTheme)
                .enableVideoPicker(true)
                .enableCameraSupport(true)
                .showGifs(false)
                .showFolderView(false)
                .pickPhoto(this, PICK_PHOTO_VIDEO_REQUEST_CODE);
    }

    public void openPDFPicker(final View view) {
        findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.GONE);
        FilePickerBuilder.getInstance()
                .setMaxCount(1)
                .setActivityTheme(R.style.LibAppTheme)
                .addFileSupport(getString(R.string.ninchat_file_type_pdf), new String[]{".pdf"})
                .pickFile(this, PICK_PDF_REQUEST_CODE);
    }

    public void closeFilePickerDialog(final View view) {
        findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.GONE);
    }

    public void onSendClick(final View view) {
        final TextView messageView = findViewById(R.id.message);
        final String message = messageView.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        NinchatSessionManager.getInstance().sendMessage(message);
        lastSentMessage = message;
        lastMessageWasRemote = false;
        messageView.setText(null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webRTCView = new NinchatWebRTCView(findViewById(R.id.videoContainer));
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
}
