package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.sdk.NinchatSessionManager;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 27/08/2018.
 */
public final class NinchatListQueuesTask extends NinchatBaseTask {

    public static void start() {
        new NinchatListQueuesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        final Session session = sessionManager.getSession();
        if (session == null) {
            return new Exception("No chat session!");
        }
        final Props params = new Props();
        params.setString("action", "describe_realm_queues");
        params.setString("realm_id", sessionManager.getRealmId());
        try {
            session.send(params, null);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }

}
