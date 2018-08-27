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
import android.widget.Toast;

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

    private BroadcastReceiver configurationFetchStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(NinchatSessionManager.CONFIGURATION_FETCH_ERROR)) {
                final Exception error = (Exception) intent.getSerializableExtra(NinchatSessionManager.CONFIGURATION_FETCH_ERROR_REASON);
                Toast.makeText(NinchatActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(configurationFetchStatusBroadcastReceiver, new IntentFilter(NinchatSessionManager.CONFIGURATION_FETCH_ERROR));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(configurationFetchStatusBroadcastReceiver);
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
