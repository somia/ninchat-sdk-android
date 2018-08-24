package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;


public final class NinchatActivity extends BaseActivity {

    protected static final String CONFIGURATION_KEY = "configurationKey";
    protected static final String SITE_SECRET = "siteSecret";
    protected static final String SHOW_LAUNCHER = "showLauncher";

    public static Intent getLaunchIntent(final Context context, final String configurationKey, final String siteSecret, final boolean showLauncher) {
        return new Intent(context, NinchatActivity.class)
                .putExtra(CONFIGURATION_KEY, configurationKey)
                .putExtra(SITE_SECRET, siteSecret)
                .putExtra(SHOW_LAUNCHER, showLauncher);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat;
    }

    protected NinchatSessionManager.ConfigurationFetchListener configurationFetchListener = new NinchatSessionManager.ConfigurationFetchListener() {
        @Override
        public void success() {
        }

        @Override
        public void failure(final Exception error) {
            Toast.makeText(NinchatActivity.this, getString(R.string.ninchat_configuration_fetch_error, error.getMessage()), Toast.LENGTH_LONG).show();
        }
    };

    protected boolean showLauncher = true;

    @Override
    protected void handleOnCreateIntent(Intent intent) {
        final String configurationKey = intent.getStringExtra(CONFIGURATION_KEY);
        showLauncher = intent.getBooleanExtra(SHOW_LAUNCHER, true);
        if (showLauncher) {
            NinchatSessionManager.fetchConfig(getResources(), configurationFetchListener, configurationKey);
        } else {
            openQueueActivity(configurationKey);
        }
    }

    public void onBlogLinkClick(final View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.ninchat.com")));
    }

    public void onStartButtonClick(final View view) {
       openQueueActivity(null);
    }

    private void openQueueActivity(final String configurationKey) {
        startActivityForResult(NinchatQueueActivity.getLaunchIntent(this, configurationKey), NinchatQueueActivity.REQUEST_CODE);
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
