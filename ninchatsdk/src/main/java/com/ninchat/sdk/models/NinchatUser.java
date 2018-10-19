package com.ninchat.sdk.models;

import com.ninchat.sdk.NinchatSessionManager;

public final class NinchatUser {

    private String displayName;
    private String realName;
    private String avatar;
    private boolean guest;

    public NinchatUser(final String displayName, final String realName, final String avatar, final boolean guest) {
        this.displayName = displayName;
        this.realName = realName;
        this.avatar = avatar;
        this.guest = guest;
    }

    public String getName() {
        return displayName == null ? NinchatSessionManager.getInstance().getUserName() : displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public boolean isGuest() {
        return guest;
    }
}
