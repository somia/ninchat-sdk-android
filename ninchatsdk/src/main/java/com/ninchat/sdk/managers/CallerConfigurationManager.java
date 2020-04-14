package com.ninchat.sdk.managers;

import android.support.annotation.Nullable;

/**
 * A caller configuration manager
 * With this class we will modify caller configuration related attributes.
 * todo - An initial version. Add more functionality and remove me
 * The current implementation can change the caller user name attribute so that user can sdk user
 * can change user name during set up
 */
public class CallerConfigurationManager {
    @Nullable
    private String userName;

    private CallerConfigurationManager() {
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

        public Builder withUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public CallerConfigurationManager build() {
            CallerConfigurationManager configurationManager = new CallerConfigurationManager();
            configurationManager.setUserName(this.userName);
            return configurationManager;
        }
    }
}
