package com.ninchat.sdk.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatQueueActivity extends BaseActivity {

    static final int REQUEST_CODE = NinchatQueueActivity.class.hashCode() & 0xffff;

    protected final static String QUEUE_ID = "queueId";

    static Intent getLaunchIntent(final Context context, final String queueId) {
        return new Intent(context, NinchatQueueActivity.class).putExtra(QUEUE_ID, queueId);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_queue;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatChatActivity.REQUEST_CODE) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    protected BroadcastReceiver channelJoinedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.CHANNEL_JOINED.equals(action)) {
                startActivityForResult(new Intent(NinchatQueueActivity.this, NinchatChatActivity.class), NinchatChatActivity.REQUEST_CODE);
            }
        }
    };

    private String queueId;

    @Override
    protected void handleOnCreateIntent(Intent intent) {
        super.handleOnCreateIntent(intent);
        queueId = intent.getStringExtra(QUEUE_ID);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(channelJoinedBroadcastReceiver, new IntentFilter(NinchatSessionManager.Broadcast.CHANNEL_JOINED));
        NinchatSessionManager.joinQueue(queueId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(channelJoinedBroadcastReceiver);
    }
}
