package com.ninchat.sdk;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;

public abstract class NinchatSDKEventListener {
    public void onSessionInitiated() {}
    public void onSessionStarted() {}
    public void onSessionInitFailed() {}
    public void onSessionEvent(final Props params) {}
    public void onEvent(final Props params, final Payload payload) {}
    public void onSessionError(final Exception error) {}
}
