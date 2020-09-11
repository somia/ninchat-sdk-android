package com.ninchat.sdk.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

// Used for removing user from queue
public class NinchatDeleteUserTask extends NinchatBaseTask {

    public static void start(final NinchatSessionManager.RequestCallback requestCallback) {
        new NinchatDeleteUserTask(requestCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private NinchatSessionManager.RequestCallback requestCallback;

    protected NinchatDeleteUserTask(final NinchatSessionManager.RequestCallback requestCallback) {
        this.requestCallback = requestCallback;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "delete_user");
        try {
            long actionId = NinchatSessionManager.getInstance().getSession().send(params, null);
            if (requestCallback != null)
                requestCallback.onActionId(actionId);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
