package com.ninchat.sdk.models;

public class NinchatSessionCredentials {
    private String userId;
    private String userAuth;
    private String sessionId;

    public NinchatSessionCredentials(String userId, String userAuth, String sessionId) {
        this.userId = userId;
        this.userAuth = userAuth;
        this.sessionId = sessionId;
    }

    public String getUserAuth() {
        return userAuth;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }
}
