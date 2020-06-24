package com.ninchat.sdk.events;

public class RequireStateChange {
    public final boolean hasError;

    public RequireStateChange(boolean hasError) {
        this.hasError = hasError;
    }
}
