package com.ninchat.sdk.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.managers.OrientationManager;
import com.ninchat.sdk.models.NinchatUser;
import com.ninchat.sdk.networkdispatchers.NinchatDeleteUser;
import com.ninchat.sdk.networkdispatchers.NinchatPartChannel;
import com.ninchat.sdk.networkdispatchers.NinchatSendFile;
import com.ninchat.sdk.networkdispatchers.NinchatSendMessage;
import com.ninchat.sdk.networkdispatchers.NinchatUpdateMember;
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes;
import com.ninchat.sdk.utils.misc.Broadcast;
import com.ninchat.sdk.utils.misc.Misc;
import com.ninchat.sdk.utils.misc.Parameter;
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler;
import com.ninchat.sdk.views.NinchatWebRTCView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends NinchatBaseActivity {

    static int REQUEST_CODE = NinchatChatActivity.class.hashCode() & 0xffff;

    protected static final int CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE = "WebRTCVideoAudio".hashCode() & 0xffff;
    protected static final int PICK_PHOTO_VIDEO_REQUEST_CODE = "PickPhotoVideo".hashCode() & 0xffff;
    private OrientationManager orientationManager;
    private boolean historyLoaded = false;
    private int rootViewHeight = 0;

    private NinchatMessageAdapter messageAdapter = sessionManager != null ? sessionManager.getMessageAdapter() : new NinchatMessageAdapter();

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_chat;
    }

    protected void quit(Intent data) {
        if (data == null) {
            data = new Intent();
        }
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatReviewActivity.REQUEST_CODE) {
            // coming from ninchat review
            if (sessionManager != null &&
                    sessionManager.ninchatState.getNinchatQuestionnaire() != null &&
                    !sessionManager.ninchatState.getNinchatQuestionnaire().hasPostAudienceQuestionnaire()) {
                NinchatPartChannel.executeAsync(
                        NinchatScopeHandler.getIOScope(),
                        sessionManager.getSession(),
                        sessionManager.ninchatState.getChannelId(),
                        aLong -> null
                );
                // delete the user if current user is a guest
                if (NinchatSessionManager.getInstance().isGuestMemeber()) {
                    NinchatDeleteUser.executeAsync(
                            NinchatScopeHandler.getIOScope(),
                            sessionManager.getSession(),
                            aLong -> null
                    );
                }
            }
            quit(data);
        } else if (requestCode == NinchatChatActivity.PICK_PHOTO_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                final Uri uri = data.getData();
                String fileName = getFileName(uri);
                final InputStream inputStream = getContentResolver().openInputStream(uri);
                final int size = inputStream.available();
                final byte[] buffer = new byte[size];
                inputStream.read(buffer);
                inputStream.close();
                NinchatSendFile.executeAsync(
                        NinchatScopeHandler.getIOScope(),
                        sessionManager.getSession(),
                        sessionManager.ninchatState.getChannelId(),
                        fileName,
                        buffer,
                        aLong -> null
                );

            } catch (final Exception e) {
                sessionManager.sessionError(e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getFileName(Uri uri) {
        final Cursor cursor = getContentResolver()
                .query(uri, null, null, null, null, null);
        String displayName = "";
        try {
            if (cursor != null && cursor.moveToFirst()) {
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return displayName;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE) {
            if (hasVideoCallPermissions()) {
                sendPickUpAnswer(true);
            } else {
                sendPickUpAnswer(false);
                showError(R.id.ninchat_chat_error, R.string.ninchat_chat_error_no_video_call_permissions);

            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (hasFileAccessPermissions()) {
                openImagePicker(null);
                //findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.VISIBLE);
            } else {
                showError(R.id.ninchat_chat_error, R.string.ninchat_chat_error_no_file_permissions);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected boolean chatClosed = false;

    protected BroadcastReceiver channelClosedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Broadcast.CHANNEL_CLOSED.equals(action)) {
                messageAdapter.close(NinchatChatActivity.this);
                chatClosed = true;
                hideKeyboard();
            }
        }
    };

    protected BroadcastReceiver transferReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Broadcast.AUDIENCE_ENQUEUED.equals(action)) {
                NinchatPartChannel.executeAsync(
                        NinchatScopeHandler.getIOScope(),
                        sessionManager.getSession(),
                        sessionManager.ninchatState.getChannelId(),
                        aLong -> null
                );
                quit(intent);
            }
        }
    };

    public void onCloseChat(final View view) {
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.NinchatTheme_Dialog)
                .setView(R.layout.dialog_close_chat)
                .setCancelable(true)
                .create();
        dialog.show();
        final TextView title = dialog.findViewById(R.id.ninchat_close_chat_dialog_title);
        final String closeText = sessionManager.ninchatState.getSiteConfig().getChatCloseText();
        title.setText(closeText);
        final TextView description = dialog.findViewById(R.id.ninchat_close_chat_dialog_description);
        description.setText(sessionManager.ninchatState.getSiteConfig().getChatCloseConfirmationText());
        final Button confirm = dialog.findViewById(R.id.ninchat_close_chat_dialog_confirm);
        confirm.setText(closeText);
        confirm.setOnClickListener(v -> {
            chatClosed();
            dialog.dismiss();
        });
        final Button decline = dialog.findViewById(R.id.ninchat_close_chat_dialog_decline);
        final String continueChatText = sessionManager.ninchatState.getSiteConfig().getContinueChatText();
        decline.setText(continueChatText);
        decline.setOnClickListener(v -> dialog.dismiss());
        hideKeyboard();
        if (chatClosed) {
            dialog.dismiss();
            chatClosed();
        }
    }

    public void chatClosed() {
        onVideoHangUp(null);
        if (sessionManager == null) sessionManager = NinchatSessionManager.getInstance();
        final boolean showRatings = sessionManager.ninchatState.getSiteConfig().showRating();
        // sessionManager.partChannel();
        if (showRatings) {
            startActivityForResult(NinchatReviewActivity.getLaunchIntent(NinchatChatActivity.this), NinchatReviewActivity.REQUEST_CODE);
        } else {
            quit(null);
        }
    }

    private boolean hasVideoCallPermissions() {
        return checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                checkCallingOrSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendPickUpAnswer(final boolean answer) {
        try {
            final JSONObject data = new JSONObject();
            data.put("answer", answer);
            NinchatSendMessage.executeAsync(
                    NinchatScopeHandler.getIOScope(),
                    sessionManager.getSession(),
                    sessionManager.ninchatState.getChannelId(),
                    NinchatMessageTypes.PICK_UP,
                    data.toString(),
                    aLong -> null
            );
        } catch (final JSONException e) {
            sessionManager.sessionError(e);
        }
        final String metaMessage = answer ? sessionManager.ninchatState.getSiteConfig().getVideoCallAcceptedText() :
                sessionManager.ninchatState.getSiteConfig().getVideoCallRejectedText();
        messageAdapter.addMetaMessage(messageAdapter.getLastMessageId(true) + "answer", Misc.center(metaMessage));
    }

    private View videoContainer;
    protected NinchatWebRTCView webRTCView;

    protected BroadcastReceiver webRTCMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Broadcast.WEBRTC_MESSAGE.equals(action)) {
                final String messageType = intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_TYPE);
                if (NinchatMessageTypes.CALL.equals(messageType)) {
                    final AlertDialog dialog = new AlertDialog.Builder(NinchatChatActivity.this, R.style.NinchatTheme_Dialog)
                            .setView(R.layout.dialog_video_call_consent)
                            .setCancelable(false)
                            .create();
                    dialog.show();
                    final TextView title = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_title);
                    title.setText(sessionManager.ninchatState.getSiteConfig().getVideoChatTitleText());
                    final ImageView userImage = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_user_avatar);
                    final NinchatUser user = sessionManager.getMember(intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_SENDER));
                    String avatar = user.getAvatar();
                    if (TextUtils.isEmpty(avatar)) {
                        avatar = sessionManager.ninchatState.getSiteConfig().getAgentAvatar();
                    }
                    if (!TextUtils.isEmpty(avatar)) {
                        GlideApp.with(userImage.getContext())
                                .load(avatar)
                                .circleCrop()
                                .into(userImage);
                    }
                    final TextView userName = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_user_name);
                    userName.setText(user.getName());
                    final TextView description = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_description);
                    description.setText(sessionManager.ninchatState.getSiteConfig().getVideoChatDescriptionText());
                    final Button accept = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_accept);
                    accept.setText(sessionManager.ninchatState.getSiteConfig().getVideoCallAcceptText(
                    ));
                    accept.setOnClickListener(v -> {
                        dialog.dismiss();
                        if (hasVideoCallPermissions()) {
                            sendPickUpAnswer(true);
                        } else {
                            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO}, CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE);
                        }
                    });
                    final Button decline = dialog.findViewById(R.id.ninchat_video_call_consent_dialog_decline);
                    decline.setText(sessionManager.ninchatState.getSiteConfig().getVideoCallDeclineText());
                    decline.setOnClickListener(v -> {
                        sendPickUpAnswer(false);
                        dialog.dismiss();
                    });
                    hideKeyboard();
                    messageAdapter.addMetaMessage(intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_ID), sessionManager.ninchatState.getSiteConfig().getVideoCallMetaMessageText());
                } else if (webRTCView.handleWebRTCMessage(messageType, intent.getStringExtra(Broadcast.WEBRTC_MESSAGE_CONTENT))) {
                    if (NinchatMessageTypes.HANG_UP.equals(messageType)) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }
                }
            }
        }
    };

    private void hideKeyboard() {
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (final Exception e) {
            // Ignore
        }
    }

    public void onVideoHangUp(final View view) {
        hangUp();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    public void onToggleFullScreen(final View view) {

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public void onToggleAudio(final View view) {
        webRTCView.toggleAudio();
    }

    public void onToggleMicrophone(final View view) {
        webRTCView.toggleMicrophone();
    }

    public void onToggleVideo(final View view) {
        webRTCView.toggleVideo();
    }

    public void onVideoCall(final View view) {
        if (chatClosed) {
            return;
        }
        webRTCView.call();
    }

    public void onAttachmentClick(final View view) {
        if (chatClosed) {
            return;
        }
        if (hasFileAccessPermissions()) {
            openImagePicker(view);
            /*final Intent photos = new Intent(Intent.ACTION_PICK).setType("image/*");
            final Intent videos = new Intent(Intent.ACTION_PICK).setType("video/*");
            final Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            final List<Intent> openIntents = new ArrayList<>();
            addIntentToList(openIntents, photos);
            addIntentToList(openIntents, camera);
            if (openIntents.size() > 0) {
                final Intent chooserIntent = Intent.createChooser(openIntents.remove(openIntents.size() -1), "foo");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, openIntents.toArray(new Parcelable[]{}));
                startActivityForResult(chooserIntent, PICK_PHOTO_VIDEO_REQUEST_CODE);
            }*/
            //findViewById(R.id.ninchat_chat_file_picker_dialog).setVisibility(View.VISIBLE);
        } else {
            requestFileAccessPermissions();
        }
    }

    private void addIntentToList(final List<Intent> intents, final Intent intent) {
        for (final ResolveInfo resInfo : getPackageManager().queryIntentActivities(intent, 0)) {
            String packageName = resInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            intents.add(targetedIntent);
        }
    }

    public void openImagePicker(final View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_PHOTO_VIDEO_REQUEST_CODE);
    }

    public void onEditTextClick(final View view) {
        final EditText editText = findViewById(R.id.message);
        if (editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void onSendClick(final View view) {
        if (chatClosed) {
            return;
        }
        final TextView messageView = findViewById(R.id.message);
        final String message = messageView.getText().toString();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        try {
            final JSONObject data = new JSONObject();
            data.put("text", message);
            NinchatSendMessage.executeAsync(
                    NinchatScopeHandler.getIOScope(),
                    sessionManager.getSession(),
                    sessionManager.ninchatState.getChannelId(),
                    NinchatMessageTypes.TEXT,
                    data.toString(),
                    aLong -> null
            );
        } catch (final JSONException e) {
            sessionManager.sessionError(e);
        }

        writingMessageSent = false;
        messageView.setText(null);
    }

    private boolean writingMessageSent = false;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() != 0 && !writingMessageSent) {
                NinchatUpdateMember.executeAsync(
                        NinchatScopeHandler.getIOScope(),
                        sessionManager.getSession(),
                        sessionManager.ninchatState.getChannelId(),
                        sessionManager.ninchatState.getUserId(),
                        true,
                        aLong -> null
                );
                writingMessageSent = true;
            } else if (s.length() == 0) {
                NinchatUpdateMember.executeAsync(
                        NinchatScopeHandler.getIOScope(),
                        sessionManager.getSession(),
                        sessionManager.ninchatState.getChannelId(),
                        sessionManager.ninchatState.getUserId(),
                        false,
                        aLong -> null
                );
                writingMessageSent = false;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.ninchat_chat_background_not_tiled)) {
            findViewById(R.id.ninchat_chat_root).setBackgroundResource(R.drawable.ninchat_chat_background);
        }

        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialzed again
        if (sessionManager == null) {
            setResult(Activity.RESULT_CANCELED, null);
            finish();
            this.overridePendingTransition(0, 0);
            return;
        }

        orientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_UI);
        orientationManager.enable();

        videoContainer = findViewById(R.id.videoContainer);
        webRTCView = new NinchatWebRTCView(videoContainer);
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(channelClosedReceiver, new IntentFilter(Broadcast.CHANNEL_CLOSED));
        localBroadcastManager.registerReceiver(transferReceiver, new IntentFilter(Broadcast.AUDIENCE_ENQUEUED));
        localBroadcastManager.registerReceiver(webRTCMessageReceiver, new IntentFilter(Broadcast.WEBRTC_MESSAGE));
        final RecyclerView messages = findViewById(R.id.message_list);
        messages.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        messages.setAdapter(messageAdapter);
        final EditText message = findViewById(R.id.message);
        final String enterMessageText = sessionManager.ninchatState.getSiteConfig().getEnterMessageText();
        message.setHint(enterMessageText);
        message.addTextChangedListener(textWatcher);
        final Button closeButton = findViewById(R.id.ninchat_chat_close);
        final String closeText = sessionManager.ninchatState.getSiteConfig().getChatCloseText();
        closeButton.setText(closeText);
        final String sendButtonText = sessionManager.ninchatState.getSiteConfig().getSendButtonText(
        );
        final Button sendButton = findViewById(R.id.send_button);
        final RelativeLayout sendIcon = findViewById(R.id.send_button_icon);
        if (sendButtonText != null) {
            sendButton.setText(sendButtonText);
        } else {
            sendButton.setVisibility(View.GONE);
            sendIcon.setVisibility(View.VISIBLE);
        }
        if (sessionManager.ninchatState.getSiteConfig().isAttachmentsEnabled()) {
            findViewById(R.id.attachment).setVisibility(View.VISIBLE);
        }
        if (sessionManager.ninchatState.getSiteConfig().isVideoEnabled() && getResources().getBoolean(R.bool.ninchat_allow_user_initiated_video_calls)) {
            findViewById(R.id.video_call).setVisibility(View.VISIBLE);
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Parameter.CHAT_IS_CLOSED)) {
            initializeClosedChat(messages);
        }

        // Set up a soft keyboard visibility listener so video call container height can be adjusted
        setRootViewHeightListener();
    }

    private void initializeClosedChat(RecyclerView messages) {

        // Wait for RecyclerView to be initialized
        messages.getViewTreeObserver().addOnGlobalLayoutListener(() -> {

            // Close chat if it hasn't been closed yet
            if (!chatClosed && historyLoaded) {
                messageAdapter.close(NinchatChatActivity.this);
                chatClosed = true;
                hideKeyboard();
            }

            // Initialize closed chat with recent messages only
            if (!historyLoaded) {
                sessionManager.loadChannelHistory(null);
                historyLoaded = true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the message list, just in case
        messageAdapter.notifyDataSetChanged();

        if (webRTCView != null && sessionManager != null) {
            webRTCView.onResume();
        }

        // Don't load first messages if chat is closed, we want to load the latest messages only
        if (getIntent().getExtras() == null || !(getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Parameter.CHAT_IS_CLOSED))) {

            if (sessionManager != null) {
                sessionManager.loadChannelHistory(messageAdapter.getLastMessageId(false));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (webRTCView != null && sessionManager != null) {
            webRTCView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hangUp();
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(channelClosedReceiver);
        localBroadcastManager.unregisterReceiver(transferReceiver);
        localBroadcastManager.unregisterReceiver(webRTCMessageReceiver);

        if (orientationManager != null) {
            orientationManager.disable();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        webRTCView.onPause();
        final ViewGroup.LayoutParams layoutParams = videoContainer.getLayoutParams();
        final ImageView image = findViewById(R.id.fullscreen_on_off);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            image.setImageResource(R.drawable.ninchat_icon_video_toggle_normal);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutParams.height = (int) getResources().getDimension(R.dimen.ninchat_chat_activity_video_view_height);
            image.setImageResource(R.drawable.ninchat_icon_video_toggle_full);
        }
        videoContainer.setLayoutParams(layoutParams);

        // Update pip video orientation
        final View pip = videoContainer.findViewById(R.id.pip_video);
        final ViewGroup.LayoutParams pipLayoutParams = pip.getLayoutParams();
        pipLayoutParams.height = getResources().getDimensionPixelSize(R.dimen.ninchat_chat_activity_pip_video_height);
        pipLayoutParams.width = getResources().getDimensionPixelSize(R.dimen.ninchat_chat_activity_pip_video_width);
        pip.setLayoutParams(pipLayoutParams);

        webRTCView.onResume();
        messageAdapter.scrollToBottom(true);
    }

    // Reinitialize webRTC on hangup for possible new connection
    private void hangUp() {
        if (sessionManager != null && webRTCView != null) {
            webRTCView.hangUp();
            webRTCView = new NinchatWebRTCView(videoContainer);
        }
    }

    // Listen for changes in root view height so we can determine when soft keyboard is visible
    public void setRootViewHeightListener() {
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);

        // Set initial rootViewHeight
        if (rootViewHeight == 0) {
            rootViewHeight = activityRootView.getHeight();
        }

        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            final ViewGroup.LayoutParams layoutParams = videoContainer.getLayoutParams();

            // if new height differs from the cached one, keyboard visibility has changed
            int heightDiff = activityRootView.getHeight() - rootViewHeight;

            // Keyboard hidden
            if (heightDiff > 0 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutParams.height = (int) getResources().getDimension(R.dimen.ninchat_chat_activity_video_view_height);
                // Update video height and cache current rootview height
                videoContainer.setLayoutParams(layoutParams);
            }
            // Keyboard visible
            else if (heightDiff < 0 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutParams.height = (int) getResources().getDimension(R.dimen.ninchat_chat_activity_video_view_height_small);
                // Update video height and cache current rootview height
                videoContainer.setLayoutParams(layoutParams);
                // push messages on top of soft keyboard
                messageAdapter.scrollToBottom(true);
            }
            rootViewHeight = activityRootView.getHeight();
        });
    }
}
