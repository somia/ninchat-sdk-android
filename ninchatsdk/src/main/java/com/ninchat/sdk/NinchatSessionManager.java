package com.ninchat.sdk;

import android.content.res.Resources;

import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager {

    public interface ConfigurationFetchListener {
        void success();
        void failure(final Exception error);
    }

    protected static JSONObject configuration;

    public static void setConfiguration(final String config) {
        try {
            configuration = new JSONObject(config);
        } catch (final JSONException e) {
            configuration = null;
        }
    }

    public static void fetchConfig(final Resources resources, final ConfigurationFetchListener listener, final String configurationKey) {
        NinchatConfigurationFetchTask.start(listener,resources.getString(R.string.__ninchat_server) + resources.getString(R.string.__ninchat_config_endpoint, configurationKey));
    }
}
