package com.ninchat.sdk.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import com.ninchat.sdk.NinchatSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

public final class NinchatMessage {

    public enum Type {
        META,
        MESSAGE,
        WRITING,
        END,
        PADDING,
        MULTICHOICE
    }

    private Type type;
    private String sender;
    private String message;
    private String fileId;
    private Date timestamp;
    private boolean isRemoteMessage = false;
    private JSONObject data;
    private List<NinchatOption> options;

    public NinchatMessage(final Type type) {
        this(type, null, null, null, System.currentTimeMillis(), false);
    }

    public NinchatMessage(final Type type, final String data) {
        this(type, type == Type.WRITING ? null : data, null, type == Type.WRITING ? data : null, System.currentTimeMillis(), true);
    }

    public NinchatMessage(final Type type, final String sender, final String label, final JSONObject data, final List<NinchatOption> options) {
        this(type, label, null, sender, System.currentTimeMillis(), true);
        this.data = data;
        this.options = options;
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

    public List<NinchatOption> getOptions() {
        return options;
    }

    public void toggleOption(final int position) {
        final NinchatOption option = options.get(position);
        option.toggle();
    }

    public JSONObject getMultiChoiceData() throws JSONException {
        final JSONArray options = new JSONArray();
        for (final NinchatOption option : this.options) {
            options.put(option.toJSON());
        }
        return this.data.put("options", options);
    }
}
