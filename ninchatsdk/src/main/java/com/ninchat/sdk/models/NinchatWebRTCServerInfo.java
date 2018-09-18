package com.ninchat.sdk.models;

public final class NinchatWebRTCServerInfo {

    private String url;
    private String username;
    private String credential;

    public NinchatWebRTCServerInfo(String url) {
        this(url, "", "");
    }

    public NinchatWebRTCServerInfo(String url, String username, String credential) {
        this.url = url;
        this.username = username;
        this.credential = credential;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getCredential() {
        return credential;
    }
}
