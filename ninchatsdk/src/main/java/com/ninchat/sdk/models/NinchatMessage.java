package com.ninchat.sdk.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import com.ninchat.sdk.NinchatSessionManager;

import java.util.Date;

public final class NinchatMessage {

    public enum Type {
        META,
        MESSAGE,
        WRITING,
        END,
        PADDING
    }

    private Type type;
    private String sender;
    private String message;
    private String fileId;
    private Date timestamp;
    private boolean isRemoteMessage = false;

    public NinchatMessage(final Type type) {
        this(type, null, null, null, System.currentTimeMillis(), false);
    }

    public NinchatMessage(final Type type, final String data) {
        this(type, type == Type.WRITING ? null : data, null, type == Type.WRITING ? data : null, System.currentTimeMillis(), true);
    }

    public NinchatMessage(final String message, final String fileId, final String sender, long timestamp, final boolean isRemoteMessage) {
        this(Type.MESSAGE, message, fileId, sender, timestamp, isRemoteMessage);
    }

    private NinchatMessage(final Type type, final String message, final String fileId, final String sender, long timestamp, final boolean isRemoteMessage) {
        this.type = type;
        this.message = message;
        this.fileId = fileId;
        this.sender = sender;
        this.timestamp = new Date(timestamp);
        this.isRemoteMessage = isRemoteMessage;
    }

    public Type getType() {
        return type;
    }

    public String getFileId() {
        return fileId;
    }

    public String getSender() {
        final NinchatUser member = NinchatSessionManager.getInstance().getMember(sender);
        return member == null ? NinchatSessionManager.getInstance().getUserName() : member.getName();
    }

    public String getSenderId() {
        return sender;
    }

    public Spanned getMessage() {
        return message == null ? null : Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(message);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isRemoteMessage() {
        return isRemoteMessage;
    }
}
