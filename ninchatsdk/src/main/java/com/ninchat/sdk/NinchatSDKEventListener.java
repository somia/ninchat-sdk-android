package com.ninchat.sdk;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;

public interface NinchatSDKEventListener {
    void onSessionInitiated();
    void onSessionStarted();
    void onSessionInitFailed();
    void onSessionEvent(final Props params);
    void onEvent(final Props params, final Payload payload);
    void onSessionError(final Exception error);
}
