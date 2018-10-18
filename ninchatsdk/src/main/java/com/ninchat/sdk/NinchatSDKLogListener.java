package com.ninchat.sdk;

public interface NinchatSDKLogListener {
    void onLog(final String message, final Throwable throwable);
}
