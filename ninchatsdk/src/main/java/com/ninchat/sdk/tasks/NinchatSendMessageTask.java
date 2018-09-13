package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public final class NinchatSendMessageTask extends BaseTask {

    public static void start(final String message, final String channelId) {
        new NinchatSendMessageTask(message, channelId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String message;
    private String channelId;

    protected NinchatSendMessageTask(final String message, final String channelId) {
        this.message = message;
        this.channelId = channelId;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final JSONObject data = new JSONObject();
        try {
            data.put("text", message);
        } catch (final JSONException e) {
            return e;
        }
        final Props params = new Props();
        params.setString("action", "send_message");
        params.setString("message_type", "ninchat.com/text");
        params.setString("channel_id", channelId);
        final Payload payload = new Payload();
        payload.append(data.toString().getBytes());
        try {
            NinchatSessionManager.getInstance().getSession().send(params, payload);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
