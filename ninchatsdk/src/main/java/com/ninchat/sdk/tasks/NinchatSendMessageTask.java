package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.client.Strings;
import com.ninchat.sdk.NinchatSessionManager;

import org.json.JSONException;
import org.json.JSONObject;

public final class NinchatSendMessageTask extends NinchatBaseTask {

    public static void start(final String messageType, final String message, final String channelId) {
        new NinchatSendMessageTask(messageType, message, channelId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String messageType;
    private String message;
    private String channelId;

    protected NinchatSendMessageTask(final String messageType, final String message, final String channelId) {
        this.messageType = messageType;
        this.message = message;
        this.channelId = channelId;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "send_message");
        params.setString("message_type", messageType);
        params.setString("channel_id", channelId);
        if (messageType.startsWith(NinchatSessionManager.MessageTypes.WEBRTC_PREFIX)) {
            params.setInt("message_ttl", 10);
        } else if (messageType.equals(NinchatSessionManager.MessageTypes.RATING)) {
            params.setStringArray("message_recipient_ids", new Strings());
            params.setBool("message_fold", true);
        }
        final Payload payload = new Payload();
        payload.append(message.getBytes());
        try {
            NinchatSessionManager.getInstance().getSession().send(params, payload);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
