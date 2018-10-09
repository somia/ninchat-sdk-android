package com.ninchat.sdk.models;

import java.util.Date;

public final class NinchatFile {

    private String fileId;
    private String name;
    private String type;
    private long timestamp;
    private String sender;
    private boolean isRemote;
    private String url;
    private Date urlExpiry;
    private float aspectRatio;

    private boolean isDownloaded = false;

    public NinchatFile(final String fileId, final String name, final String type, final long timestamp, final String sender, final boolean isRemote) {
        this.fileId = fileId;
        this.name = name;
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

    public boolean isImage() {
        return type.startsWith("image/");
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

    public Date getUrlExpiry() {
        return urlExpiry;
    }
}
