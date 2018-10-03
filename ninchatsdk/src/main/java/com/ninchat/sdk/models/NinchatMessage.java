package com.ninchat.sdk.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

public final class NinchatMessage {

    private String sender;
    private String message;
    private boolean isRemoteMessage = false;

    public NinchatMessage(String message) {
        this(message, null, false);
    }

    public NinchatMessage(String message, String sender, boolean isRemoteMessage) {
        this.sender = sender;
        this.message = message;
        this.isRemoteMessage = isRemoteMessage;
    }

    public String getSender() {
        return sender;
    }

    public Spanned getMessage() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(message, 0) : Html.fromHtml(message);
    }

    public boolean isRemoteMessage() {
        return isRemoteMessage;
    }
}
