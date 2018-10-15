package com.ninchat.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.webkit.MimeTypeMap;
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
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatQueue;
import com.ninchat.sdk.models.NinchatWebRTCServerInfo;
import com.ninchat.sdk.tasks.NinchatConfigurationFetchTask;
import com.ninchat.sdk.tasks.NinchatDescribeFileTask;
import com.ninchat.sdk.tasks.NinchatJoinQueueTask;
import com.ninchat.sdk.tasks.NinchatListQueuesTask;
import com.ninchat.sdk.tasks.NinchatOpenSessionTask;
import com.ninchat.sdk.tasks.NinchatSendBeginIceTask;
import com.ninchat.sdk.tasks.NinchatSendFileTask;
import com.ninchat.sdk.tasks.NinchatSendMessageTask;

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

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 24/08/2018.
 */
public final class NinchatSessionManager {

    public static final class Broadcast {
        public static final String CHANNEL_JOINED = BuildConfig.APPLICATION_ID + ".channelJoined";
        public static final String CHANNEL_UPDATED = BuildConfig.APPLICATION_ID + ".channelUpdated";
        public static final String CHANNEL_CLOSED = BuildConfig.APPLICATION_ID + ".channelClosed";
        public static final String NEW_MESSAGE = BuildConfig.APPLICATION_ID + ".newMessage";
        public static final String MESSAGE_CONTENT = NEW_MESSAGE + ".content";
        public static final String MESSAGE_FILE_ID = NEW_MESSAGE + ".fileId";
        public static final String MESSAGE_SENDER = NEW_MESSAGE + ".sender";
        public static final String MESSAGE_IS_REMOTE = NEW_MESSAGE + ".isRemote";
        public static final String MESSAGE_TIMESTAMP = NEW_MESSAGE + ".timestamp";
        public static final String MESSAGE_IS_WRITING = NEW_MESSAGE + ".writing";
        public static final String WEBRTC_MESSAGE = BuildConfig.APPLICATION_ID + ".webRTCMessage";
        public static final String WEBRTC_MESSAGE_TYPE = WEBRTC_MESSAGE + ".type";
        public static final String WEBRTC_MESSAGE_CONTENT = WEBRTC_MESSAGE + ".content";
        public static final String DOWNLOADING_FILE = BuildConfig.APPLICATION_ID + ".downloadingFile";
        public static final String FILE_DOWNLOADED = BuildConfig.APPLICATION_ID + ".fileDownloaded";
    }

    public static final class MessageTypes {
        public static final String TEXT = "ninchat.com/text";
        public static final String FILE = "ninchat.com/file";
        public static final String WEBRTC_PREFIX = "ninchat.com/rtc/";
        public static final String ICE_CANDIDATE = WEBRTC_PREFIX + "ice-candidate";
        public static final String ANSWER = WEBRTC_PREFIX + "answer";
        public static final String OFFER = WEBRTC_PREFIX + "offer";
        public static final String CALL = WEBRTC_PREFIX + "call";
        public static final String PICK_UP = WEBRTC_PREFIX + "pick-up";
        public static final String HANG_UP = WEBRTC_PREFIX + "hang-up";
        public static final String WEBRTC_SERVERS_PARSED = WEBRTC_PREFIX + "serversParsed";
        public static final String RATING = "ninchat.com/metadata";

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

