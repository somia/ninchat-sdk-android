package com.ninchat.sdk;

import android.content.Context;
import android.content.res.Resources;

import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
final class NinchatSessionManager {

    protected final static NinchatConfigurationFetchTask.Listener configurationFetchListener = new NinchatConfigurationFetchTask.Listener() {
        @Override
        public void success(final String config) {
            try {
                configuration = new JSONObject(config);
            } catch (final JSONException e) {
                configuration = null;
            }
        }

        @Override
        public void failure(final Exception error) {
            configuration = null;
        }
    };

    protected static JSONObject configuration;

    public static void fetchConfig(final Context context, final String configurationKey) {
        final Resources resources = context.getResources();
        NinchatConfigurationFetchTask.start(configurationFetchListener, resources.getString(R.string.__ninchat_server) + resources.getString(R.string.__ninchat_config_endpoint, configurationKey));
    }
}
