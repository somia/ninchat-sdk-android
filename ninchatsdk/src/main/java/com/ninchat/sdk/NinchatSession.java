package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.ninchat.sdk.activities.NinchatActivity;
import com.ninchat.sdk.adapters.QueueListAdapter;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
public final class NinchatSession {

    public static final class Analytics {

        public static final class Keys {
            public static final String RATING = "rating";
        }

        public static final class Rating {
            public static final int GOOD = 1;
            public static final int FAIR = 0;
            public static final int POOR = -1;
            public static final int NO_ANSWER = -2;
        }

    }

    public static final class Broadcast {
        public static final String QUEUES_UPDATED = BuildConfig.APPLICATION_ID + ".QUEUES_UPDATED";
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, final String siteSecret) {
        NinchatSessionManager.init(applicationContext, configurationKey, siteSecret);
    }

    public static final int NINCHAT_SESSION_REQUEST_CODE = NinchatSession.class.hashCode() & 0xffff;

    public void start(final Activity activity) {
        start(activity, NINCHAT_SESSION_REQUEST_CODE);
    }

    public void start(final Activity activity, final int requestCode) {
        start(activity, requestCode, null);
    }

    public void start(final Activity activity, final int requestCode, final String queueId) {
        activity.startActivityForResult(NinchatActivity.getLaunchIntent(activity, queueId), requestCode);
    }

    public void close() {
        NinchatSessionManager.getInstance().close();
    }

    public RecyclerView.Adapter<QueueListAdapter.QueueViewHolder> getQueueAdapter(final Activity activity) {
        return NinchatSessionManager.getInstance().getQueueListAdapter(activity);
    }

}
