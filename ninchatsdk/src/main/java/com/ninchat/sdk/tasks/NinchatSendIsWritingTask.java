package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatSendIsWritingTask extends NinchatBaseTask {

    public static void start(final String channelId, final String userId, final boolean isWriting) {
        new NinchatSendIsWritingTask(channelId, userId, isWriting).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String channelId;
    private String userId;
    private boolean isWriting;

    private NinchatSendIsWritingTask(final String channelId, final String userId, final boolean isWriting) {
        this.channelId = channelId;
        this.userId = userId;
        this.isWriting = isWriting;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final Props memberAttrs = new Props();
        memberAttrs.setBool("writing", isWriting);
        final Props params = new Props();
        params.setString("action", "update_member");
        params.setString("channel_id", channelId);
        params.setString("user_id", userId);
        params.setObject("member_attrs", memberAttrs);
        try {
            NinchatSessionManager.getInstance().getSession().send(params, null);
        } catch (final Exception e) {
            return e;
        }
        return null;
    }
}
