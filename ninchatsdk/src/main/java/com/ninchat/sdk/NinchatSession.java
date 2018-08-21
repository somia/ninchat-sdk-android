package com.ninchat.sdk;

import android.app.Activity;
import android.content.Intent;

import com.ninchat.sdk.activities.NinchatActivity;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
public final class NinchatSession {

    public static final int NINCHAT_SESSION_REQUEST_CODE = NinchatSession.class.hashCode() & 0xffff;

    public static void start(final Activity activity) {
        start(activity, NINCHAT_SESSION_REQUEST_CODE);
    }

    public static void start(final Activity activity, final int requestCode) {
        activity.startActivityForResult(new Intent(activity, NinchatActivity.class), requestCode);
    }
}
