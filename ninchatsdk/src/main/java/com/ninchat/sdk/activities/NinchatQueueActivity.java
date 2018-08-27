package com.ninchat.sdk.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatQueueActivity extends BaseActivity {

    static final int REQUEST_CODE = NinchatQueueActivity.class.hashCode() & 0xffff;

    static Intent getLaunchIntent(final Context context) {
        return new Intent(context, NinchatQueueActivity.class);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_queue;
    }

    private BroadcastReceiver configurationFetchStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(NinchatSessionManager.CONFIGURATION_FETCH_ERROR)) {
                final Exception error = (Exception) intent.getSerializableExtra(NinchatSessionManager.CONFIGURATION_FETCH_ERROR_REASON);
                Toast.makeText(NinchatQueueActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this).registerReceiver(configurationFetchStatusBroadcastReceiver, new IntentFilter(NinchatSessionManager.CONFIGURATION_FETCH_ERROR));
    }
}
