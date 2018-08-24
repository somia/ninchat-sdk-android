package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatQueueActivity extends BaseActivity {

    static final int REQUEST_CODE = NinchatQueueActivity.class.hashCode() & 0xffff;

    protected static final String CONFIGURATION_KEY = "configurationKey";

    static Intent getLaunchIntent(final Context context, final String configurationKey) {
        return new Intent(context, NinchatQueueActivity.class)
                .putExtra(CONFIGURATION_KEY, configurationKey);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_queue;
    }

    protected NinchatSessionManager.ConfigurationFetchListener configurationFetchListener = new NinchatSessionManager.ConfigurationFetchListener() {
        @Override
        public void success() {
        }

        @Override
        public void failure(final Exception error) {
            Toast.makeText(NinchatQueueActivity.this, getString(R.string.ninchat_configuration_fetch_error, error.getMessage()), Toast.LENGTH_LONG).show();;
        }
    };

    @Override
    protected void handleOnCreateIntent(Intent intent) {
        final String configurationKey = intent.getStringExtra(CONFIGURATION_KEY);
        if (configurationKey != null) {
            NinchatSessionManager.fetchConfig(getResources(), configurationFetchListener, configurationKey);
        }
    }
}
