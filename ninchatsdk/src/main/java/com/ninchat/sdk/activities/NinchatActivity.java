package com.ninchat.sdk.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatQueueListAdapter;


public final class NinchatActivity extends NinchatBaseActivity {

    protected static final String QUEUE_ID = "queueId";

    public static Intent getLaunchIntent(final Context context, final String queueId) {
        return new Intent(context, NinchatActivity.class)
                .putExtra(QUEUE_ID, queueId);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat;
    }

    protected String queueId;

    protected BroadcastReceiver queuesUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSession.Broadcast.QUEUES_UPDATED.equals(action)) {
                setQueueAdapter();
            }
        }
    };

    private void setQueueAdapter() {
        final RecyclerView queueList = findViewById(R.id.ninchat_activity_queue_list);
        final NinchatQueueListAdapter ninchatQueueListAdapter = NinchatSessionManager.getInstance().getNinchatQueueListAdapter(NinchatActivity.this);
        queueList.setAdapter(ninchatQueueListAdapter);
        if (ninchatQueueListAdapter.getItemCount() == 0) {
            findViewById(R.id.ninchat_activity_no_queues).setVisibility(View.VISIBLE);
            final TextView motd = findViewById(R.id.ninchat_activity_motd);
            motd.setText(NinchatSessionManager.getInstance().getNoQueues());
            findViewById(R.id.ninchat_activity_close).setVisibility(View.VISIBLE);
        }
    }

    protected BroadcastReceiver configurationFetchedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NinchatSession.Broadcast.CONFIGURATION_FETCHED.equals(intent.getAction())) {
                setTexts();
            }
        }
    };

    private void setTexts() {
        final NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        final TextView topHeader = findViewById(R.id.ninchat_activity_header);
        topHeader.setText(sessionManager.getWelcome());
        final Button closeButton = findViewById(R.id.ninchat_activity_close);
        closeButton.setText(sessionManager.getCloseWindow());
        closeButton.setVisibility(sessionManager.showNoThanksButton() ? View.VISIBLE : View.GONE);
        final TextView motd = findViewById(R.id.ninchat_activity_motd);
        motd.setText(sessionManager.getMOTD());
        final TextView noQueues = findViewById(R.id.ninchat_activity_no_queues);
        noQueues.setText(sessionManager.getNoQueues());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        if (intent != null) {
            queueId = intent.getStringExtra(QUEUE_ID);
            if (queueId != null) {
                openQueueActivity();
            }
        }
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(queuesUpdatedReceiver, new IntentFilter(NinchatSession.Broadcast.QUEUES_UPDATED));
        broadcastManager.registerReceiver(configurationFetchedReceiver, new IntentFilter(NinchatSession.Broadcast.CONFIGURATION_FETCHED));
        final NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        if (sessionManager != null) {
            setQueueAdapter();
            setTexts();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(queuesUpdatedReceiver);
        broadcastManager.unregisterReceiver(configurationFetchedReceiver);
    }

    public void onCloseClick(final View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void openQueueActivity() {
        startActivityForResult(NinchatQueueActivity.getLaunchIntent(this, queueId), NinchatQueueActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatQueueActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK || queueId != null) {
                setResult(resultCode, data);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
