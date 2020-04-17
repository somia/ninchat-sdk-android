package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;

import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.sdk.models.NinchatSessionCredentials;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
public final class NinchatSession {

    public static final int NINCHAT_SESSION_REQUEST_CODE = NinchatSession.class.hashCode() & 0xffff;
    private NinchatSessionManager sessionManager;
    private String siteSecret = null;

    public static class Builder {
        private final Context context;
        private final String configurationKey;
        private NinchatSessionCredentials sessionCredentials;
        private NinchatConfiguration configuration;
        private String[] preferredEnvironments;
        private NinchatSDKEventListener eventListener;
        private NinchatSDKLogListener logListener;

        public Builder(Context context, String configurationKey) {
            this.context = context;
            this.configurationKey = configurationKey;
        }

        public NinchatSession build() {
            return new NinchatSession(this);
        }

        public Builder setSessionCredentials(NinchatSessionCredentials sessionCredentials) {
            this.sessionCredentials = sessionCredentials;
            return this;
        }

        public Builder setConfiguration(NinchatConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public Builder setPreferredEnvironments(String[] preferredEnvironments) {
            this.preferredEnvironments = preferredEnvironments;
            return this;
        }

        public Builder setEventListener(NinchatSDKEventListener eventListener) {
            this.eventListener = eventListener;
            return this;
        }

        public Builder setLogListener(NinchatSDKLogListener logListener) {
            this.logListener = logListener;
            return this;
        }
    }

    private NinchatSession(Builder builder) {
        Context context = builder.context;
        String configurationKey = builder.configurationKey;
        NinchatSessionCredentials sessionCredentials = builder.sessionCredentials;
        NinchatConfiguration configuration = builder.configuration;
        String[] preferredEnvironments = builder.preferredEnvironments;
        NinchatSDKEventListener eventListener = builder.eventListener;
        NinchatSDKLogListener logListener = builder.logListener;

        this.sessionManager = NinchatSessionManager.init(context, configurationKey, sessionCredentials, configuration, preferredEnvironments, eventListener, logListener);
    }

    public static final class Analytics {

        public static final class Keys {
            public static final String RATING = "rating";
        }

        public static final class Rating {
            public static final int GOOD = 1;
            public static final int FAIR = 0;
            public static final int POOR = -1;
            public static final int NO_ANSWER = -2;
        }

    }

    public static final class Broadcast {
        public static final String CONFIGURATION_FETCHED = BuildConfig.LIBRARY_PACKAGE_NAME + ".CONFIGURATION_FETCHED";
        public static final String SESSION_CREATED = BuildConfig.LIBRARY_PACKAGE_NAME + ".SESSION_CREATED";
        public static final String QUEUES_UPDATED = BuildConfig.LIBRARY_PACKAGE_NAME + ".QUEUES_UPDATED";
        public static final String START_FAILED = BuildConfig.LIBRARY_PACKAGE_NAME + ".START_FAILED";
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials) {
        this(applicationContext, configurationKey, sessionCredentials, null, null, null, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final String[] preferredEnvironments) {
        this(applicationContext, configurationKey, sessionCredentials, null, preferredEnvironments, null, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, null,null, eventListener, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final String[] preferredEnvironments, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, preferredEnvironments, eventListener, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, null, null, logListener);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final String[] preferredEnvironments, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, preferredEnvironments, null, logListener);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatSDKEventListener eventListener, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, null, eventListener, logListener);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, null, null, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final String[] preferredEnvironments) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, null, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration,null, eventListener, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, null, null, logListener);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final String[] preferredEnvironments, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, eventListener, null);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final String[] preferredEnvironments, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, null, logListener);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final NinchatSDKEventListener eventListener, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, null, eventListener, logListener);
    }

    @Deprecated
    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, @Nullable NinchatConfiguration configurationManager,
                          final String[] preferredEnvironments, final NinchatSDKEventListener eventListener, final NinchatSDKLogListener logListener) {
        this.sessionManager = NinchatSessionManager.init(applicationContext, configurationKey, sessionCredentials, configurationManager, preferredEnvironments, eventListener, logListener);
    }

    /**
     * Append information to the User-Agent string.  The string should be in
     * the form "app-name/version" or "app-name/version (more; details)".
     */
    public void setAppDetails(final String appDetails) {
        NinchatSessionManager.getInstance().setAppDetails(appDetails);
    }

    public void setServerAddress(final String serverAddress) {
        NinchatSessionManager.getInstance().setServerAddress(serverAddress);
    }

    public void setSiteSecret(final String siteSecret) {
        this.siteSecret = siteSecret;
    }

    public void setAudienceMetadata(final Props audienceMetadata) {
        sessionManager.setAudienceMetadata(audienceMetadata);
    }

    public Session getSession() {
        return sessionManager.getSession();
    }

    public void start(final Activity activity) {
        start(activity, null);
    }

    public void start(final Activity activity, final int requestCode) {
        start(activity, requestCode, null);
    }

    public void start(final Activity activity, final String queueId) {
        start(activity, NINCHAT_SESSION_REQUEST_CODE, queueId);
    }

    public void start(final Activity activity, final int requestCode, final String queueId) {
        sessionManager.start(activity, siteSecret, requestCode, queueId);
    }

    public void close() {
        sessionManager.close();
    }

}
