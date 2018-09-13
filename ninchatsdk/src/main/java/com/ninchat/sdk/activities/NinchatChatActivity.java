package com.ninchat.sdk.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends NinchatBaseActivity {

    static int REQUEST_CODE = NinchatChatActivity.class.hashCode() & 0xffff;

    private NinchatMessageAdapter messageAdapter;
    private boolean lastMessageWasRemote = false;
    private String lastSentMessage = null;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_chat;
    }

    protected BroadcastReceiver channelClosedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.CHANNEL_CLOSED.equals(action)) {
                NinchatChatActivity.this.setResult(RESULT_OK);
                finish();
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
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(channelClosedReceiver, new IntentFilter(NinchatSessionManager.Broadcast.CHANNEL_CLOSED));
        localBroadcastManager.registerReceiver(messageReceiver, new IntentFilter(NinchatSessionManager.Broadcast.NEW_MESSAGE));
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
    }
}
