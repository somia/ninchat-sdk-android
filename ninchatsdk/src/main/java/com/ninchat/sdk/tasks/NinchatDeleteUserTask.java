package com.ninchat.sdk.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

// Used for removing user from queue
public class NinchatDeleteUserTask extends NinchatBaseTask {

    public static void start() {
        new NinchatDeleteUserTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action","delete_user");
        try {
            NinchatSessionManager.getInstance().getSession().send(params, null);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
