package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatSendBeginIceTask extends NinchatBaseTask {

    public static void start() {
        new NinchatSendBeginIceTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "begin_ice");
        try {
            NinchatSessionManager.getInstance().getSession().send(params, null);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
