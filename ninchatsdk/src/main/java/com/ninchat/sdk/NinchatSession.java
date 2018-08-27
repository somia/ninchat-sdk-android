package com.ninchat.sdk;

import android.app.Activity;

import com.ninchat.sdk.activities.NinchatActivity;

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

    public static final int NINCHAT_SESSION_REQUEST_CODE = NinchatSession.class.hashCode() & 0xffff;

    public static void start(final Activity activity, final String configurationKey) {
        start(activity, NINCHAT_SESSION_REQUEST_CODE, configurationKey, null, false);
    }

    public static void start(final Activity activity, final String configurationKey, final String siteSecret) {
        start(activity, NINCHAT_SESSION_REQUEST_CODE, configurationKey, siteSecret, false);
    }

    public static void start(final Activity activity, final int requestCode, final String configurationKey) {
        start(activity, requestCode, configurationKey, null, false);
    }

    public static void start(final Activity activity, final int requestCode, final String configurationKey, final String siteSecret) {
        start(activity, requestCode, configurationKey, siteSecret, false);
    }

    public static void start(final Activity activity, final int requestCode, final String configurationKey, final String siteSecret, final boolean showLauncher) {
        activity.startActivityForResult(NinchatActivity.getLaunchIntent(activity, showLauncher), requestCode);
        NinchatSessionManager.init(activity.getApplicationContext(), configurationKey, siteSecret);
    }
}
