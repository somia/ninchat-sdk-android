package com.ninchat.sdk.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;


public final class NinchatActivity extends BaseActivity {

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

    @Override
    protected void handleOnCreateIntent(Intent intent) {
        queueId = intent.getStringExtra(QUEUE_ID);
        if (queueId != null) {
            openQueueActivity();
        }
    }

    protected BroadcastReceiver queuesUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSession.Broadcast.QUEUES_UPDATED.equals(action)) {
                // Enable the button
                findViewById(R.id.start_button).setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(queuesUpdatedReceiver, new IntentFilter(NinchatSession.Broadcast.QUEUES_UPDATED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(queuesUpdatedReceiver);
    }

    public void onBlogLinkClick(final View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.ninchat.com")));
    }

    public void onStartButtonClick(final View view) {
        openQueueActivity();
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
