package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 29/08/2018.
 */
public final class NinchatRegisterAudienceTask extends NinchatBaseTask {

    public static void start(final String queueId) {
        new NinchatRegisterAudienceTask(queueId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String queueId;

    protected NinchatRegisterAudienceTask(final String queueId) {
        this.queueId = queueId;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "register_audience");
        params.setString("queue_id", queueId);
        final Props audienceMetadata = NinchatSessionManager.getInstance().getAudienceMetadata();
        if (audienceMetadata != null) {
            params.setObject("audience_metadata", audienceMetadata);
        }
        try {
            NinchatSessionManager.getInstance().getSession().send(params, null);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
