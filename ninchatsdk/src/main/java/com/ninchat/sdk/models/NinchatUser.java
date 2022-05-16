package com.ninchat.sdk.models;

import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatUser {

    private String displayName;
    private String realName;
    private String avatar;
    private boolean guest;
    private String jobTitle;
    private String channelId;

    public NinchatUser(final String displayName,
                       final String realName,
                       final String avatar,
                       final boolean guest,
                       final String jobTitle,
                       final String channelId) {
        this.displayName = displayName;
        this.realName = realName;
        this.avatar = avatar;
        this.guest = guest;
        this.jobTitle = jobTitle;
        this.channelId = channelId;
    }

    public String getName() {
        return displayName == null ? NinchatSessionManager.getInstance().getUserName() : displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public boolean isGuest() {
        return guest;
    }
    public String getChannelId() { return channelId; }
}
