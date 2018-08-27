package com.ninchat.sdk;

import android.app.Activity;

import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager {

    static void init(final Activity activity, final String configurationKey, final String siteSecret) {
        instance = new NinchatSessionManager(siteSecret);
        NinchatConfigurationFetchTask.start(activity.getApplicationContext(), activity.getResources().getString(R.string.__ninchat_server) + activity.getResources().getString(R.string.__ninchat_config_endpoint, configurationKey));
    }

    public static final String CONFIGURATION_FETCH_ERROR = "configurationFetchError";
    public static final String CONFIGURATION_FETCH_ERROR_REASON = "configurationFetchErrorReason";

    protected NinchatSessionManager(final String siteSecret) {
        this.siteSecret = siteSecret;
    }

    public static NinchatSessionManager getInstance() {
        return instance;
    }

    protected static NinchatSessionManager instance;

    protected String siteSecret;
    protected JSONObject configuration;

    public void setConfiguration(final String config) throws JSONException {
        try {
            configuration = new JSONObject(config);
        } catch (final JSONException e) {
            configuration = null;
            throw e;
        }
    }
}
