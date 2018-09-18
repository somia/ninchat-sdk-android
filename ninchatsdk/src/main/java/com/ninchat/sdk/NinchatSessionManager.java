package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

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
import com.ninchat.sdk.adapters.NinchatQueueListAdapter;
import com.ninchat.sdk.models.NinchatQueue;
import com.ninchat.sdk.models.NinchatWebRTCServerInfo;
import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;
import com.ninchat.sdk.tasks.NinchatJoinQueueTask;
import com.ninchat.sdk.tasks.NinchatListQueuesTask;
import com.ninchat.sdk.tasks.NinchatOpenSessionTask;
import com.ninchat.sdk.tasks.NinchatSendBeginIceTask;
import com.ninchat.sdk.tasks.NinchatSendMessageTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager {

    public static final class Broadcast {
        public static final String CHANNEL_JOINED = BuildConfig.APPLICATION_ID + ".channelJoined";
        public static final String CHANNEL_CLOSED = BuildConfig.APPLICATION_ID + ".channelClosed";
        public static final String NEW_MESSAGE = BuildConfig.APPLICATION_ID + ".newMessage";
        public static final String MESSAGE_CONTENT = NEW_MESSAGE + ".content";
        public static final String WEBRTC_MESSAGE = BuildConfig.APPLICATION_ID + ".webRTCMessage";
        public static final String WEBRTC_MESSAGE_TYPE = WEBRTC_MESSAGE + ".type";
        public static final String WEBRTC_MESSAGE_CONTENT = WEBRTC_MESSAGE + ".content";
    }

    public static final class MessageTypes {
        public static final String TEXT = "ninchat.com/text";
        public static final String WEBRTC_PREFIX = "ninchat.com/rtc/";
        public static final String ICE_CANDIDATE = WEBRTC_PREFIX + "ice-candidate";
        public static final String ANSWER = WEBRTC_PREFIX + "answer";
        public static final String OFFER = WEBRTC_PREFIX + "offer";
        public static final String CALL = WEBRTC_PREFIX + "call";
        public static final String PICK_UP = WEBRTC_PREFIX + "pick-up";
        public static final String HANG_UP = WEBRTC_PREFIX + "hang-up";
        public static final String WEBRTC_SERVERS_PARSED = WEBRTC_PREFIX + "serversParsed";

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

    static void init(final Context context, final String configurationKey, final String siteSecret) {
        instance = new NinchatSessionManager(context, siteSecret);
        NinchatConfigurationFetchTask.start(configurationKey);
    }

    public static String getServer() {
        return BuildConfig.DEBUG ? "api.luupi.net" : "api.ninchat.com";
    }

    public static NinchatSessionManager getInstance() {
        return instance;
    }

    public Context getContext() {
        return contextWeakReference.get();
    }

    public static void joinQueue(final String queueId) {
        NinchatJoinQueueTask.start(queueId);
    }

    protected static NinchatSessionManager instance;
    protected static final String TAG = NinchatSessionManager.class.getSimpleName();

    protected NinchatSessionManager(final Context context, final String siteSecret) {
        this.contextWeakReference = new WeakReference<>(context);
        this.siteSecret = siteSecret;
        this.configuration = null;
        this.session = null;
        this.queues = new ArrayList<>();
        this.ninchatQueueListAdapter = null;
    }

    protected WeakReference<Context> contextWeakReference;
    protected String siteSecret;

    protected JSONObject configuration;
    protected Session session;
    protected List<NinchatQueue> queues;
    protected NinchatQueueListAdapter ninchatQueueListAdapter;
    protected String channelId;
    protected List<NinchatWebRTCServerInfo> stunServers;
    protected List<NinchatWebRTCServerInfo> turnServers;

    public void setConfiguration(final String config) throws JSONException {
        try {
            Log.v(TAG, "Got configuration: " + config);
            this.configuration = new JSONObject(config);
            Log.i(TAG, "Configuration fetched successfully!");
        } catch (final JSONException e) {
            this.configuration = null;
            throw e;
        }
        NinchatOpenSessionTask.start(siteSecret);
    }

    public void sessionError(final Exception error) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final Context context = contextWeakReference.get();
                if (context != null) {
                    Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void setSession(final Session session) {
        this.session = session;
        this.session.setOnSessionEvent(new SessionEventHandler() {
            @Override
            public void onSessionEvent(Props params) {
                try {
                    Log.v(TAG, "onSessionEvent: " + params.string());
                    final String event = params.getString("event");
                    if (event.equals("session_created")) {
                        NinchatListQueuesTask.start();
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "Failed to get the event from " + params.string(), e);
                }
            }
        });
        this.session.setOnEvent(new EventHandler() {
            @Override
            public void onEvent(Props params, Payload payload, boolean lastReply) {
                Log.v(TAG, "onEvent: " + params.string() + ", " + payload.string() + ", " + lastReply);
                try {
                    final String event = params.getString("event");
                    if (event.equals("realm_queues_found")) {
                        NinchatSessionManager.getInstance().parseQueues(params);
                    } else if (event.equals("queue_updated")) {
                        //NinchatSessionManager.getInstance().queueUpdated(params);
                    } else if (event.equals("audience_enqueued")) {
                        //NinchatSessionManager.getInstance().audienceEnqueued(params);
                    } else if (event.equals("channel_joined")) {
                        NinchatSessionManager.getInstance().channelJoined(params);
                    } else if (event.equals("channel_updated")) {
                        NinchatSessionManager.getInstance().channelUpdated(params);
                    } else if (event.equals("message_received")) {
                        NinchatSessionManager.getInstance().messageReceived(params, payload);
                    } else if (event.equals("ice_begun")) {
                        NinchatSessionManager.getInstance().iceBegun(params);
                    }
                } catch (final Exception e) {
                    Log.e(TAG, "Failed to get the event from " + params.string(), e);
                }
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
    }

    public Session getSession() {
        return session;
    }

    public NinchatQueueListAdapter getNinchatQueueListAdapter(final Activity activity) {
        if (ninchatQueueListAdapter == null) {
            ninchatQueueListAdapter = new NinchatQueueListAdapter(activity, queues);
        }
        return ninchatQueueListAdapter;
    }

    public List<NinchatQueue> getQueues() {
        return queues;
    }

    public boolean hasQueues() {
        return queues.size() > 0;
    }

    public List<NinchatWebRTCServerInfo> getStunServers() {
        return stunServers;
    }

    public List<NinchatWebRTCServerInfo> getTurnServers() {
        return turnServers;
    }

    private class QueuePropVisitor implements PropVisitor {

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

        }
    }

    private void parseQueues(final Props params) {
        Props remoteQueues;
        try {
            remoteQueues = params.getObject("realm_queues");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        final QueuePropVisitor parser = new QueuePropVisitor();
        try {
            remoteQueues.accept(parser);
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        for (String queueId : parser.properties.keySet()) {
            Props queue;
            try {
                queue = (Props) parser.properties.get(queueId);
            } catch (final Exception e) {
                sessionError(e);
                return;
            }
            Props queueAttributes;
            try {
                queueAttributes = queue.getObject("queue_attrs");
            } catch (final Exception e) {
                sessionError(e);
                return;
            }
            String name;
            try {
                name = queueAttributes.getString("name");
            } catch (final Exception e) {
                sessionError(e);
                return;
            }
            final NinchatQueue ninchatQueue = new NinchatQueue(queueId, name);
            queues.add(ninchatQueue);
            if (ninchatQueueListAdapter != null) {
                ninchatQueueListAdapter.addQueue(ninchatQueue);
            }
        }
        final Context context = contextWeakReference.get();
        if (context != null && hasQueues()) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSession.Broadcast.QUEUES_UPDATED));
        }
    }

    private void queueUpdated(final Props params) {}

    private void channelJoined(final Props params) {
        try {
            channelId = params.getString("channel_id");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.CHANNEL_JOINED));
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
        String messageType;
        try {
            messageType = params.getString("message_type");
        } catch (final Exception e) {
            return;
        }
        final Context context = contextWeakReference.get();
        if (context == null) {
            return;
        }
        if (MessageTypes.WEBRTC_MESSAGE_TYPES.contains(messageType)) {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < payload.length(); ++i) {
                builder.append(new String(payload.get(i)));
            }
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(Broadcast.WEBRTC_MESSAGE)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_TYPE, messageType)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_CONTENT, builder.toString()));
            return;
        }
        if (!messageType.equals(MessageTypes.TEXT)) {
            return;
        }
        for (int i = 0; i < payload.length(); ++i) {
            try {
                final JSONObject message = new JSONObject(new String(payload.get(i)));
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcast(new Intent(Broadcast.NEW_MESSAGE)
                                .putExtra(Broadcast.MESSAGE_CONTENT, message.getString("text")));
            } catch (final JSONException e) {
                // Ignore
            }
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

    public String getRealmId() {
        if (configuration != null) {
            try {
                return configuration.getJSONObject("default").getString("audienceRealmId");
            } catch (final JSONException e) {
                return null;
            }
        }
        return null;
    }

    public boolean showRating() {
        if (configuration != null) {
            try {
                return configuration.getJSONObject("default").getBoolean("audienceRating");
            } catch (final JSONException e) {
                return false;
            }
        }
        return false;
    }

    public void close() {
        if (session != null) {
            session.close();
        }
    }

}
