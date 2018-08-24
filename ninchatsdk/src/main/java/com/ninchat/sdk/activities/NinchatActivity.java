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

    public static Intent getLaunchIntent(final Context context, final String configurationKey, final String siteSecret) {
        return new Intent(context, NinchatActivity.class)
                .putExtra(CONFIGURATION_KEY, configurationKey)
                .putExtra(SITE_SECRET, siteSecret);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat;
    }

    protected String configurationKey;

    protected NinchatSessionManager.ConfigurationFetchListener configurationFetchListener = new NinchatSessionManager.ConfigurationFetchListener() {
        @Override
        public void success() {

        }

        @Override
        public void failure(final Exception error) {
            Toast.makeText(NinchatActivity.this, getString(R.string.ninchat_configuration_fetch_error) + " " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void handleOnCreateIntent(Intent intent) {
        configurationKey = intent.getStringExtra(CONFIGURATION_KEY);
        NinchatSessionManager.fetchConfig(getResources(), configurationFetchListener, configurationKey);
    }

    public void onBlogLinkClick(final View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.ninchat.com")));
    }

    public void onStartButtonClick(final View view) {
        startActivityForResult(NinchatQueueActivity.getLaunchIntent(this), NinchatQueueActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatQueueActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
