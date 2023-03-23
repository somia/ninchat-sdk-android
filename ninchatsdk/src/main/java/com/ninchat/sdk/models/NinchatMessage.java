package com.ninchat.sdk.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import com.ninchat.sdk.NinchatSessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class NinchatMessage {

    public enum Type {
        META,
        MESSAGE,
        WRITING,
        REMOVE_WRITING,
        END,
        REMOVE_END,
        PADDING,
        MULTICHOICE,
        CLEAR,
    }

    private Type type;
    private String sender;
    private String senderName;
    private String message;
    private String fileId;
    private Date timestamp;
    private Boolean deleted;
    private boolean isRemoteMessage = false;
    private JSONObject data;
    private List<NinchatOption> options;

    public NinchatMessage(final Type type, long timestamp) {
        this(type, null, null, null, null, timestamp, false, false);
    }

    public NinchatMessage(final Type type, final long timestamp, final boolean deleted) {
        this(type, null, null, null, null, timestamp, true, deleted);
    }

    public NinchatMessage(final Type type, final String data, long timestamp) {
        this(type, type == Type.WRITING ? null : data, null, type == Type.WRITING ? data : null, null, timestamp, true, false);
    }

    public NinchatMessage(final Type type, final String sender, final String senderName, final String label, final JSONObject data, final ArrayList<NinchatOption> options, long timestamp) {
        this(type, label, null, sender, senderName, timestamp, true, false);
        this.data = data;
        this.options = options;
    }

    public NinchatMessage(final String message, final String fileId, final String sender, final String senderName, long timestamp, final boolean isRemoteMessage) {
        this(Type.MESSAGE, message, fileId, sender, senderName, timestamp, isRemoteMessage, false);
    }

    private NinchatMessage(final Type type, final String message, final String fileId, final String sender, final String senderName, long timestamp, final boolean isRemoteMessage, final boolean deleted) {
        this.type = type;
        this.message = message;
        this.fileId = fileId;
        this.sender = sender;
        this.senderName = senderName;
        this.timestamp = new Date(timestamp);
        this.isRemoteMessage = isRemoteMessage;
        this.deleted = deleted;
    }

    public Type getType() {
        return type;
    }

    public String getFileId() {
        return fileId;
    }

    public String getSender(boolean isAgent) {
        final NinchatUser member = NinchatSessionManager.getInstance().getMember(sender);
        return member == null ? isAgent ? this.senderName :
                NinchatSessionManager.getInstance().getUserName() : member.getName();
    }

    public String getSenderId() {
        return sender;
    }

    public Boolean isDeleted() {
        return deleted;
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
