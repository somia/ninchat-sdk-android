package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatDescribeChannelTask extends NinchatBaseTask {

    public static void start(final String channelId, final NinchatSessionManager.RequestCallback requestCallback) {
        new NinchatDescribeChannelTask(channelId, requestCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String channelId;
    private NinchatSessionManager.RequestCallback requestCallback;

    protected NinchatDescribeChannelTask(final String channelId, final NinchatSessionManager.RequestCallback requestCallback) {
        this.channelId = channelId;
        this.requestCallback = requestCallback;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "describe_channel");
        params.setString("channel_id", channelId);
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
