package com.ninchat.sdk;

import android.app.Activity;
import android.content.Intent;

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
        start(activity, NINCHAT_SESSION_REQUEST_CODE, configurationKey, null);
    }

    public static void start(final Activity activity, final String configurationKey, final String siteSecret) {
        start(activity, NINCHAT_SESSION_REQUEST_CODE, configurationKey, siteSecret);
    }

    public static void start(final Activity activity, final int requestCode, final String configurationKey) {
        start(activity, requestCode, configurationKey, null);
    }

    public static void start(final Activity activity, final int requestCode, final String configurationKey, final String siteSecret) {
        activity.startActivityForResult(NinchatActivity.getLaunchIntent(activity, configurationKey, siteSecret), requestCode);
    }
}
