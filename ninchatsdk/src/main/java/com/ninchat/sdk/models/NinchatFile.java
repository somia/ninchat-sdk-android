package com.ninchat.sdk.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.util.Date;

public final class NinchatFile {

    private String fileId;
    private String name;
    private int size;
    private String type;
    private long timestamp;
    private String sender;
    private boolean isRemote;
    private String url;
    private Date urlExpiry;
    private float aspectRatio;

    private boolean isDownloaded = false;

    public NinchatFile(final String fileId, final String name, int size, final String type, final long timestamp, final String sender, final boolean isRemote) {
        this.fileId = fileId;
        this.name = name;
        this.size = size;
        this.type = type;
        this.timestamp = timestamp;
        this.sender = sender;
        this.isRemote = isRemote;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUrlExpiry(Date urlExpiry) {
        this.urlExpiry = urlExpiry;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public void setDownloaded() {
        isDownloaded = true;
    }

    public boolean isVideo() {
        return type.startsWith("video/");
    }

    public boolean isPDF() {
        return type.equals("application/pdf");
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public String getId() {
        return fileId;
    }

    public String getName() {
        return name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public String getUrl() {
        return url;
    }

    private String getFileSize() {
        if (size % 1024 == 0) {
            return size + "B";
        }
        int kiloBytes = size % 1024;
        if (kiloBytes % 1024 == 0) {
            return kiloBytes + "kB";
        }
        int megaBytes = kiloBytes % 1024;
        // TODO: Should we support gigabytes and terabytes too?
        return megaBytes + "MB";
    }

    public Spanned getPDFLInk() {
        final String link = "<a href='" + url + "'>" + name + "</a> (" + getFileSize() + ")";
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(link, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(link);
    }

    public Date getUrlExpiry() {
        return urlExpiry;
    }
}
