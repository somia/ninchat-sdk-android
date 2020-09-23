package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ninchat.client.Payload;
import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.sdk.activities.NinchatActivity;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.adapters.NinchatQueueListAdapter;
import com.ninchat.sdk.events.OnPostAudienceQuestionnaire;
import com.ninchat.sdk.helper.sessionmanager.SessionManagerHelper;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatOption;
import com.ninchat.sdk.models.NinchatQueue;
import com.ninchat.sdk.models.NinchatSessionCredentials;
import com.ninchat.sdk.models.NinchatUser;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder;
import com.ninchat.sdk.networkdispatchers.NinchatDescribeFile;
import com.ninchat.sdk.networkdispatchers.NinchatFetchConfiguration;
import com.ninchat.sdk.networkdispatchers.NinchatOpenSession;
import com.ninchat.sdk.session.NinchatSessionHolder;
import com.ninchat.sdk.states.NinchatState;
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes;
import com.ninchat.sdk.utils.misc.Broadcast;
import com.ninchat.sdk.utils.misc.Misc;
import com.ninchat.sdk.utils.misc.Parameter;
import com.ninchat.sdk.utils.propsvisitor.NinchatPropVisitor;
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.ninchat.sdk.helper.propsparser.NinchatPropsParser.*;
import static com.ninchat.sdk.utils.misc.Misc.guessMimeTypeFromFileName;

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
    private NinchatSessionCredentials sessionCredentials;
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
        this.messageAdapter = new NinchatMessageAdapter();
        this.ninchatQueueListAdapter = null;
        this.activityWeakReference = new WeakReference(null);
        this.sessionCredentials = sessionCredentials;
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
                }
        );
    }

    public NinchatMessageAdapter getMessageAdapter() {
        return messageAdapter;
    }

    public void setConfiguration(final String config) {
        Log.v(TAG, "Got configuration: " + config);
        ninchatState.setNinchatQuestionnaire(new NinchatQuestionnaireHolder(this));
        ninchatState.getSiteConfig().setConfigString(config, ninchatState.getPreferredEnvironments());
        Log.i(TAG, "Configuration fetched successfully!");
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(NinchatSession.Broadcast.CONFIGURATION_FETCHED));
        }
        String userAuth = null;
        String userId = null;
        if (sessionCredentials != null) {
            userId = sessionCredentials.getUserId();
            userAuth = sessionCredentials.getUserAuth();
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
            ninchatQueueListAdapter = new NinchatQueueListAdapter(activity, ninchatState.getQueues());
        }
        return ninchatQueueListAdapter;
    }

    public boolean hasQueues() {
        return ninchatState.getQueues().size() > 0;
    }

    public NinchatUser getMember(final String userId) {
        if (ninchatState.getUserId() != null && ninchatState.getUserId().equals(userId)) {
            return new NinchatUser(getUserName(), getUserName(), null, true);
        }
        return ninchatState.getMember(userId);
    }

    public boolean isGuestMemeber() {
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
        for (NinchatQueue queue : ninchatState.getQueues()) {
            if (queue.getId().equals(queueId)) {
                return queue;
            }
        }
        return null;
    }

    public void queueUpdated(final Props params) {
        SessionManagerHelper.parseQueue(params);
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.QUEUE_UPDATED));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSession.Broadcast.QUEUES_UPDATED));
        }
    }

    public void audienceEnqueued(final Props params) {
        ninchatState.setCurrentSessionState(Misc.NEW_SESSION);
        final String queueId = SessionManagerHelper.parseQueue(params);
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
            ninchatSessionHolder.getCurrentSession().send(load, null);
        } catch (final Exception e) {
            // Ignore
        }
    }

    public void messageReceived(final Props params, final Payload payload) {
        if (ninchatState.getChannelId() == null) {
            return;
        }

        long currentActionId = 0;
        String messageType;
        String sender;
        String messageId;
        long timestampMs;

        try {
            currentActionId = params.getInt("action_id");
            messageType = params.getString("message_type");
            sender = params.getString("message_user_id");
            messageId = params.getString("message_id");
            double timestampParam = params.getFloat("message_time");
            timestampMs = (Double.valueOf(timestampParam).longValue()) * 1000;
        } catch (final Exception e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        if (NinchatMessageTypes.webrtcMessage(messageType) && !sender.equals(ninchatState.getUserId())) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < payload.length(); ++i) {
                builder.append(new String(payload.get(i)));
            }
            final Context context = contextWeakReference.get();
            if (context == null) {
                return;
            }
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(Broadcast.WEBRTC_MESSAGE)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_ID, messageId)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_TYPE, messageType)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_SENDER, sender)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_CONTENT, builder.toString()));
        }
        if (NinchatMessageTypes.UI_COMPOSE.equals(messageType)) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < payload.length(); ++i) {
                builder.append(new String(payload.get(i)));
            }
            try {
                final JSONArray messages = new JSONArray(builder.toString());
                List<NinchatOption> messageOptions = new ArrayList<>();
                boolean simpleButtonChoice = false;
                for (int j = 0; j < messages.length(); ++j) {
                    final JSONObject message = messages.getJSONObject(j);
                    final JSONArray options = message.optJSONArray("options");
                    if (options != null) {
                        messageOptions = new ArrayList<>();
                        for (int k = 0; k < options.length(); ++k) {
                            messageOptions.add(new NinchatOption(options.getJSONObject(k)));
                        }
                        messageAdapter.add(messageId, new NinchatMessage(NinchatMessage.Type.MULTICHOICE, sender, message.getString("label"), message, messageOptions, timestampMs));
                    } else {
                        simpleButtonChoice = true;
                        messageOptions.add(new NinchatOption(message));
                    }
                }
                if (simpleButtonChoice) {
                    messageAdapter.add(messageId, new NinchatMessage(NinchatMessage.Type.MULTICHOICE, sender, null, null, messageOptions, timestampMs));
                }
            } catch (final JSONException e) {
                // Ignore message
            }
        }

        if (ninchatState.getActionId() == currentActionId) {
            EventBus.getDefault().post(new OnPostAudienceQuestionnaire());
        }

        if (!messageType.equals(NinchatMessageTypes.TEXT) && !messageType.equals(NinchatMessageTypes.FILE)) {
            return;
        }

        for (int i = 0; i < payload.length(); ++i) {
            try {
                final JSONObject message = new JSONObject(new String(payload.get(i)));
                final JSONArray files = message.optJSONArray("files");
                if (files != null) {
                    final JSONObject file = files.getJSONObject(0);
                    final String filename = file.getJSONObject("file_attrs").getString("name");
                    final int filesize = file.getJSONObject("file_attrs").getInt("size");
                    String filetype = file.getJSONObject("file_attrs").getString("type");
                    if (filetype == null || filetype.equals("application/octet-stream")) {
                        filetype = guessMimeTypeFromFileName(filename);
                    }
                    if (filetype != null) {
                        final String fileId = file.getString("file_id");
                        final NinchatFile ninchatFile = new NinchatFile(messageId, fileId, filename, filesize, filetype, timestampMs, sender, !sender.equals(ninchatState.getUserId()));
                        ninchatState.addFile(fileId, ninchatFile);
                        if (ninchatFile.getUrl() == null ||
                                ninchatFile.getUrlExpiry() == null ||
                                ninchatFile.getUrlExpiry().before(new Date())) {

                            NinchatDescribeFile.executeAsync(
                                    NinchatScopeHandler.getIOScope(),
                                    getSession(),
                                    fileId,
                                    aLong -> null
                            );
                        }
                    }
                } else {
                    messageAdapter.add(messageId, new NinchatMessage(message.getString("text"), null, sender, timestampMs, !sender.equals(ninchatState.getUserId())));
                }
            } catch (final JSONException e) {
                // Ignore
            }
        }
    }

    public String getUserName() {
        if (this.ninchatConfiguration != null && this.ninchatConfiguration.getUserName() != null) {
            return this.ninchatConfiguration.getUserName();
        }
        return ninchatState.getSiteConfig().getUserName();
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
        SessionManagerHelper.iceBegun(props);
    }

    public String getChatStarted() {
        final NinchatQueue queue = getQueue(ninchatState.getQueueId());
        String name = "";
        if (queue != null) {
            name = queue.getName();
        }
        return Misc.center(
                ninchatState.getSiteConfig().getChatStarted(name));
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
