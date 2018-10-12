package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatSendFileTask extends NinchatBaseTask {

    public static void start(final String name, final byte[] data, final String channelId) {
        new NinchatSendFileTask(name, data, channelId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String name;
    private String fileType;
    private byte[] data;
    private String channelId;

    private NinchatSendFileTask(final String name, final byte[] data, final String channelId) {
        this.name = name;
        this.data = data;
        this.channelId = channelId;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "send_file");
        params.setString("channel_id", channelId);
        final Props fileAttrs = new Props();
        fileAttrs.setString("name", name);
        params.setObject("file_attrs", fileAttrs);
        final Payload payload = new Payload();
        payload.append(data);
        try {
            NinchatSessionManager.getInstance().getSession().send(params, payload);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
