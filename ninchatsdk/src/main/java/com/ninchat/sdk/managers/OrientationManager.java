package com.ninchat.sdk.managers;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.OrientationEventListener;

public class OrientationManager extends OrientationEventListener {
    private Activity activity;
    private int oldRotation = -1;
    private static final int ORIENTATION_CHANGE_THRESHOLD = 15; // How much phone needs to be tilted for an orientation change

    public OrientationManager(Activity activity, int rate) {
        super(activity, rate);

        this.activity = activity;
    }

    @Override
    public void onOrientationChanged(int rotation) {

        // Set portrait/landscape based on the phone angle
        if ((rotation <= 80) || (rotation >= 320)) {
            if (isOrientationChange(rotation)) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (isOrientationChange(rotation)) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        oldRotation = rotation;
    }

    // Return true if phone orientation moved more than given threshold
    private boolean isOrientationChange(int rotation) {
        int change = rotation >= oldRotation ? rotation - oldRotation : oldRotation - rotation;

        if (change >= ORIENTATION_CHANGE_THRESHOLD) {
            return true;
        }

        return false;
    }
}
