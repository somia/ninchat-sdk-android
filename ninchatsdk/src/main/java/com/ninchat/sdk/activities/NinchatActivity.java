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

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;


public final class NinchatActivity extends BaseActivity {

    protected static final String SHOW_LAUNCHER = "showLauncher";

    public static Intent getLaunchIntent(final Context context, final boolean showLauncher) {
        return new Intent(context, NinchatActivity.class)
                .putExtra(SHOW_LAUNCHER, showLauncher);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat;
    }

    protected boolean showLauncher = true;

    @Override
    protected void handleOnCreateIntent(Intent intent) {
        showLauncher = intent.getBooleanExtra(SHOW_LAUNCHER, true);
        if (!showLauncher) {
            openQueueActivity();
        }
    }

    protected BroadcastReceiver queuesFoundReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.QUEUES_FOUND.equals(action)) {
                // Enable the button
                findViewById(R.id.start_button).setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(queuesFoundReceiver, new IntentFilter(NinchatSessionManager.Broadcast.QUEUES_FOUND));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(queuesFoundReceiver);
    }

    public void onBlogLinkClick(final View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.ninchat.com")));
    }

    public void onStartButtonClick(final View view) {
       openQueueActivity();
    }

    private void openQueueActivity() {
        startActivityForResult(NinchatQueueActivity.getLaunchIntent(this), NinchatQueueActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatQueueActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK || !showLauncher) {
                setResult(resultCode, data);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
