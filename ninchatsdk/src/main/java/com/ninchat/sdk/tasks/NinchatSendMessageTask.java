package com.ninchat.sdk.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.client.Strings;
import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatSendMessageTask extends NinchatBaseTask {

    public static void start(final String messageType, final String message, final String channelId) {
        new NinchatSendMessageTask(messageType, message, channelId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void start(final String messageType, final String message, final String channelId, final NinchatSessionManager.RequestCallback requestCallback) {
        new NinchatSendMessageTask(messageType, message, channelId, requestCallback).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String messageType;
    private String message;
    private String channelId;
    private NinchatSessionManager.RequestCallback requestCallback;

    protected NinchatSendMessageTask(final String messageType, final String message, final String channelId) {
        this.messageType = messageType;
        this.message = message;
        this.channelId = channelId;
        this.requestCallback = null;
    }

    protected NinchatSendMessageTask(final String messageType, final String message, final String channelId, final NinchatSessionManager.RequestCallback requestCallback) {
        this.messageType = messageType;
        this.message = message;
        this.channelId = channelId;
        this.requestCallback = requestCallback;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props params = new Props();
        params.setString("action", "send_message");
        params.setString("message_type", messageType);
        params.setString("channel_id", channelId);
        if (messageType.startsWith(NinchatSessionManager.MessageTypes.WEBRTC_PREFIX)) {
            params.setInt("message_ttl", 10);
        } else if (messageType.equals(NinchatSessionManager.MessageTypes.RATING_OR_POST_ANSWERS)) {
            if (requestCallback != null) {
                // todo (pallab) workaround - request callback now only for post questionnaire
                params.setBool("message_fold", true);

            } else {
                params.setStringArray("message_recipient_ids", new Strings());
                params.setBool("message_fold", false);
            }

        }
        Payload payload = null;
        if (message != null) {
            payload = new Payload();
            payload.append(message.getBytes());
        }
        try {
            long actionId = NinchatSessionManager.getInstance().getSession().send(params, payload);
            if (requestCallback != null)
                requestCallback.onActionId(actionId);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
