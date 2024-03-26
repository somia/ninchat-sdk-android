package com.ninchat.sdk;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.sdk.models.NinchatSessionCredentials;

public abstract class NinchatSDKEventListener {
    public void onSessionInitiated(NinchatSessionCredentials sessionCredentials) {}
    public void onSessionStarted() {}
    public void onSessionInitFailed() {} // TODO: Add error types
    public void onSessionEvent(final Props params) {}
    public void onEvent(final Props params, final Payload payload) {}
    public void onSessionError(final Exception error) {}
    public void onActivityDropped() {}
}
