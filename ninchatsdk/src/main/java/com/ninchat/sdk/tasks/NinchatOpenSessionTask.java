package com.ninchat.sdk.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.client.Strings;
import com.ninchat.sdk.NinchatSessionManager;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 27/08/2018.
 */
public final class NinchatOpenSessionTask extends BaseTask {

    public static void start(final String siteSecret) {
        new NinchatOpenSessionTask(siteSecret).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected static final String TAG = NinchatOpenSessionTask.class.getSimpleName();

    protected String siteSecret;

    protected NinchatOpenSessionTask(final String siteSecret) {
        this.siteSecret = siteSecret;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Strings messageTypes = new Strings();
        messageTypes.append("ninchat.com/*");
        final Props sessionParams = new Props();
        if (siteSecret != null) {
            sessionParams.setString("site_secret", siteSecret);
        }
        sessionParams.setStringArray("message_types", messageTypes);
        final Session session = new Session();
        session.setAddress(NinchatSessionManager.getServer());
        try {
            session.setParams(sessionParams);
        } catch (final Exception e) {
            Log.e(TAG, "setParams failed!", e);
            return e;
        }
        NinchatSessionManager.getInstance().setSession(session);
        try {
            session.open();
        } catch (final Exception e) {
            Log.e(TAG, "open failed!", e);
            return e;
        }
        return null;
    }

}
