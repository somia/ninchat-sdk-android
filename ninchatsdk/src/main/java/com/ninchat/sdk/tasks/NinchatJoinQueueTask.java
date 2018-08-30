package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 29/08/2018.
 */
public final class NinchatJoinQueueTask extends BaseTask {

    public static void start(final String queueId) {
        new NinchatJoinQueueTask(queueId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String queueId;

    protected NinchatJoinQueueTask(final String queueId) {
        this.queueId = queueId;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "request_audience");
        params.setString("queue_id", queueId);
        try {
            NinchatSessionManager.getInstance().getSession().send(params, null);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
