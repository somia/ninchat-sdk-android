package com.ninchat.sdk.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.util.Date;

public final class NinchatMessage {

    public enum Type {
        START,
        MESSAGE,
        END
    }

    private Type type;
    private String sender;
    private String message;
    private Date timestamp;
    private boolean isRemoteMessage = false;

    public NinchatMessage(final Type type) {
        this(type, null, null, 0, false);
    }

    public NinchatMessage(final String message, final String sender, long timestamp, final boolean isRemoteMessage) {
        this(Type.MESSAGE, message, sender, timestamp, isRemoteMessage);
    }

    private NinchatMessage(final Type type, final String message, final String sender, long timestamp, final boolean isRemoteMessage) {
        this.type = type;
        this.message = message;
        this.sender = sender;
        this.timestamp = new Date(timestamp);
        this.isRemoteMessage = isRemoteMessage;
    }

    public Type getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public Spanned getMessage() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(message, 0) : Html.fromHtml(message);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isRemoteMessage() {
        return isRemoteMessage;
    }
}
