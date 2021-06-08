package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.ninchatqueuelist.view.NinchatQueueListAdapter;
import com.ninchat.sdk.helper.session.NinchatSessionManagerHelper;
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue;
import com.ninchat.sdk.models.NinchatSessionCredentials;
import com.ninchat.sdk.models.NinchatUser;
import com.ninchat.sdk.networkdispatchers.NinchatFetchConfiguration;
import com.ninchat.sdk.networkdispatchers.NinchatOpenSession;
import com.ninchat.sdk.helper.session.NinchatSessionHolder;
import com.ninchat.sdk.states.NinchatState;
import com.ninchat.sdk.utils.misc.Broadcast;
import com.ninchat.sdk.utils.misc.Misc;
import com.ninchat.sdk.utils.misc.Parameter;
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler;


import java.lang.ref.WeakReference;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager {
    protected static NinchatSessionManager instance;
    protected static final String TAG = NinchatSessionManager.class.getSimpleName();
    public WeakReference<Context> contextWeakReference;
    public WeakReference<Activity> activityWeakReference;
    public WeakReference<NinchatSDKEventListener> eventListenerWeakReference;
    public WeakReference<NinchatSDKLogListener> logListenerWeakReference;

    public NinchatQueueListAdapter ninchatQueueListAdapter;
    protected NinchatMessageAdapter messageAdapter;

    @Nullable
    private NinchatConfiguration ninchatConfiguration;


    static NinchatSessionManager init(final Context context,
                                      final String configurationKey,
                                      @Nullable NinchatSessionCredentials sessionCredentials,
                                      @Nullable final NinchatConfiguration configurationManager,
                                      final String[] preferredEnvironments,
                                      final NinchatSDKEventListener eventListener,
                                      final NinchatSDKLogListener logListener) {
        instance = new NinchatSessionManager(context, configurationKey, sessionCredentials, configurationManager, preferredEnvironments, eventListener, logListener);
        return instance;
    }


    public static NinchatSessionManager getInstance() {
        return instance;
    }

    public Context getContext() {
        return contextWeakReference.get();
    }

    public NinchatState ninchatState = new NinchatState();
    public NinchatSessionHolder ninchatSessionHolder = new NinchatSessionHolder(ninchatState);

    protected NinchatSessionManager(final Context context,
                                    final String configurationKey,
                                    @Nullable NinchatSessionCredentials sessionCredentials,
                                    @Nullable NinchatConfiguration configurationManager,
                                    final String[] preferredEnvironments,
                                    final NinchatSDKEventListener eventListener,
                                    final NinchatSDKLogListener logListener) {
        this.contextWeakReference = new WeakReference<>(context);
        this.eventListenerWeakReference = new WeakReference<>(eventListener);
        this.logListenerWeakReference = new WeakReference<>(logListener);

        ninchatState.setConfigurationKey(configurationKey);
        ninchatState.setPreferredEnvironments(preferredEnvironments);
        ninchatState.setSessionCredentials(sessionCredentials);
        this.messageAdapter = new NinchatMessageAdapter();
        this.ninchatQueueListAdapter = null;
        this.activityWeakReference = new WeakReference(null);
        this.ninchatConfiguration = configurationManager;
    }


    public void start(final Activity activity, final String siteSecret, final int requestCode, final String queueId) {
        this.activityWeakReference = new WeakReference<>(activity);
        ninchatState.setSiteSecret(siteSecret);
        ninchatState.setQueueId(queueId);
        ninchatState.setRequestCode(requestCode);

        NinchatFetchConfiguration.executeAsync(
                NinchatScopeHandler.getIOScope(),
                ninchatState.getServerAddress(),
                ninchatState.getConfigurationKey(),
                s -> {
                    setConfiguration(s);
                    return null;
                },
                e -> {
                    sessionError(e);
                    return null;
                }
        );
    }

    public NinchatMessageAdapter getMessageAdapter() {
        if (messageAdapter == null) reInitializeMessageAdapter(this);
        return messageAdapter;
    }

    public void reInitializeMessageAdapter(NinchatSessionManager currentSession) {
        messageAdapter = new NinchatMessageAdapter();
        messageAdapter.addMetaMessage("", currentSession.getChatStarted());
    }

    public void setConfiguration(final String config) {
        Log.v(TAG, "Got configuration: " + config);
        ninchatState.getSiteConfig().setConfigString(config, ninchatState.getPreferredEnvironments());
        Log.i(TAG, "Configuration fetched successfully!");
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(NinchatSession.Broadcast.CONFIGURATION_FETCHED));
        }
        String userAuth = null;
        String userId = null;
        if (ninchatState.getSessionCredentials() != null) {
            userId = ninchatState.getSessionCredentials().getUserId();
            userAuth = ninchatState.getSessionCredentials().getUserAuth();
        }
        NinchatOpenSession.executeAsync(
                NinchatScopeHandler.getIOScope(),
                ninchatState.getSiteSecret(),
                getUserName(),
                userId,
                userAuth,
                ninchatState.userAgent(),
                ninchatState.getServerAddress(),
                currentSession -> {
                    ninchatSessionHolder.onNewSession(currentSession,
                            ninchatState.getSiteConfig(),
                            eventListenerWeakReference.get());
                    final Context mContent = contextWeakReference.get();
                    if (mContent != null) {
                        LocalBroadcastManager.getInstance(mContent).sendBroadcast(new Intent(NinchatSession.Broadcast.SESSION_CREATED));
                    }
                    return null;
                }
        );
    }

    public void sessionError(final Exception error) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final NinchatSDKEventListener listener = eventListenerWeakReference.get();
                if (listener != null) {
                    listener.onSessionError(error);
                    if (ninchatSessionHolder.getCurrentSession() == null) {
                        listener.onSessionInitFailed();
                    }
                }
            }
        });
    }

    public Session getSession() {
        return ninchatSessionHolder.getCurrentSession();
    }

    public NinchatQueueListAdapter getNinchatQueueListAdapter(final Activity activity) {
        if (ninchatQueueListAdapter == null) {
            ninchatQueueListAdapter = new NinchatQueueListAdapter(activity, ninchatState.getQueueList());
        }
        return ninchatQueueListAdapter;
    }


    public NinchatUser getMember(final String userId) {
        if (ninchatState.getUserId() != null && ninchatState.getUserId().equals(userId)) {
            return new NinchatUser(getUserName(), getUserName(), null, true, "");
        }
        return ninchatState.getMember(userId);
    }

    public boolean isGuestMember() {
        final NinchatUser currentUser = ninchatState.getMember(ninchatState.getUserId());
        if (currentUser == null) {
            return false;
        }
        return currentUser.isGuest();
    }

    public void sendQueueParsingError() {
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(NinchatSession.Broadcast.START_FAILED));
        }
        final NinchatSDKEventListener listener = eventListenerWeakReference.get();
        if (listener != null) {
            listener.onSessionInitFailed();
        }
    }


    public NinchatQueue getQueue(final String queueId) {
        for (NinchatQueue queue : ninchatState.getQueueList()) {
            if (queue.getId().equals(queueId)) {
                return queue;
            }
        }
        // queue not found from queue list
        return null;
    }

    public void queueUpdated(final Props params) {
        NinchatSessionManagerHelper.parseQueue(params);
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.QUEUE_UPDATED));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSession.Broadcast.QUEUES_UPDATED));
        }
    }

    public void audienceEnqueued(final Props params) {
        ninchatState.setCurrentSessionState(Misc.NEW_SESSION);
        final String queueId = NinchatSessionManagerHelper.parseQueue(params);
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.AUDIENCE_ENQUEUED).putExtra(Parameter.QUEUE_ID, queueId));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.QUEUE_UPDATED));
        }
    }

    public void audienceEnqueued(final String queueId) {
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.AUDIENCE_ENQUEUED).putExtra(Parameter.QUEUE_ID, queueId));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.QUEUE_UPDATED));
        }
    }

    public void loadChannelHistory(final String messageId) {
        final Props load = new Props();
        load.setString("action", "load_history");
        load.setString("channel_id", ninchatState.getChannelId());
        load.setInt("history_order", 1);

        if (messageId != null && !messageId.isEmpty()) {
            load.setString("message_id", messageId);
        }
        try {
            if (ninchatSessionHolder.getCurrentSession() != null)
                ninchatSessionHolder.getCurrentSession().send(load, null);
        } catch (final Exception e) {
            // Ignore
        }
    }

    public String getUserName() {
        if (this.ninchatConfiguration != null && this.ninchatConfiguration.getUserName() != null) {
            return this.ninchatConfiguration.getUserName();
        }
        return ninchatState.getSiteConfig().getUserName();
    }

    public int getNinchatChatBackground() {
        if (this.ninchatConfiguration != null && this.ninchatConfiguration.getNinchatChatBackground() != -1) {
            return this.ninchatConfiguration.getNinchatChatBackground();
        }
        return R.drawable.ninchat_chat_background;
    }

    // Get username or agentname if it is set in configuration
    public String getName(boolean isAgent) {
        if (!isAgent) return getUserName();
        try {
            return ninchatState.getSiteConfig().getAgentName();
        } catch (final Exception e) {
            return null;
        }
    }

    public void iceBegun(Props props) {
        NinchatSessionManagerHelper.iceBegun(props);
    }

    public String getChatStarted() {
        final NinchatQueue queue = getQueue(ninchatState.getQueueId());
        String name = "";
        if (queue != null) {
            name = queue.getName();
        }
        return Misc.center(ninchatState.getSiteConfig().getChatStarted(name));
    }

    public void close() {
        if (ninchatSessionHolder != null) {
            ninchatSessionHolder.dispose();
        }
        if (ninchatState != null) {
            ninchatState.dispose();
        }
        if (messageAdapter != null) {
            messageAdapter.clear();
        }
    }

}
