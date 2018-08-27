package com.ninchat.sdk;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ninchat.client.CloseHandler;
import com.ninchat.client.ConnStateHandler;
import com.ninchat.client.EventHandler;
import com.ninchat.client.LogHandler;
import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.client.SessionEventHandler;
import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;
import com.ninchat.sdk.tasks.NinchatOpenSessionTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager implements SessionEventHandler, EventHandler, CloseHandler, ConnStateHandler, LogHandler {

    static void init(final Context context, final String configurationKey, final String siteSecret) {
        instance = new NinchatSessionManager(context, siteSecret);
        NinchatConfigurationFetchTask.start(configurationKey);
    }

    public static String getServer() {
        return BuildConfig.DEBUG ? "api.luupi.net" : "api.ninchat.com";
    }

    public static NinchatSessionManager getInstance() {
        return instance;
    }

    protected static NinchatSessionManager instance;
    protected static final String TAG = NinchatSessionManager.class.getSimpleName();

    protected NinchatSessionManager(final Context context, final String siteSecret) {
        this.contextWeakReference = new WeakReference<>(context);
        this.siteSecret = siteSecret;
    }

    protected WeakReference<Context> contextWeakReference;
    protected String siteSecret;

    protected JSONObject configuration;
    protected Session session;

    public void setConfiguration(final String config) throws JSONException {
        try {
            this.configuration = new JSONObject(config);
        } catch (final JSONException e) {
            this.configuration = null;
            throw e;
        }
        NinchatOpenSessionTask.start(siteSecret);
    }

    public void sessionError(final Exception error) {
        final Context context = contextWeakReference.get();
        if (context != null) {
            Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void setSession(final Session session) {
        this.session = session;
        this.session.setOnSessionEvent(this);
        this.session.setOnEvent(this);
        this.session.setOnClose(this);
        this.session.setOnConnState(this);
        this.session.setOnLog(this);

    }

    @Override
    public void onSessionEvent(Props params) {
        try {
            final String event = params.getString("event");
            if (event.equals("session_created")) {
                // TODO: Queue handling
            }
        } catch (final Exception e) {
            Log.e(TAG, "onSessionEvent: " + params.string());
        }
    }

    @Override
    public void onEvent(Props params, Payload payload, boolean lastReply) {
        Log.e(TAG, "onEvent: " + params.string() + ", " + payload.string() + ", " + lastReply);
    }

    @Override
    public void onClose() {
        Log.e(TAG, "onClose");
    }

    @Override
    public void onConnState(String state) {
        Log.e(TAG, "onConnState: " + state);
    }

    @Override
    public void onLog(String msg) {
        Log.e(TAG, "onLog: " + msg);
    }
}
