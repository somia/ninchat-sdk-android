package com.ninchat.sdk;

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
import com.ninchat.client.Payload;
import com.ninchat.client.PropVisitor;
import com.ninchat.client.Props;
import com.ninchat.client.Session;
import com.ninchat.client.SessionEventHandler;
import com.ninchat.client.Strings;
import com.ninchat.sdk.adapters.NinchatQueueListAdapter;
import com.ninchat.sdk.models.NinchatQueue;
import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;
import com.ninchat.sdk.tasks.NinchatJoinQueueTask;
import com.ninchat.sdk.tasks.NinchatListQueuesTask;
import com.ninchat.sdk.tasks.NinchatOpenSessionTask;
import com.ninchat.sdk.tasks.NinchatSendMessageTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager implements SessionEventHandler, EventHandler, CloseHandler, ConnStateHandler, LogHandler {

    public static final class Broadcast {
        public static final String CHANNEL_JOINED = BuildConfig.APPLICATION_ID + ".channelJoined";
        public static final String CHANNEL_CLOSED = BuildConfig.APPLICATION_ID + ".channelClosed";
        public static final String NEW_MESSAGE = BuildConfig.APPLICATION_ID + ".newMessage";
        public static final String MESSAGE_CONTENT = NEW_MESSAGE + ".content";
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
        this.ninchatQueueListAdapter = new NinchatQueueListAdapter();
    }

    protected WeakReference<Context> contextWeakReference;
    protected String siteSecret;

    protected JSONObject configuration;
    protected Session session;
    protected NinchatQueueListAdapter ninchatQueueListAdapter;
    protected String channelId;

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
        this.session.setOnSessionEvent(this);
        this.session.setOnEvent(this);
        this.session.setOnClose(this);
        this.session.setOnConnState(this);
        this.session.setOnLog(this);
    }

    public Session getSession() {
        return session;
    }

    public NinchatQueueListAdapter getNinchatQueueListAdapter() {
        return ninchatQueueListAdapter;
    }

    public List<NinchatQueue> getQueues() {
        return ninchatQueueListAdapter.getQueues();
    }

    public boolean hasQueues() {
        return ninchatQueueListAdapter.getQueues().size() > 0;
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
            ninchatQueueListAdapter.addQueue(new NinchatQueue(queueId, name));
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
        if (!messageType.equals("ninchat.com/text")) {
            return;
        }
        final Context context = contextWeakReference.get();
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

    public void sendMessage(final String message) {
        NinchatSendMessageTask.start(message, channelId);
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

    public void close() {
        session.close();
    }

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

    @Override
    public void onEvent(Props params, Payload payload, boolean lastReply) {
        Log.v(TAG, "onEvent: " + params.string() + ", " + payload.string() + ", " + lastReply);
        try {
            final String event = params.getString("event");
            if (event.equals("realm_queues_found")) {
                parseQueues(params);
            } else if (event.equals("queue_updated")) {
                //queueUpdated(params);
            } else if (event.equals("audience_enqueued")) {
                //audienceEnqueued(params);
            } else if (event.equals("channel_joined")) {
                channelJoined(params);
            } else if (event.equals("channel_updated")) {
                channelUpdated(params);
            } else if (event.equals("message_received")) {
                messageReceived(params, payload);
            }
        } catch (final Exception e) {
            Log.e(TAG, "Failed to get the event from " + params.string(), e);
        }
    }

    @Override
    public void onClose() {
        Log.v(TAG, "onClose");
    }

    @Override
    public void onConnState(String state) {
        Log.v(TAG, "onConnState: " + state);
    }

    @Override
    public void onLog(String msg) {
        Log.v(TAG, "onLog: " + msg);
    }
}
