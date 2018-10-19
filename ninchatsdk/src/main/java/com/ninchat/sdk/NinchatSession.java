package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;

import com.ninchat.client.Props;
import com.ninchat.sdk.models.NinchatQueue;
import com.ninchat.sdk.tasks.NinchatOpenSessionTask;

import java.util.List;

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
        public static final String CONFIGURATION_FETCHED = BuildConfig.APPLICATION_ID + ".CONFIGURATION_FETCHED";
        public static final String SESSION_CREATED = BuildConfig.APPLICATION_ID + ".SESSION_CREATED";
        public static final String QUEUES_UPDATED = BuildConfig.APPLICATION_ID + ".QUEUES_UPDATED";
    }

    public static final int NINCHAT_SESSION_REQUEST_CODE = NinchatSession.class.hashCode() & 0xffff;

    public NinchatSession(final Context applicationContext, final String configurationKey) {
        this(applicationContext, configurationKey, null, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, final NinchatSDKEventListener eventListener, final NinchatSDKLogListener logListener) {
        NinchatSessionManager.init(applicationContext, configurationKey, eventListener, logListener);
    }

    public void setServerAddress(final String serverAddress) {
        NinchatSessionManager.getInstance().setServerAddress(serverAddress);
    }

    public void setAudienceMetadata(final Props audienceMetadata) {
        NinchatSessionManager.getInstance().setAudienceMetadata(audienceMetadata);
    }

    public void start(final Activity activity, String siteSecret) {
        start(activity, siteSecret, NINCHAT_SESSION_REQUEST_CODE);
    }

    public void start(final Activity activity, String siteSecret, final int requestCode) {
        start(activity, siteSecret, requestCode, null);
    }

    public void start(final Activity activity, final String siteSecret, final String queueId) {
        start(activity, siteSecret, NINCHAT_SESSION_REQUEST_CODE, queueId);
    }

    public void start(final Activity activity, final String siteSecret, final int requestCode, final String queueId) {
        NinchatOpenSessionTask.start(activity, siteSecret, requestCode, queueId);
    }

    public void close() {
        NinchatSessionManager.getInstance().close();
    }

    public List<NinchatQueue> getQueues() {
        return NinchatSessionManager.getInstance().getQueues();
    }

}
