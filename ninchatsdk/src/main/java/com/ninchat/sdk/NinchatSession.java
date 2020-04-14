package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;

import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.sdk.managers.NinchatConfiguration;
import com.ninchat.sdk.models.NinchatSessionCredentials;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
public final class NinchatSession {

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

    public static final int NINCHAT_SESSION_REQUEST_CODE = NinchatSession.class.hashCode() & 0xffff;

    private NinchatSessionManager sessionManager;
    private String siteSecret = null;

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials) {
        this(applicationContext, configurationKey, sessionCredentials, null, null, null, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final String[] preferredEnvironments) {
        this(applicationContext, configurationKey, sessionCredentials, null, preferredEnvironments, null, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, null,null, eventListener, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final String[] preferredEnvironments, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, preferredEnvironments, eventListener, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, null, null, logListener);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final String[] preferredEnvironments, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, preferredEnvironments, null, logListener);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatSDKEventListener eventListener, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, null, null, eventListener, logListener);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, null, null, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final String[] preferredEnvironments) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, null, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration,null, eventListener, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, null, null, logListener);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final String[] preferredEnvironments, final NinchatSDKEventListener eventListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, eventListener, null);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final String[] preferredEnvironments, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, preferredEnvironments, null, logListener);
    }

    public NinchatSession(final Context applicationContext, final String configurationKey, @Nullable NinchatSessionCredentials sessionCredentials, final NinchatConfiguration ninchatConfiguration, final NinchatSDKEventListener eventListener, final NinchatSDKLogListener logListener) {
        this(applicationContext, configurationKey, sessionCredentials, ninchatConfiguration, null, eventListener, logListener);
    }


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
