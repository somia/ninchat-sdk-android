package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.ninchat.client.CloseHandler;
import com.ninchat.client.ConnStateHandler;
import com.ninchat.client.EventHandler;
import com.ninchat.client.LogHandler;
import com.ninchat.client.Objects;
import com.ninchat.client.Payload;
import com.ninchat.client.PropVisitor;
import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.client.SessionEventHandler;
import com.ninchat.client.Strings;
import com.ninchat.sdk.activities.NinchatActivity;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.adapters.NinchatQueueListAdapter;
import com.ninchat.sdk.events.OnAudienceRegistered;
import com.ninchat.sdk.events.OnPostAudienceQuestionnaire;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatOption;
import com.ninchat.sdk.models.NinchatQueue;
import com.ninchat.sdk.models.NinchatSessionCredentials;
import com.ninchat.sdk.models.NinchatUser;
import com.ninchat.sdk.models.NinchatWebRTCServerInfo;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder;
import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;
import com.ninchat.sdk.tasks.NinchatDeleteUserTask;
import com.ninchat.sdk.tasks.NinchatDescribeFileTask;
import com.ninchat.sdk.tasks.NinchatDescribeChannelTask;
import com.ninchat.sdk.tasks.NinchatJoinQueueTask;
import com.ninchat.sdk.tasks.NinchatListQueuesTask;
import com.ninchat.sdk.tasks.NinchatOpenSessionTask;
import com.ninchat.sdk.tasks.NinchatPartChannelTask;
import com.ninchat.sdk.tasks.NinchatSendBeginIceTask;
import com.ninchat.sdk.tasks.NinchatSendFileTask;
import com.ninchat.sdk.tasks.NinchatSendIsWritingTask;
import com.ninchat.sdk.tasks.NinchatSendMessageTask;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.HAS_CHANNEL;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.IN_QUEUE;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.NEW_SESSION;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager {

    public static final class Broadcast {
        public static final String QUEUE_UPDATED = BuildConfig.LIBRARY_PACKAGE_NAME + ".queueUpdated";
        public static final String AUDIENCE_ENQUEUED = BuildConfig.LIBRARY_PACKAGE_NAME + ".audienceEnqueued";
        public static final String CHANNEL_JOINED = BuildConfig.LIBRARY_PACKAGE_NAME + ".channelJoined";
        public static final String CHANNEL_CLOSED = BuildConfig.LIBRARY_PACKAGE_NAME + ".channelClosed";
        public static final String WEBRTC_MESSAGE = BuildConfig.LIBRARY_PACKAGE_NAME + ".webRTCMessage";
        public static final String WEBRTC_MESSAGE_ID = WEBRTC_MESSAGE + ".messageId";
        public static final String WEBRTC_MESSAGE_SENDER = WEBRTC_MESSAGE + ".sender";
        public static final String WEBRTC_MESSAGE_TYPE = WEBRTC_MESSAGE + ".type";
        public static final String WEBRTC_MESSAGE_CONTENT = WEBRTC_MESSAGE + ".content";
    }

    public static final class Parameter {
        public static final String QUEUE_ID = "queueId";
        public static final String CHAT_IS_CLOSED = "isClosed";
    }

    public static final class MessageTypes {
        public static final String TEXT = "ninchat.com/text";
        public static final String FILE = "ninchat.com/file";
        public static final String UI_COMPOSE = "ninchat.com/ui/compose";
        public static final String UI_ACTION = "ninchat.com/ui/action";
        public static final String WEBRTC_PREFIX = "ninchat.com/rtc/";
        public static final String ICE_CANDIDATE = WEBRTC_PREFIX + "ice-candidate";
        public static final String ANSWER = WEBRTC_PREFIX + "answer";
        public static final String OFFER = WEBRTC_PREFIX + "offer";
        public static final String CALL = WEBRTC_PREFIX + "call";
        public static final String PICK_UP = WEBRTC_PREFIX + "pick-up";
        public static final String HANG_UP = WEBRTC_PREFIX + "hang-up";
        public static final String WEBRTC_SERVERS_PARSED = WEBRTC_PREFIX + "serversParsed";
        public static final String RATING_OR_POST_ANSWERS = "ninchat.com/metadata";

        static final List<String> WEBRTC_MESSAGE_TYPES = new ArrayList<>();

        static {
            WEBRTC_MESSAGE_TYPES.add(ICE_CANDIDATE);
            WEBRTC_MESSAGE_TYPES.add(ANSWER);
            WEBRTC_MESSAGE_TYPES.add(OFFER);
            WEBRTC_MESSAGE_TYPES.add(CALL);
            WEBRTC_MESSAGE_TYPES.add(PICK_UP);
            WEBRTC_MESSAGE_TYPES.add(HANG_UP);
        }
    }

    public static final String DEFAULT_USER_AGENT = "ninchat-sdk-android/" + BuildConfig.VERSION_NAME + " (Android " + Build.VERSION.RELEASE + "; " + Build.MANUFACTURER + " " + Build.MODEL + ")";

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

    private String appDetails = null;
    private String serverAddress = null;
    private long actionId = -1;

    public void setAppDetails(final String appDetails) {
        this.appDetails = appDetails;
    }

    public String getAppDetails() {
        return appDetails;
    }

    public String getUserAgent() {
        String s = DEFAULT_USER_AGENT;
        if (appDetails != null) {
            s += " " + appDetails;
        }
        return s;
    }

    public void setServerAddress(final String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String getServerAddress() {
        return serverAddress != null ? serverAddress : "api.ninchat.com";
    }

    protected Props audienceMetadata;
    protected Props userChannels;

    public void setAudienceMetadata(final Props audienceMetadata) {
        this.audienceMetadata = audienceMetadata;
    }

    public Props getAudienceMetadata() {
        return audienceMetadata;
    }

    public static NinchatSessionManager getInstance() {
        return instance;
    }

    public Context getContext() {
        return contextWeakReference.get();
    }

    public NinchatFile getFile(final String fileId) {
        return files.get(fileId);
    }

    public static void joinQueue(final String queueId) {
        instance.queueId = queueId;
        // if there is already audience queue or user is in the queue in the user session
        if (instance.isResumedSession()) {
            instance.audienceEnqueued(queueId);
            if (instance.hasChannel()) {
                final String currentChannelId = instance.parseChannelId(instance.userChannels);
                NinchatDescribeChannelTask.start(currentChannelId, instance.requestCallback);
                return;
            }
            if (instance.isInQueue()) {
                return;
            }
        }
        NinchatJoinQueueTask.start(queueId);
    }

    public static void exitQueue() {
        NinchatDeleteUserTask.start();
    }

    public void partChannel() {
        NinchatPartChannelTask.start(channelId);
    }

    protected static NinchatSessionManager instance;
    protected static final String TAG = NinchatSessionManager.class.getSimpleName();
    protected String configurationKey;
    private String[] preferredEnvironments;
    protected String siteSecret;

    protected NinchatSessionManager(final Context context,
                                    final String configurationKey,
                                    @Nullable NinchatSessionCredentials sessionCredentials,
                                    @Nullable NinchatConfiguration configurationManager,
                                    final String[] preferredEnvironments,
                                    final NinchatSDKEventListener eventListener,
                                    final NinchatSDKLogListener logListener) {
        this.contextWeakReference = new WeakReference<>(context);
        this.configurationKey = configurationKey;
        this.preferredEnvironments = preferredEnvironments;
        this.eventListenerWeakReference = new WeakReference<>(eventListener);
        this.logListenerWeakReference = new WeakReference<>(logListener);
        this.configuration = null;
        this.session = null;
        this.queues = new ArrayList<>();
        this.messageAdapter = null;
        this.members = new HashMap<>();
        this.ninchatQueueListAdapter = null;
        this.files = new HashMap<>();
        this.activityWeakReference = new WeakReference<>(null);
        this.sessionCredentials = sessionCredentials;
        this.resumedSession = NEW_SESSION;
        this.ninchatConfiguration = configurationManager;
    }

    protected WeakReference<Context> contextWeakReference;
    protected WeakReference<NinchatSDKEventListener> eventListenerWeakReference;
    protected WeakReference<NinchatSDKLogListener> logListenerWeakReference;

    protected JSONObject configuration;
    protected NinchatQuestionnaireHolder ninchatQuestionnaireHolder;
    protected Session session;
    protected WeakReference<Activity> activityWeakReference;
    protected int requestCode;
    protected String queueId;
    protected List<NinchatQueue> queues;
    protected NinchatQueueListAdapter ninchatQueueListAdapter;
    protected String channelId;
    protected String userId;
    protected Map<String, NinchatUser> members;
    protected NinchatMessageAdapter messageAdapter;
    protected List<NinchatWebRTCServerInfo> stunServers;
    protected List<NinchatWebRTCServerInfo> turnServers;
    protected Map<String, NinchatFile> files;

    @Nullable
    private NinchatSessionCredentials sessionCredentials;
    private int resumedSession;
    @Nullable
    private NinchatConfiguration ninchatConfiguration;

    public void start(final Activity activity, final String siteSecret, final int requestCode, final String queueId) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.siteSecret = siteSecret;
        this.requestCode = requestCode;
        this.queueId = queueId;
        NinchatConfigurationFetchTask.start(configurationKey);
    }

    public NinchatMessageAdapter getMessageAdapter() {
        return messageAdapter;
    }

    public void setConfiguration(final String config) throws JSONException {
        try {
            Log.v(TAG, "Got configuration: " + config);
            this.configuration = new JSONObject(config);
            this.ninchatQuestionnaireHolder = new NinchatQuestionnaireHolder(this.getDefault());
            Log.i(TAG, "Configuration fetched successfully!");
        } catch (final JSONException e) {
            this.configuration = null;
            throw e;
        }
        final Context context = contextWeakReference.get();
        if (context != null && configuration != null) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(NinchatSession.Broadcast.CONFIGURATION_FETCHED));
        }
        NinchatOpenSessionTask.start(siteSecret, sessionCredentials);
    }

    public void sessionError(final Exception error) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final NinchatSDKEventListener listener = eventListenerWeakReference.get();
                if (listener != null) {
                    listener.onSessionError(error);
                    if (configuration == null || session == null) {
                        listener.onSessionInitFailed();
                    }
                }
            }
        });
    }

    public void setSession(final Session session) {
        this.session = session;
        final NinchatSDKEventListener listener = eventListenerWeakReference.get();

        this.session.setOnSessionEvent(new SessionEventHandler() {
            @Override
            public void onSessionEvent(Props params) {
                try {
                    Log.v(TAG, "onSessionEvent: " + params.string());
                    final String event = params.getString("event");

                    if (event.equals("session_created")) {
                        userId = params.getString("user_id");
                        userChannels = params.getObject("user_channels");
                        String userAuth = sessionCredentials != null ? sessionCredentials.getUserAuth() : params.getString("user_auth");
                        // a resumed session if session credentials were used
                        resumedSession = 1 << (hasUserChannel(userChannels) ? HAS_CHANNEL : NEW_SESSION);
                        final String existingQueueId = hasChannel() ? parseQueueId(userChannels) : null;
                        if (existingQueueId != null && queueId == null) {
                            queueId = existingQueueId;
                        }
                        sessionCredentials = new NinchatSessionCredentials(
                                params.getString("user_id"),
                                userAuth,
                                params.getString("session_id")
                        );

                        NinchatListQueuesTask.start();

                        if (listener != null) {
                            // TODO: Close previous session, hasn't been added since it didn't work
                            if (configuration != null && sessionCredentials != null) {
                                listener.onSessionInitiated(sessionCredentials);
                                sessionCredentials = null;
                            } else if (configuration != null) {
                                listener.onSessionInitiated(sessionCredentials);
                            } else {
                                resumedSession = NEW_SESSION;
                                listener.onSessionInitFailed();
                            }
                        }
                    } else {
                        resumedSession = NEW_SESSION;
                        listener.onSessionInitFailed();
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "Failed to get the event from " + params.string(), e);
                    resumedSession = NEW_SESSION;
                    listener.onSessionInitFailed();
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        final NinchatSDKEventListener eventListener = eventListenerWeakReference.get();
                        if (eventListener != null) {
                            eventListener.onSessionEvent(params);
                        }
                    }
                });
            }
        });
        this.session.setOnEvent(new EventHandler() {
            @Override
            public void onEvent(Props params, Payload payload, boolean lastReply) {
                Log.v(TAG, "onEvent: " + params.string() + ", " + payload.string() + ", " + lastReply);
                try {
                    final String event = params.getString("event");
                    final long currentActionId = params.getInt("action_id");
                    if (event.equals("realm_queues_found")) {
                        NinchatSessionManager.getInstance().parseQueues(params);
                    } else if (event.equals("queue_found") || event.equals("queue_updated")) {
                        NinchatSessionManager.getInstance().queueUpdated(params);
                    } else if (event.equals("audience_enqueued")) {
                        NinchatSessionManager.getInstance().audienceEnqueued(params);
                    } else if (event.equals("channel_joined")) {
                        NinchatSessionManager.getInstance().channelJoined(params);
                    } else if (event.equals("channel_found") && actionId == currentActionId) {
                        NinchatSessionManager.getInstance().channelJoined(params);
                    } else if (event.equals("channel_found") || event.equals("channel_updated")) {
                        NinchatSessionManager.getInstance().channelUpdated(params);
                    } else if (event.equals("message_received")) {
                        NinchatSessionManager.getInstance().messageReceived(params, payload);
                    } else if (event.equals("ice_begun")) {
                        NinchatSessionManager.getInstance().iceBegun(params);
                    } else if (event.equals("file_found")) {
                        NinchatSessionManager.getInstance().fileFound(params);
                    } else if (event.equals("channel_member_updated") || event.equals("user_updated")) {
                        NinchatSessionManager.getInstance().memberUpdated(params);
                    } else if (event.equals("audience_registered")) {
                        // send event that audience is register, and we can not close the session
                        EventBus.getDefault().post(new OnAudienceRegistered(false));
                    } else if (event.equals("error")) {
                        EventBus.getDefault().post(new OnAudienceRegistered(true));
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "Failed to get the event from " + params.string(), e);
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        final NinchatSDKEventListener eventListener = eventListenerWeakReference.get();
                        if (eventListener != null) {
                            eventListener.onEvent(params, payload);
                        }
                    }
                });
            }
        });
        this.session.setOnClose(new CloseHandler() {
            @Override
            public void onClose() {
                Log.v(TAG, "onClose");
            }
        });
        this.session.setOnConnState(new ConnStateHandler() {
            @Override
            public void onConnState(String state) {
                Log.v(TAG, "onConnState: " + state);
            }
        });
        this.session.setOnLog(new LogHandler() {
            @Override
            public void onLog(String msg) {
                Log.v(TAG, "onLog: " + msg);
            }
        });
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSession.Broadcast.SESSION_CREATED));
        }

    }

    public Session getSession() {
        return session;
    }

    private String parseQueueId(final Props currentUserChannel) {
        final NinchatPropVisitor parser = new NinchatPropVisitor();
        try {
            currentUserChannel.accept(parser);
            // audience has one or multiple channel
            for (String channelId : parser.properties.keySet()) {
                final Props channelInfo = (Props) parser.properties.get(channelId);
                final Props channelAttrs = channelInfo.getObject("channel_attrs");
                return channelAttrs.getString("queue_id");
            }
        } catch (final Exception e) {
            return null;
        }
        return null;
    }

    private String parseChannelId(final Props currentUserChannel) {
        final NinchatPropVisitor parser = new NinchatPropVisitor();
        try {
            currentUserChannel.accept(parser);
            // audience has one or multiple channel
            for (String channelId : parser.properties.keySet()) {
                return channelId;
            }
        } catch (final Exception e) {
            return null;
        }
        return null;
    }

    public boolean hasUserChannel(final Props currentUserChannel) {
        final NinchatPropVisitor parser = new NinchatPropVisitor();
        try {
            currentUserChannel.accept(parser);
            return parser.properties.keySet().size() > 0;
        } catch (final Exception e) {
            return false;
        }
    }


    public NinchatQueueListAdapter getNinchatQueueListAdapter(final Activity activity) {
        if (ninchatQueueListAdapter == null) {
            ninchatQueueListAdapter = new NinchatQueueListAdapter(activity, queues);
        }
        return ninchatQueueListAdapter;
    }

    public boolean hasQueues() {
        return queues.size() > 0;
    }

    public NinchatUser getMember(final String userId) {
        if (this.userId.equals(userId)) {
            return new NinchatUser(getUserName(), getUserName(), null, true);
        }
        return members.get(userId);
    }

    public boolean isGuestMemeber() {
        final NinchatUser currentUser = members.get(userId);
        if (currentUser == null) {
            return false;
        }
        return currentUser.isGuest();
    }

    public int getMemberCount() {
        return members.size();
    }

    public List<NinchatWebRTCServerInfo> getStunServers() {
        return stunServers;
    }

    public List<NinchatWebRTCServerInfo> getTurnServers() {
        return turnServers;
    }

    private class NinchatPropVisitor implements PropVisitor {

        private Map<String, Object> properties = new HashMap<>();

        @Override
        public void visitBool(String p0, boolean p1) throws Exception {
            properties.put(p0, p1);
        }

        @Override
        public void visitNumber(String p0, double p1) throws Exception {
            properties.put(p0, p1);
        }

        @Override
        public void visitObject(String p0, Props p1) throws Exception {
            properties.put(p0, p1);
        }

        @Override
        public void visitString(String p0, String p1) throws Exception {
            properties.put(p0, p1);
        }

        @Override
        public void visitStringArray(String p0, Strings p1) throws Exception {
            properties.put(p0, p1);
        }

        @Override
        public void visitObjectArray(String p0, Objects p1) throws Exception {
            properties.put(p0, p1);
        }
    }

    private void sendQueueParsingError() {
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

    private void parseQueues(final Props params) {
        Props remoteQueues;
        try {
            remoteQueues = params.getObject("realm_queues");
        } catch (final Exception e) {
            sessionError(e);
            sendQueueParsingError();
            return;
        }
        final NinchatPropVisitor parser = new NinchatPropVisitor();
        try {
            remoteQueues.accept(parser);
        } catch (final Exception e) {
            sessionError(e);
            sendQueueParsingError();
            return;
        }
        queues.clear();
        if (ninchatQueueListAdapter != null) {
            ninchatQueueListAdapter.clear();
        }
        final List<String> openQueues = getAudienceQueues();
        for (String currentQueueId : parser.properties.keySet()) {
            if (!openQueues.contains(currentQueueId)) {
                continue;
            }
            Props info;
            try {
                info = (Props) parser.properties.get(currentQueueId);
            } catch (final Exception e) {
                sessionError(e);
                sendQueueParsingError();
                return;
            }
            long position;
            try {
                position = info.getInt("position");
            } catch (final Exception e) {
                sessionError(e);
                sendQueueParsingError();
                return;
            }
            try {
                long queuePosition = info.getInt("queue_position");
                if (queuePosition != 0) {
                    resumedSession = resumedSession | (1 << IN_QUEUE);
                    queueId = currentQueueId;
                    position = queuePosition;
                }
            } catch (final Exception e) {
            }
            Props queueAttributes;
            try {
                queueAttributes = info.getObject("queue_attrs");
            } catch (final Exception e) {
                sessionError(e);
                sendQueueParsingError();
                return;
            }
            String name;
            try {
                name = queueAttributes.getString("name");
            } catch (final Exception e) {
                sessionError(e);
                sendQueueParsingError();
                return;
            }
            boolean closed = false;
            try {
                closed = queueAttributes.getBool("closed");
            } catch (final Exception e) {
                // Ignore
            }
            final NinchatQueue ninchatQueue = new NinchatQueue(currentQueueId, name);
            ninchatQueue.setPosition(position);
            ninchatQueue.setClosed(closed);
            queues.add(ninchatQueue);
            if (ninchatQueueListAdapter != null) {
                ninchatQueueListAdapter.addQueue(ninchatQueue);
            }
        }
        final Context context = contextWeakReference.get();
        if (context != null && hasQueues()) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSession.Broadcast.QUEUES_UPDATED));
        }
        final Activity activity = activityWeakReference.get();
        if (activity != null) {
            activity.startActivityForResult(NinchatActivity.getLaunchIntent(activity, queueId), requestCode);
        }
        final NinchatSDKEventListener listener = eventListenerWeakReference.get();
        if (listener != null) {
            listener.onSessionStarted();
        }
    }

    public NinchatQueue getQueue(final String queueId) {
        for (NinchatQueue queue : queues) {
            if (queue.getId().equals(queueId)) {
                return queue;
            }
        }
        return null;
    }

    private String parseQueue(final Props params) {
        String queueId;
        try {
            queueId = params.getString("queue_id");
        } catch (final Exception e) {
            sessionError(e);
            return null;
        }
        long position;
        try {
            position = params.getInt("queue_position");
        } catch (final Exception e) {
            sessionError(e);
            return null;
        }
        Props queueAttributes;
        try {
            queueAttributes = params.getObject("queue_attrs");
        } catch (final Exception e) {
            sessionError(e);
            sendQueueParsingError();
            return null;
        }
        boolean closed = false;
        try {
            closed = queueAttributes.getBool("closed");
        } catch (final Exception e) {
            // Ignore
        }
        final NinchatQueue queue = getQueue(queueId);
        if (queue != null) {
            queue.setPosition(position);
            queue.setClosed(closed);
        }
        return queueId;
    }

    private void queueUpdated(final Props params) {
        parseQueue(params);
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.QUEUE_UPDATED));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSession.Broadcast.QUEUES_UPDATED));
        }
    }

    private void audienceEnqueued(final Props params) {
        resumedSession = NEW_SESSION;
        final String queueId = parseQueue(params);
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.AUDIENCE_ENQUEUED).putExtra(Parameter.QUEUE_ID, queueId));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.QUEUE_UPDATED));
        }
    }

    private void audienceEnqueued(final String queueId) {
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.AUDIENCE_ENQUEUED).putExtra(Parameter.QUEUE_ID, queueId));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.QUEUE_UPDATED));
        }
    }

    private void channelJoined(final Props params) {
        boolean isClosed = false;

        try {
            Props channelAttrs = params.getObject("channel_attrs");
            isClosed = channelAttrs.getBool("closed");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            channelId = params.getString("channel_id");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        Props members;
        try {
            members = params.getObject("channel_members");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        final NinchatPropVisitor parser = new NinchatPropVisitor();
        try {
            members.accept(parser);
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        this.members.clear();
        for (final String userId : parser.properties.keySet()) {
            Props memberAttrs = (Props) parser.properties.get(userId);
            Props userAttrs;
            try {
                userAttrs = memberAttrs.getObject("user_attrs");
            } catch (final Exception e) {
                // Ignore
                continue;
            }
            String displayName = null;
            try {
                displayName = userAttrs.getString("name");
            } catch (final Exception e) {
                continue;
            }
            String realName = null;
            try {
                realName = userAttrs.getString("realname");
            } catch (final Exception e) {
                // Ignore
            }
            String avatar = null;
            try {
                avatar = userAttrs.getString("iconurl");
            } catch (final Exception e) {
                // Ignore
            }
            boolean guest = false;
            try {
                guest = userAttrs.getBool("guest");
            } catch (final Exception e) {
                // Ignore
            }
            this.members.put(userId, new NinchatUser(displayName, realName, avatar, guest));
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messageAdapter = new NinchatMessageAdapter();
                messageAdapter.addMetaMessage("", getChatStarted());
            }
        });
        final Context context = contextWeakReference.get();
        if (context != null) {
            Intent i = new Intent(Broadcast.CHANNEL_JOINED);
            i.putExtra(Parameter.CHAT_IS_CLOSED, isClosed);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        }
    }

    public void loadChannelHistory(final String messageId) {
        final Props load = new Props();
        load.setString("action", "load_history");
        load.setString("channel_id", channelId);
        load.setInt("history_order", 1);

        if (messageId != null && !messageId.isEmpty()) {
            load.setString("message_id", messageId);
        }

        try {
            session.send(load, null);
        } catch (final Exception e) {
            // Ignore
        }
    }

    private void channelUpdated(final Props params) {
        try {
            if (!params.getString("channel_id").equals(channelId)) {
                return;
            }
        } catch (final Exception e) {
            return;
        }
        Props channelAttributes;
        try {
            channelAttributes = params.getObject("channel_attrs");
        } catch (final Exception e) {
            return;
        }
        boolean closed = false;
        try {
            closed = channelAttributes.getBool("closed");
        } catch (final Exception e) {
            return;
        }
        boolean suspended = false;
        try {
            suspended = channelAttributes.getBool("suspended");
        } catch (final Exception e) {
            return;
        }
        final Context context = contextWeakReference.get();
        if (closed || suspended) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.CHANNEL_CLOSED));
        }
    }

    private void messageReceived(final Props params, final Payload payload) {
        if (channelId == null) {
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

        if (MessageTypes.WEBRTC_MESSAGE_TYPES.contains(messageType) && !sender.equals(userId)) {
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
        if (MessageTypes.UI_COMPOSE.equals(messageType)) {
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

        if (actionId == currentActionId) {
            EventBus.getDefault().post(new OnPostAudienceQuestionnaire());
        }

        if (!messageType.equals(MessageTypes.TEXT) && !messageType.equals(MessageTypes.FILE)) {
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
                        final NinchatFile ninchatFile = new NinchatFile(messageId, fileId, filename, filesize, filetype, timestampMs, sender, !sender.equals(userId));
                        this.files.put(fileId, ninchatFile);
                        NinchatDescribeFileTask.start(fileId);
                    }
                } else {
                    messageAdapter.add(messageId, new NinchatMessage(message.getString("text"), null, sender, timestampMs, !sender.equals(userId)));
                }
            } catch (final JSONException e) {
                // Ignore
            }
        }
    }

    private String guessMimeTypeFromFileName(final String name) {
        final String extension = name.replaceAll(".*\\.", "");
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (mimeType == null) {
            return "application/octet-stream";
        }
        return mimeType;
    }

    private void fileFound(final Props params) {
        String fileId;
        try {
            fileId = params.getString("file_id");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        String url;
        try {
            url = params.getString("file_url");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        long urlExpiry;
        try {
            urlExpiry = params.getInt("url_expiry");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        float aspectRatio;
        long width;
        long height;
        try {
            final Props attrs = params.getObject("file_attrs");
            final Props thumbnail = attrs.getObject("thumbnail");
            width = thumbnail.getInt("width");
            height = thumbnail.getInt("height");
            aspectRatio = ((float) width) / ((float) height);
        } catch (final Exception e) {
            aspectRatio = 16.0f / 9.0f;
            width = -1;
            height = -1;
        }
        final NinchatFile file = files.get(fileId);
        file.setUrl(url);
        file.setUrlExpiry(new Date(urlExpiry));
        file.setAspectRatio(aspectRatio);
        file.setWidth(width);
        file.setHeight(height);
        file.setDownloadableFile(width == -1 || height == -1);

        messageAdapter.add(file.getMessageId(), new NinchatMessage(null, fileId, file.getSender(), file.getTimestamp(), file.isRemote()));
    }

    private void memberUpdated(final Props params) {
        String sender;
        try {
            sender = params.getString("user_id");
        } catch (final Exception e) {
            return;
        }
        if (sender.equals(userId)) {
            // Do not update myself
            return;
        }
        Props memberAttrs = null;
        try {
            memberAttrs = params.getObject("member_attrs");
        } catch (final Exception e) {
            // Ignore
        }
        boolean addWritingMessage = false;
        try {
            addWritingMessage = memberAttrs.getBool("writing");
        } catch (final Exception e) {
            // Ignore
        }
        // null check
        if (messageAdapter == null) return;
        if (addWritingMessage) {
            messageAdapter.addWriting(sender);
        } else {
            messageAdapter.removeWritingMessage(sender);
        }
    }

    private void iceBegun(final Props params) {
        Objects stunServers;
        try {
            stunServers = params.getObjectArray("stun_servers");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        try {
            if (this.stunServers == null) {
                this.stunServers = new ArrayList<>();
            }
            this.stunServers.clear();
            for (int i = 0; i < stunServers.length(); ++i) {
                final Props stunServerProps = stunServers.get(i);
                final Strings urls = stunServerProps.getStringArray("urls");
                for (int j = 0; j < urls.length(); ++j) {
                    this.stunServers.add(new NinchatWebRTCServerInfo(urls.get(j)));
                }
            }
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        Objects turnServers;
        try {
            turnServers = params.getObjectArray("turn_servers");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        try {
            if (this.turnServers == null) {
                this.turnServers = new ArrayList<>();
            }
            this.turnServers.clear();
            for (int i = 0; i < turnServers.length(); ++i) {
                final Props turnServerProps = turnServers.get(i);
                final String username = turnServerProps.getString("username");
                final String credential = turnServerProps.getString("credential");
                final Strings urls = turnServerProps.getStringArray("urls");
                for (int j = 0; j < urls.length(); ++j) {
                    this.turnServers.add(new NinchatWebRTCServerInfo(urls.get(j), username, credential));
                }
            }
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.WEBRTC_MESSAGE)
                    .putExtra(Broadcast.WEBRTC_MESSAGE_TYPE, MessageTypes.WEBRTC_SERVERS_PARSED));
        }
    }

    public void sendMessage(final String message) {
        try {
            final JSONObject data = new JSONObject();
            data.put("text", message);
            NinchatSendMessageTask.start(MessageTypes.TEXT, data.toString(), channelId);
        } catch (final JSONException e) {
            sessionError(e);
        }
    }

    public void sendUIAction(final JSONObject selected) {
        try {
            final JSONObject data = new JSONObject();
            data.put("action", "click");
            data.put("target", selected);
            NinchatSendMessageTask.start(MessageTypes.UI_ACTION, data.toString(), channelId);
        } catch (final JSONException e) {
            Log.e(TAG, "Error when sending multichoice answer!", e);
        }
    }

    public void sendIsWritingUpdate(final boolean isWriting) {
        NinchatSendIsWritingTask.start(channelId, userId, isWriting);
    }

    public void sendImage(final String name, final byte[] data) {
        NinchatSendFileTask.start(name, data, channelId);
    }

    public void sendWebRTCCall() {
        NinchatSendMessageTask.start(MessageTypes.CALL, "{}", channelId);
    }

    public void sendWebRTCCallAnswer(final boolean answer) {
        try {
            final JSONObject data = new JSONObject();
            data.put("answer", answer);
            NinchatSendMessageTask.start(MessageTypes.PICK_UP, data.toString(), channelId);
        } catch (final JSONException e) {
            sessionError(e);
        }
    }

    public void sendWebRTCBeginIce() {
        NinchatSendBeginIceTask.start();
    }

    public void sendWebRTCSDPReply(final SessionDescription sessionDescription) {
        try {
            final Map<SessionDescription.Type, String> typeMap = new HashMap<>();
            typeMap.put(SessionDescription.Type.ANSWER, MessageTypes.ANSWER);
            typeMap.put(SessionDescription.Type.OFFER, MessageTypes.OFFER);
            final String messageType = typeMap.get(sessionDescription.type);
            if (messageType == null) {
                return;
            }
            final JSONObject data = new JSONObject();
            final JSONObject sdp = new JSONObject();
            sdp.put("type", sessionDescription.type.canonicalForm());
            sdp.put("sdp", sessionDescription.description);
            data.put("sdp", sdp);
            NinchatSendMessageTask.start(messageType, data.toString(), channelId);
        } catch (final JSONException e) {
            sessionError(e);
        }
    }

    public void sendWebRTCIceCandidate(final IceCandidate iceCandidate) {
        try {
            final JSONObject data = new JSONObject();
            final JSONObject candidate = new JSONObject();
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("candidate", iceCandidate.sdp);
            candidate.put("id", iceCandidate.sdpMLineIndex);
            candidate.put("label", iceCandidate.sdpMid);
            data.put("candidate", candidate);
            NinchatSendMessageTask.start(MessageTypes.ICE_CANDIDATE, data.toString(), channelId);
        } catch (final JSONException e) {
            sessionError(e);
        }
    }

    public void sendWebRTCHangUp() {
        NinchatSendMessageTask.start(MessageTypes.HANG_UP, "{}", channelId);
    }

    public void sendRating(final int rating) {
        try {
            final JSONObject value = new JSONObject();
            value.put("rating", rating);
            final JSONObject data = new JSONObject();
            data.put("data", value);
            NinchatSendMessageTask.start(MessageTypes.RATING_OR_POST_ANSWERS, data.toString(2), channelId);
        } catch (final JSONException e) {
            // Ignore
        }
    }

    public void sendPostAnswers(final JSONObject postAnswers) {
        try {
            final JSONObject value = new JSONObject();
            value.put("post_answers", postAnswers);
            final JSONObject data = new JSONObject();
            data.put("data", value);
            data.put("time", System.currentTimeMillis());
            NinchatSendMessageTask.start(MessageTypes.RATING_OR_POST_ANSWERS, data.toString(2), channelId, requestCallback);
        } catch (final JSONException e) {
            // Ignore
        }
    }

    private JSONArray getArrayFromConfiguration(final String key) {
        JSONArray array = null;
        if (configuration != null) {
            if (preferredEnvironments != null) {
                for (final String configuration : preferredEnvironments) {
                    try {
                        array = this.configuration.getJSONObject(configuration).getJSONArray(key);
                    } catch (final Exception e) {
                        // Ignore…
                    }
                    if (array != null) {
                        return array;
                    }
                }
            }
            try {
                return getDefault().getJSONArray(key);
            } catch (final Exception e) {
                // Ignore…
            }
        }
        return array;
    }

    private boolean getBooleanFromConfiguration(final String key) throws JSONException {
        Boolean value = null;
        if (configuration != null) {
            if (preferredEnvironments != null) {
                for (final String configuration : preferredEnvironments) {
                    try {
                        value = this.configuration.getJSONObject(configuration).getBoolean(key);
                    } catch (final Exception e) {
                        // Ignore…
                    }
                    if (value != null) {
                        return value;
                    }
                }
            }
            try {
                return getDefault().getBoolean(key);
            } catch (final Exception e) {
                throw new JSONException("");
            }
        }
        throw new JSONException("");
    }

    private String getStringFromConfiguration(final String key) throws JSONException {
        String value = null;
        if (configuration != null) {
            if (preferredEnvironments != null) {
                for (final String configuration : preferredEnvironments) {
                    try {
                        value = this.configuration.getJSONObject(configuration).getString(key);
                    } catch (final Exception e) {
                        // Ignore…
                    }
                    if (value != null) {
                        return value;
                    }
                }
            }
            try {
                return getDefault().getString(key);
            } catch (final Exception e) {
                throw new JSONException("");
            }
        }
        throw new JSONException("");

    }

    private JSONObject getDefault() throws JSONException {
        if (configuration != null) {
            return configuration.getJSONObject("default");
        }
        return null;
    }

    public List<String> getAudienceQueues() {
        final List<String> queues = new ArrayList<>();
        try {
            final JSONArray array = getArrayFromConfiguration("audienceQueues");
            for (int i = 0; i < array.length(); ++i) {
                queues.add(array.getString(i));
            }
        } catch (final Exception e) {
            // Ignore
        }
        return queues;
    }

    public String getRealmId() {
        try {
            return getStringFromConfiguration("audienceRealmId");
        } catch (final Exception e) {
            return null;
        }
    }

    private String center(final String text) {
        return (text == null || (text.contains("<center>") && text.contains("</center>"))) ?
                text : ("<center>" + text + "</center>");
    }

    private Spanned toSpanned(final String text) {
        final String centeredText = center(text) == null ? "" : center(text);
        return centeredText == null ? null :
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(centeredText, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(centeredText);
    }

    public Spanned getWelcome() {
        final String key = "welcome";
        String welcomeText = key;
        try {
            welcomeText = getStringFromConfiguration(key);
        } catch (final Exception e) {
        }
        return toSpanned(welcomeText);
    }

    public Spanned getNoQueues() {
        final String key = "noQueuesText";
        try {
            return toSpanned(getStringFromConfiguration(key));
        } catch (final Exception e) {
            return toSpanned(key);
        }
    }

    public String getTranslation(final String key) {
        if (configuration != null) {
            if (preferredEnvironments != null) {
                for (final String configuration : preferredEnvironments) {
                    try {
                        return this.configuration.getJSONObject(configuration).getJSONObject("translations").getString(key);
                    } catch (final Exception e) {
                        // Ignore…
                    }
                }
            }
            try {
                return getDefault().getJSONObject("translations").getString(key);
            } catch (final Exception e) {
                // Ignore…
            }
        }
        return key;
    }

    public boolean showNoThanksButton() {
        final String key = "noThanksButton";
        try {
            return getBooleanFromConfiguration(key);
        } catch (final Exception e) {
            return true;
        }
    }

    public String getCloseWindow() {
        return getTranslation("Close window");
    }

    public String getUserName() {
        if (this.ninchatConfiguration != null && this.ninchatConfiguration.getUserName() != null) {
            return this.ninchatConfiguration.getUserName();
        }
        final String key = "userName";
        try {
            return getStringFromConfiguration(key);
        } catch (final Exception e) {
            return null;
        }
    }

    // Get username or agentname if it is set in configuration
    public String getName(boolean isAgent) {

        if (!isAgent) return getUserName();

        try {
            return getStringFromConfiguration("agentName");
        } catch (final Exception e) {
            return null;
        }
    }

    public String getSendButtonText() {
        final String key = "sendButtonText";
        try {
            return getStringFromConfiguration(key);
        } catch (final Exception e) {
            return null;
        }
    }

    public String getSubmitButtonText() {
        return getTranslation("Submit");
    }

    public boolean isAttachmentsEnabled() {
        final String key = "supportFiles";
        try {
            return getBooleanFromConfiguration(key);
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isVideoEnabled() {
        final String key = "supportVideo";
        try {
            return getBooleanFromConfiguration(key);
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean showAvatars(final boolean agentAvatar) {
        final String key = agentAvatar ? "agentAvatar" : "userAvatar";
        try {
            return getBooleanFromConfiguration(key);
        } catch (final Exception e) {
            return getDefaultAvatar(agentAvatar) != null;
        }
    }

    public String getDefaultAvatar(final boolean agentAvatar) {
        final String key = agentAvatar ? "agentAvatar" : "userAvatar";
        try {
            return getStringFromConfiguration(key);
        } catch (final Exception e) {
            return null;
        }
    }

    private String replacePlaceholder(final String origin, final String replacement) {
        return origin.replaceFirst("\\{\\{([^}]*?)\\}\\}", replacement);
    }

    public String getChatStarted() {
        final String key = "Audience in queue {{queue}} accepted.";
        String chatStarted = getTranslation(key);
        final NinchatQueue queue = getQueue(this.queueId);
        String name = "";
        if (queue != null) {
            name = queue.getName();
        }
        return center(replacePlaceholder(chatStarted, name));
    }

    public Spanned getChatEnded() {
        return toSpanned(getTranslation("Conversation ended"));
    }

    public String getCloseChat() {
        return getTranslation("Close chat");
    }

    public String getCloseChatDescription() {
        final String key = "closeConfirmText";
        try {
            return getStringFromConfiguration(key);
        } catch (final Exception e) {
            return key;
        }
    }

    public String getContinueChat() {
        return getTranslation("Continue chat");
    }

    public String getEnterMessage() {
        return getTranslation("Enter your message");
    }

    public String getVideoChatTitle() {
        return getTranslation("You are invited to a video chat");
    }

    public String getVideoChatDescription() {
        return getTranslation("wants to video chat with you");
    }

    public String getVideoCallAccept() {
        return getTranslation("Accept");
    }

    public String getVideoCallDecline() {
        return getTranslation("Decline");
    }

    public String getVideoCallMetaMessage() {
        return center(getTranslation("You are invited to a video chat"));
    }

    public String getVideoCallAccepted() {
        return center(getTranslation("Video chat answered"));
    }

    public String getVideoCallRejected() {
        return center(getTranslation("Video chat declined"));
    }

    public Spanned getMOTD() {
        final String key = "motd";
        String motd = key;
        try {
            motd = getStringFromConfiguration(key);
        } catch (final Exception e) {
        }
        return toSpanned(motd);
    }

    public String getQueueName(final String name) {
        return replacePlaceholder(getTranslation("Join audience queue {{audienceQueue.queue_attrs.name}}"), name);
    }

    public String getQueueName(final String name, boolean closed) {
        if (closed) {
            return replacePlaceholder(getTranslation("Join audience queue {{audienceQueue.queue_attrs.name}} (closed)"), name);
        }

        return replacePlaceholder(getTranslation("Join audience queue {{audienceQueue.queue_attrs.name}}"), name);
    }

    public Spanned getQueueStatus(final String queueId) {
        NinchatQueue selectedQueue = getQueue(queueId);
        if (selectedQueue == null) {
            return null;
        }
        final long position = selectedQueue.getPosition();
        final String key = position == 1
                ? "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are next."
                : "Joined audience queue {{audienceQueue.queue_attrs.name}}, you are at position {{audienceQueue.queue_position}}.";
        String queueStatus = getTranslation(key);
        if (queueStatus.contains("audienceQueue.queue_attrs.name")) {
            queueStatus = replacePlaceholder(queueStatus, selectedQueue.getName());
        }
        if (queueStatus.contains("audienceQueue.queue_position")) {
            queueStatus = replacePlaceholder(queueStatus, String.valueOf(position));
        }
        return toSpanned(queueStatus);
    }

    public Spanned getQueueMessage() {
        final String key = "inQueueText";
        String queueMessage = null;
        try {
            queueMessage = getStringFromConfiguration(key);
        } catch (final Exception e) {
        }
        return toSpanned(queueMessage);
    }

    public boolean showRating() {
        try {
            return getBooleanFromConfiguration("audienceRating");
        } catch (final Exception e) {
            return false;
        }
    }

    public Spanned getFeedbackTitle() {
        return toSpanned(getTranslation("How was our customer service?"));
    }

    public Spanned getThankYouText() {
        return toSpanned(getTranslation("Thank you for the conversation!"));
    }

    public String getFeedbackPositive() {
        return getTranslation("Good");
    }

    public String getFeedbackNeutral() {
        return getTranslation("Okay");
    }

    public String getFeedbackNegative() {
        return getTranslation("Poor");
    }

    public String getFeedbackSkip() {
        return getTranslation("Skip");
    }

    public NinchatQuestionnaireHolder getNinchatQuestionnaireHolder() {
        return ninchatQuestionnaireHolder;
    }

    private RequestCallback requestCallback = currentActionId -> actionId = currentActionId;

    public interface RequestCallback {
        void onActionId(long actionId);
    }

    public boolean isResumedSession() {
        return isInQueue() || hasChannel();
    }

    public boolean isInQueue() {
        return (resumedSession & (1 << IN_QUEUE)) != 0;
    }

    public boolean hasChannel() {
        return (resumedSession & (1 << HAS_CHANNEL)) != 0;
    }

    public void resetSession() {
        resumedSession = NEW_SESSION;
    }

    public void close() {
        if (session != null) {
            session.close();
        }
        if (messageAdapter != null) {
            messageAdapter.clear();
        }

        if (userChannels != null) {
            userChannels = null;
        }
    }

}
