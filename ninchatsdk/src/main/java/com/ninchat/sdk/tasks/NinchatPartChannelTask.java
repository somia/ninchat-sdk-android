package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatPartChannelTask extends NinchatBaseTask {

    public static void start(final String channelId) {
        new NinchatPartChannelTask(channelId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String channelId;

    private NinchatPartChannelTask(String channelId) {
        this.channelId = channelId;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        if (channelId == null) {
            return null;
        }
        final Props params = new Props();
        params.setString("action", "part_channel");
        params.setString("channel_id", channelId);
        try {
            NinchatSessionManager.getInstance().getSession().send(params, null);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