    public NinchatFile getFile(final String fileId) {
        return files.get(fileId);
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
        this.files = new HashMap<>();
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
    protected Map<String, NinchatFile> files;

    public void setConfiguration(final String config) throws JSONException {
        try {
            Log.v(TAG, "Got configuration: " + config);
            this.configuration = new JSONObject(config);
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
                    } else if (event.equals("queue_updated") || event.equals("audience_enqueued")) {
                        NinchatSessionManager.getInstance().queueUpdated(params);
                    } else if (event.equals("channel_joined")) {
                        NinchatSessionManager.getInstance().channelJoined(params);
                    } else if (event.equals("channel_updated")) {
                        NinchatSessionManager.getInstance().channelUpdated(params);
                    } else if (event.equals("message_received")) {
                        NinchatSessionManager.getInstance().messageReceived(params, payload);
                    } else if (event.equals("ice_begun")) {
                        NinchatSessionManager.getInstance().iceBegun(params);
                    } else if (event.equals("file_found")) {
                        NinchatSessionManager.getInstance().fileFound(params);
                    } else if (event.equals("channel_member_updated")) {
                        NinchatSessionManager.getInstance().memberUpdated(params);
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

    private NinchatQueue getQueue(final String queueId) {
        for (NinchatQueue queue : queues) {
            if (queue.getId().equals(queueId)) {
                return queue;
            }
        }
        return null;
    }

    private void queueUpdated(final Props params) {
        String queueId;
        try {
            queueId = params.getString("queue_id");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        long position;
        try {
            position = params.getInt("queue_position");
        } catch (final Exception e) {
            sessionError(e);
            return;
        }
        final NinchatQueue queue = getQueue(queueId);
        if (queue != null) {
            queue.setPosition(position);
        }
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Broadcast.CHANNEL_UPDATED));
        }
    }

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
        long actionId = 0;
        try {
            actionId = params.getInt("action_id");
        } catch (final Exception e) {
            return;
        }
        String messageType;
        try {
            messageType = params.getString("message_type");
        } catch (final Exception e) {
            return;
        }
        String sender = null;
        try {
            sender = params.getString("message_user_name");
        } catch (final Exception e) {
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
                            .putExtra(Broadcast.MESSAGE_SENDER, sender)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_CONTENT, builder.toString()));
            return;
        }
        if (!messageType.equals(MessageTypes.TEXT) && !messageType.equals(MessageTypes.FILE)) {
            return;
        }
        long timestamp = 0;
        try {
            timestamp = params.getInt("message_time");
        } catch (final Exception e) {
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
                    if (filetype != null && (filetype.startsWith("image/") ||
                            filetype.startsWith("video/") || filetype.equals("application/pdf"))) {
                        final String fileId = file.getString("file_id");
                        NinchatFile ninchatFile = this.files.get(fileId);
                        if (ninchatFile == null) {
                            ninchatFile = new NinchatFile(fileId, filename, filesize, filetype, timestamp, sender, actionId == 0);
                            this.files.put(fileId, ninchatFile);
                            NinchatDescribeFileTask.start(fileId);
                        } else if (false) {
                            // TODO: Check if the same file has been fetched already
                        }
                    }
                } else {
                    LocalBroadcastManager.getInstance(context)
                            .sendBroadcast(new Intent(Broadcast.NEW_MESSAGE)
                                    .putExtra(Broadcast.MESSAGE_CONTENT, message.getString("text"))
                                    .putExtra(Broadcast.MESSAGE_SENDER, sender)
                                    .putExtra(Broadcast.MESSAGE_IS_REMOTE, actionId == 0)
                                    .putExtra(Broadcast.MESSAGE_TIMESTAMP, timestamp));
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
        try {
            final Props attrs = params.getObject("file_attrs");
            final Props thumbnail = attrs.getObject("thumbnail");
            aspectRatio = ((float) thumbnail.getInt("width")) / ((float) thumbnail.getInt("height"));
        } catch (final Exception e) {
            aspectRatio = 16.0f / 9.0f;
        }
        final NinchatFile file = files.get(fileId);
        file.setUrl(url);
        file.setUrlExpiry(new Date(urlExpiry));
        file.setAspectRatio(aspectRatio);
        final Context context = contextWeakReference.get();
        if (context != null) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(Broadcast.NEW_MESSAGE)
                            .putExtra(Broadcast.MESSAGE_FILE_ID, fileId)
                            .putExtra(Broadcast.MESSAGE_SENDER, file.getSender())
                            .putExtra(Broadcast.MESSAGE_IS_REMOTE, file.isRemote())
                            .putExtra(Broadcast.MESSAGE_TIMESTAMP, file.getTimestamp()));
        }
    }

    private void memberUpdated(final Props params) {
        Props memberAttrs;
        try {
            memberAttrs = params.getObject("member_attrs");
        } catch (final Exception e) {
            return;
        }
        boolean writing;
        try {
            writing = memberAttrs.getBool("writing");
        } catch (final Exception e) {
            return;
        }
        final Context context = contextWeakReference.get();
        if (context != null && writing) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(Broadcast.NEW_MESSAGE)
                            .putExtra(Broadcast.MESSAGE_IS_WRITING, true));
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

    public void sendImage(final String name, final byte[] data) {
        NinchatSendFileTask.start(name, data, channelId);
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
            candidate.put("id", iceCandidate.sdpMLineIndex);
            candidate.put("label", iceCandidate.sdpMid);
            candidate.put("candidate", iceCandidate.sdp);
            data.put("candidate", candidate);
            NinchatSendMessageTask.start(MessageTypes.ICE_CANDIDATE, data.toString(), channelId);
        } catch (final JSONException e) {
            sessionError(e);
        }
    }

    public void sendRating(final int rating) {
        try {
            final JSONObject value = new JSONObject();
            value.put("rating", rating);
            final JSONObject data = new JSONObject();
            data.put("data", value);
            NinchatSendMessageTask.start(MessageTypes.RATING, data.toString(), channelId);
        } catch (final JSONException e) {
            // Ignore
        }
    }

    private JSONObject getDefault() throws JSONException {
        if (configuration != null) {
            return configuration.getJSONObject("default");
        }
        return null;
    }

    public String getRealmId() {
        try {
            return getDefault().getString("audienceRealmId");
        } catch (final Exception e) {
            return null;
        }
    }

    private Spanned toSpanned(final String text) {
        final String centeredText = (text == null || (text.contains("<center>") && text.contains("</center>"))) ?
                text : ("<center>" + text + "</center>");
        return centeredText == null ? null :
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? Html.fromHtml(centeredText, Html.FROM_HTML_MODE_LEGACY) : Html.fromHtml(centeredText);
    }

    public Spanned getWelcome() {
        final String key = "welcome";
        String welcomeText = key;
        try {
                welcomeText = getDefault().getString(key);
        } catch (final Exception e) {
        }
        return toSpanned(welcomeText);
    }

    public Spanned getNoQueues() {
        final String key = "noQueuesText";
        try {
            return toSpanned(getDefault().getString(key));
        } catch (final Exception e) {
            return toSpanned(key);
        }
    }

    private JSONObject getTranslations() throws JSONException, NullPointerException {
        return getDefault().getJSONObject("translations");
    }

    public boolean showNoThanksButton() {
        final String key = "noThanksButton";
        try {
            return getDefault().getBoolean(key);
        } catch (final Exception e) {
            return true;
        }
    }

    public String getCloseWindow() {
        final String key = "Close window";
        try {
            return getTranslations().getString(key);
        } catch (final Exception e) {
            return key;
        }
    }

    public String getUserName() {
        final String key = "userName";
        try {
            return getDefault().getString(key);
        } catch (final Exception e) {
            return key;
        }
    }

    public String getSendButtonText() {
        final String key = "sendButtonText";
        try {
            return getDefault().getString(key);
        } catch (final Exception e) {
            return null;
        }
    }

    public boolean isAttachmentsEnabled() {
        final String key = "supportFiles";
        try {
            return getDefault().getBoolean(key);
        } catch (final Exception e) {
            return false;
        }
    }

    public boolean isVideoEnabled() {
        final String key = "supportVideo";
        try {
            return getDefault().getBoolean(key);
        } catch (final Exception e) {
            return false;
        }
    }

    public Spanned getChatStarted() {
        final String key = "Audience in queue {{queue}} accepted.";
        try {
            return toSpanned(getTranslations().getString(key));
        } catch (final Exception e) {
            return toSpanned(key);
        }
    }

    public Spanned getChatEnded() {
        final String key = "Conversation ended";
        try {
            return toSpanned(getTranslations().getString(key));
        } catch (final Exception e) {
            return toSpanned(key);
        }
    }

    public String getCloseChat() {
        final String key = "Close chat";
        try {
            return getTranslations().getString(key);
        } catch (final Exception e) {
            return key;
        }
    }

    public String getEnterMessage() {
        final String key = "Enter your message";
        try {
            return getTranslations().getString(key);
        } catch (final Exception e) {
            return key;
        }
    }

    public Spanned getMOTD() {
        final String key = "motd";
        String motd = key;
        try {
            motd = getDefault().getString(key);
        } catch (final Exception e) {
        }
        return toSpanned(motd);
    }

    public String getQueueName(final String name) {
        final String key = "Join audience queue {{audienceQueue.queue_attrs.name}}";
        try {
            return getTranslations().getString(key).replaceFirst("\\{\\{([^}]*?)\\}\\}", name);
        } catch (final Exception e) {
            return key.replaceFirst("\\{\\{([^}]*?)\\}\\}", name);
        }
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
        String queueStatus = key;
        try {
            queueStatus = getTranslations().getString(key);
        } catch (final Exception e) {
        }
        final String[] splits = queueStatus.split("\\{\\{");
        if (splits.length > 2) {
            queueStatus = queueStatus.replaceFirst("\\{\\{([^}]*?)\\}\\}", selectedQueue.getName());
        }
        queueStatus = queueStatus.replaceFirst("\\{\\{([^}]*?)\\}\\}", String.valueOf(position));
        return toSpanned(queueStatus);
    }

    public Spanned getQueueMessage() {
        final String key = "inQueueText";
        String queueMessage = null;
        try {
            queueMessage = getDefault().getString(key);
        } catch (final Exception e) {
        }
        return toSpanned(queueMessage);
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
