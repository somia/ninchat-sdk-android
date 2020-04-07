package com.ninchat.sdk.models;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import java.util.Date;

public final class NinchatFile {

    private String messageId;
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
    private long width;
    private long height;
    private boolean isDownloadableFile;

    private boolean isDownloaded = false;

    public NinchatFile(final String messageId, final String fileId, final String name, int size, final String type, final long timestamp, final String sender, final boolean isRemote) {
        this.messageId = messageId;
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

    public void setWidth(long width) {
        this.width = width;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public void setDownloaded() {
        isDownloaded = true;
    }

    public boolean isVideo() {
        return type.startsWith("video/");
    }

    public boolean isDownloadableFile() {
        return this.width == -1 || this.height == -1;
    }

    public void setDownloadableFile(boolean downloadableFile) {
        isDownloadableFile = downloadableFile;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public String getMessageId() {
        return messageId;
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

    public int getWidth() {
        return (int) width;
    }

    public int getHeight() {
        return (int) height;
    }

    private String getFileSize() {
        if (size / 1024 == 0) {
            return size + "B";
        }
        int kiloBytes = size / 1024;
        if (kiloBytes / 1024 == 0) {
            return kiloBytes + "kB";
        }
        int megaBytes = kiloBytes / 1024;
        // TODO: Should we support gigabytes and terabytes too?
        return megaBytes + "MB";
    }

    public Spanned getFileLink() {
        final String link = "<a href='" + url + "'>" + name + "</a> (" + getFileSize() + ")";
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(link, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(link);
    }

    public Date getUrlExpiry() {
        return urlExpiry;
    }
}
