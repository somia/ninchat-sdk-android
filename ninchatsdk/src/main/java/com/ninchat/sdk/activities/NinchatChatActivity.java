package com.ninchat.sdk.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.views.NinchatWebRTCView;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends NinchatBaseActivity {

    static int REQUEST_CODE = NinchatChatActivity.class.hashCode() & 0xffff;

    protected static final int CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE = "WebRTCVideoAudio".hashCode() & 0xffff;

    private NinchatMessageAdapter messageAdapter;
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
        NinchatChatActivity.this.setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE) {
            if (hasPermissions()) {
                sendPickUpAnswer(true);
            } else {
                // Display error
                sendPickUpAnswer(false);
            }
        } else if (requestCode == NinchatReviewActivity.REQUEST_CODE) {
            quit(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected BroadcastReceiver channelClosedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.CHANNEL_CLOSED.equals(action)) {
                if (NinchatSessionManager.getInstance().showRating()) {
                    startActivityForResult(NinchatReviewActivity.getLaunchIntent(NinchatChatActivity.this), NinchatReviewActivity.REQUEST_CODE);
                } else {
                    quit(null);
                }
            }
        }
    };

    protected BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.NEW_MESSAGE.equals(action)) {
                final String message = intent.getStringExtra(NinchatSessionManager.Broadcast.MESSAGE_CONTENT);
                lastMessageWasRemote = !message.equals(lastSentMessage);
                messageAdapter.add(message, lastMessageWasRemote);
            }
        }
    };

    private boolean hasPermissions() {
        if (checkCallingOrSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
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
                    /*new AlertDialog.Builder(NinchatChatActivity.this).setTitle("Video call request")
                            .setMessage("Foo")
                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (hasPermissions()) {
                                        sendPickUpAnswer(true);
                                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE);
                                    } else {
                                        sendPickUpAnswer(false);
                                    }
                                }
                            })
                            .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    sendPickUpAnswer(false);
                                }
                            }).show();*/
                    sendPickUpAnswer(false);
                } else {
                    webRTCView.handleWebRTCMessage(messageType, intent.getStringExtra(NinchatSessionManager.Broadcast.WEBRTC_MESSAGE_CONTENT));
                }
            }
        }
    };

    public void onSendClick(final View view) {
        final TextView messageView = findViewById(R.id.message);
        final String message = messageView.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        NinchatSessionManager.getInstance().sendMessage(message);
        lastSentMessage = message;
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
        messageAdapter = new NinchatMessageAdapter();
        messages.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messages.setAdapter(messageAdapter);
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
