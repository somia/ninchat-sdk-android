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
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.managers.IOrientationManager;
import com.ninchat.sdk.ninchatchatactivity.model.NinchatChatModel;
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter;
import com.ninchat.sdk.ninchatchatactivity.view.NinchatChatBroadcastManager;
import com.ninchat.sdk.ninchatchatactivity.view.SoftKeyboardViewHandler;
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel;
import com.ninchat.sdk.ninchatreview.presenter.NinchatReviewPresenter;
import com.ninchat.sdk.ninchattitlebar.view.NinchatTitlebarView;
import com.ninchat.sdk.ninchatvideointegrations.p2p.NinchatP2PIntegration;
import com.ninchat.sdk.utils.misc.NinchatLinearLayoutManager;
import com.ninchat.sdk.utils.misc.Misc;
import com.ninchat.sdk.utils.misc.Parameter;

import static com.ninchat.sdk.ninchattitlebar.model.NinchatTitlebarKt.shouldShowTitlebar;
import static com.ninchat.sdk.utils.keyboard.NinchatKeyboardKt.hideKeyBoardForce;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatChatActivity extends NinchatBaseActivity implements IOrientationManager {

    private NinchatP2PIntegration p2pIntegration;
    private final NinchatChatModel mChatModel = new NinchatChatModel();
    private final NinchatChatPresenter presenter = new NinchatChatPresenter(mChatModel);
    private final NinchatChatBroadcastManager mBroadcastManager = new NinchatChatBroadcastManager(
            NinchatChatActivity.this,
            () -> {
                mChatModel.setChatClosed(true);
                hideKeyBoardForce(NinchatChatActivity.this);
                return null;
            },
            intent -> {
                quit(intent);
                return null;
            },
            intent -> {
                p2pIntegration.maybeHandleP2PVideoCallInvitation(intent, NinchatChatActivity.this);
                p2pIntegration.mayBeHandleWebRTCMessages(intent, NinchatChatActivity.this);
                return null;
            }
    );
    private final SoftKeyboardViewHandler mSoftKeyboardViewHandler = new SoftKeyboardViewHandler(
            // onHidden
            () -> {
                // Update video height and cache current rootview height
                this.p2pIntegration.setLayoutParams((int) getResources().getDimension(R.dimen.ninchat_chat_activity_video_view_height), -1);
                return null;
            },
            // onShow
            () -> {
                // Update video height and cache current rootview height
                this.p2pIntegration.setLayoutParams((int) getResources().getDimension(R.dimen.ninchat_chat_activity_video_view_height_small), -1);
                // push messages on top of soft keyboard
                if (NinchatSessionManager.getInstance() != null) {
                    NinchatSessionManager.getInstance().getOnInitializeMessageAdapter(adapter -> adapter.scrollToBottom(true));
                }
                return null;
            }

    );

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
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        if (requestCode == NinchatReviewModel.REQUEST_CODE) {
            // coming from ninchat review
            presenter.onActivityClose();
            quit(data);
        } else if (requestCode == NinchatChatPresenter.PICK_PHOTO_VIDEO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            p2pIntegration.onAlbumSelected(data, getApplicationContext());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == NinchatChatPresenter.CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE) {
            if (p2pIntegration.hasVideoCallPermissions()) {
                p2pIntegration.sendPickUpAnswer(true);
            } else {
                p2pIntegration.sendPickUpAnswer(false);
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

    public void onCloseChat(final View view) {
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
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
        hideKeyBoardForce(NinchatChatActivity.this);
        if (mChatModel.getChatClosed()) {
            dialog.dismiss();
            chatClosed();
        }
    }

    public void chatClosed() {
        onVideoHangUp(null);
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        final boolean showRatings = sessionManager.ninchatState.getSiteConfig().showRating();
        if (showRatings) {
            startActivityForResult(NinchatReviewPresenter.getLaunchIntent(NinchatChatActivity.this), NinchatReviewModel.REQUEST_CODE);
        } else {
            quit(null);
        }
    }

    public void onVideoHangUp(final View view) {
        hangUp();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    public void onToggleFullScreen(final View view) {
        mChatModel.setToggleFullScreen(!mChatModel.getToggleFullScreen());
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        p2pIntegration.handleTitlebarView(false, this);
    }

    public void onToggleAudio(final View view) {
        p2pIntegration.toggleAudio();
    }

    public void onToggleMicrophone(final View view) {
        p2pIntegration.toggleMicrophone();
    }

    public void onToggleVideo(final View view) {
        p2pIntegration.toggleVideo();
    }

    public void onVideoCall(final View view) {
        if (mChatModel.getChatClosed()) {
            return;
        }
        p2pIntegration.call();
    }

    public void onAttachmentClick(final View view) {
        if (mChatModel.getChatClosed()) {
            return;
        }
        if (hasFileAccessPermissions()) {
            openImagePicker(view);
        } else {
            requestFileAccessPermissions();
        }
    }

    public void openImagePicker(final View view) {
        startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), NinchatChatPresenter.PICK_PHOTO_VIDEO_REQUEST_CODE);
    }

    public void onEditTextClick(final View view) {
        final EditText editText = findViewById(R.id.message);
        if (editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void onSendClick(final View view) {
        presenter.sendMessage(findViewById(R.id.send_message_container));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialzed again
        if (sessionManager == null) {
            setResult(Activity.RESULT_CANCELED, null);
            finish();
            this.overridePendingTransition(0, 0);
            return;
        }

        if (getResources().getBoolean(R.bool.ninchat_chat_background_not_tiled)) {
            findViewById(R.id.ninchat_chat_root).setBackgroundResource(sessionManager.getNinchatChatBackground());
        } else {
            Drawable background = Misc.getNinchatChatBackground(getApplicationContext(), sessionManager.getNinchatChatBackground());
            if (background != null)
                findViewById(R.id.ninchat_chat_root).setBackground(background);
        }

        // start with orientation toggled false
        mChatModel.setToggleFullScreen(false);
        presenter.initialize(this, this);
        p2pIntegration = new NinchatP2PIntegration(findViewById(R.id.videoContainer));
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastManager.register(localBroadcastManager);
        final RecyclerView messages = findViewById(R.id.message_list);
        final NinchatLinearLayoutManager linearLayoutManager = new NinchatLinearLayoutManager(getApplicationContext());
        messages.setLayoutManager(linearLayoutManager);
        sessionManager.getOnInitializeMessageAdapter(messages::setAdapter);
        final EditText message = findViewById(R.id.message);
        final String enterMessageText = sessionManager.ninchatState.getSiteConfig().getEnterMessageText();
        message.setHint(enterMessageText);
        message.addTextChangedListener(presenter.getTextWatcher());
        presenter.getWritingIndicator().initiate();
        final Button closeButton = findViewById(R.id.ninchat_chat_close);
        final String closeText = sessionManager.ninchatState.getSiteConfig().getChatCloseText();
        closeButton.setText(closeText);
        if (!shouldShowTitlebar())
            closeButton.setVisibility(View.VISIBLE);


        final String sendButtonText = sessionManager.ninchatState.getSiteConfig().getSendButtonText();
        final Button sendButton = findViewById(R.id.send_button);
        final RelativeLayout sendIcon = findViewById(R.id.send_button_icon);
        if (sendButtonText != null) {
            sendButton.setText(sendButtonText);
        } else {
            sendButton.setVisibility(View.GONE);
            sendIcon.setVisibility(View.VISIBLE);
        }
        if (sessionManager.ninchatSessionHolder.supportFiles()) {
            findViewById(R.id.attachment).setVisibility(View.VISIBLE);
        }
        if (sessionManager.ninchatSessionHolder.supportVideos() && getResources().getBoolean(R.bool.ninchat_allow_user_initiated_video_calls)) {
            findViewById(R.id.video_call).setVisibility(View.VISIBLE);
        }

        if (getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Parameter.CHAT_IS_CLOSED)) {
            initializeClosedChat(messages);
        } else {
            initializeChat(messages);
        }
        NinchatTitlebarView.Companion.showTitlebarForBacklog(
                findViewById(R.id.ninchat_chat_root).findViewById(R.id.ninchat_titlebar),
                () -> {
                    onCloseChat(null);
                    return null;
                });
        // Set up a soft keyboard visibility listener so video call container height can be adjusted
        mSoftKeyboardViewHandler.register(getWindow().getDecorView().findViewById(android.R.id.content));
    }

    private void initializeClosedChat(RecyclerView messages) {
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();

        // Wait for RecyclerView to be initialized
        messages.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // Close chat if it hasn't been closed yet
            if (!mChatModel.getChatClosed() && mChatModel.getHistoryLoaded()) {
                sessionManager.getOnInitializeMessageAdapter(adapter -> adapter.close(NinchatChatActivity.this));
                mChatModel.setChatClosed(true);
                hideKeyBoardForce(NinchatChatActivity.this);
            }

            // Initialize closed chat with recent messages only
            if (!mChatModel.getHistoryLoaded()) {
                sessionManager.loadChannelHistory(null);
                mChatModel.setHistoryLoaded(true);
            }
        });
    }

    private void initializeChat(RecyclerView messages) {
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();

        // Wait for RecyclerView to be initialized
        messages.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!mChatModel.getChatClosed()) {
                sessionManager.getOnInitializeMessageAdapter(adapter -> adapter.removeChatCloseMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the message list, just in case
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        if (sessionManager == null) return;
        sessionManager.getOnInitializeMessageAdapter(adapter -> adapter.notifyDataSetChanged());
        // Don't load first messages if chat is closed, we want to load the latest messages only
        if (getIntent().getExtras() == null || !(getIntent().getExtras() != null && getIntent().getExtras().getBoolean(Parameter.CHAT_IS_CLOSED))) {
            sessionManager.getOnInitializeMessageAdapter(adapter -> {
                sessionManager.loadChannelHistory(adapter.getLastMessageId(false));
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        hangUp();
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        mBroadcastManager.unregister(localBroadcastManager);
        mSoftKeyboardViewHandler.unregister();

        presenter.getWritingIndicator().dispose();
        presenter.getOrientationManager().disable();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (p2pIntegration != null)
            p2pIntegration.onConfigurationChanges(newConfig);
    }

    // Reinitialize webRTC on hangup for possible new connection
    private void hangUp() {
        mChatModel.setToggleFullScreen(false);
        p2pIntegration.handleTitlebarView(true, this);
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        if (sessionManager != null && p2pIntegration != null) {
            p2pIntegration.hangUp();
        }
    }


    @Override
    public void onOrientationChange(int orientation) {
        this.presenter.handleOrientationChange(orientation, this);
        p2pIntegration.handleTitlebarView(false, this);
    }
}
