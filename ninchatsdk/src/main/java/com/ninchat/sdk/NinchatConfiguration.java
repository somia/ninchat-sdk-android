package com.ninchat.sdk;

import androidx.annotation.Nullable;

/**
 * Ninchat configuration
 * With this class we will modify configuration related attributes.
 * todo - An initial version. Add more functionality and remove me
 * The current implementation can change the caller user name attribute so that user can sdk user
 * can change user name during set up
 */
public class NinchatConfiguration {
    @Nullable
    private String userName;

    private NinchatConfiguration() {
        // pass
    }

    private void setUserName(@Nullable String userName) {
        this.userName = userName;
    }

    @Nullable
    public String getUserName() {
        return userName;
    }

    public static class Builder {
        @Nullable
        private String userName;

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public NinchatConfiguration create() {
            NinchatConfiguration configurationManager = new NinchatConfiguration();
            configurationManager.setUserName(this.userName);
            return configurationManager;
        }
    }
}
