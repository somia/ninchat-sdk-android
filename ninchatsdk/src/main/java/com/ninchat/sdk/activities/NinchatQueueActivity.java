package com.ninchat.sdk.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.networkdispatchers.NinchatDeleteUser;
import com.ninchat.sdk.utils.misc.Misc;
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatQueueActivity extends NinchatBaseActivity {

    public static final int REQUEST_CODE = NinchatQueueActivity.class.hashCode() & 0xffff;

    protected final static String QUEUE_ID = "queueId";

    public static Intent getLaunchIntent(final Context context, final String queueId) {
        return new Intent(context, NinchatQueueActivity.class).putExtra(QUEUE_ID, queueId);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_queue;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatChatActivity.REQUEST_CODE) {
            // check data is null or not. Can through exception
            final String queueId = data == null ? null : data.getStringExtra(NinchatSessionManager.Parameter.QUEUE_ID);
            if (queueId == null) {
                setResult(RESULT_OK, data);
                finish();
            } else {
                this.queueId = queueId;
                updateQueueStatus();
            }
        }
    }

    protected BroadcastReceiver channelJoinedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.CHANNEL_JOINED.equals(action)) {
                Intent i = new Intent(NinchatQueueActivity.this, NinchatChatActivity.class);

                if (intent.getExtras() != null && intent.getExtras().getBoolean(NinchatSessionManager.Parameter.CHAT_IS_CLOSED)) {
                    i.putExtra(NinchatSessionManager.Parameter.CHAT_IS_CLOSED, intent.getExtras().getBoolean(NinchatSessionManager.Parameter.CHAT_IS_CLOSED));
                }

                startActivityForResult(i, NinchatChatActivity.REQUEST_CODE);
            }
        }
    };

    protected BroadcastReceiver channelUpdatedBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.QUEUE_UPDATED.equals(action)) {
                updateQueueStatus();
            }
        }
    };

    private String queueId;

    private void updateQueueStatus() {
        final TextView queueStatus = findViewById(R.id.ninchat_queue_activity_queue_status);
        final TextView message = findViewById(R.id.ninchat_queue_activity_queue_message);
        final Button closeButton = findViewById(R.id.ninchat_queue_activity_close_button);

        if (sessionManager != null && sessionManager.hasChannel()) {
            queueStatus.setVisibility(View.INVISIBLE);
        } else {
            queueStatus.setVisibility(View.VISIBLE);
            message.setVisibility(View.VISIBLE);
            closeButton.setVisibility(View.VISIBLE);
            queueStatus.setText(sessionManager.getQueueStatus(queueId));
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialzed again
        if (sessionManager == null) {
            setResult(Activity.RESULT_CANCELED, null);
            finish();
            this.overridePendingTransition(0, 0);
            return;
        }

        if (intent != null) {
            queueId = intent.getStringExtra(QUEUE_ID);
        }

        final RotateAnimation animation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setDuration(3000);
        findViewById(R.id.ninchat_queue_activity_progress).setAnimation(animation);
        final TextView message = findViewById(R.id.ninchat_queue_activity_queue_message);
        final Button closeButton = findViewById(R.id.ninchat_queue_activity_close_button);

        if (message != null) {
            final String inQueueText = sessionManager.
                    getNinchatSiteConfig().getInQueueMessageText();
            message.setText(Misc.toSpanned(inQueueText));
        }

        if (closeButton != null) {
            final String closeText = sessionManager.getNinchatSiteConfig().getChatCloseText();
            closeButton.setText(closeText);
        }

        if (!sessionManager.hasChannel()) {
            if (message != null) {
                message.setVisibility(View.VISIBLE);
            }
            if (closeButton != null) {
                closeButton.setVisibility(View.VISIBLE);
            }
        } else {
            if (message != null)
                message.setVisibility(View.INVISIBLE);
            if (closeButton != null)
                closeButton.setVisibility(View.INVISIBLE);
        }
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(channelJoinedBroadcastReceiver, new IntentFilter(NinchatSessionManager.Broadcast.CHANNEL_JOINED));
        broadcastManager.registerReceiver(channelUpdatedBroadcastReceiver, new IntentFilter(NinchatSessionManager.Broadcast.QUEUE_UPDATED));
        NinchatSessionManager.joinQueue(queueId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(channelJoinedBroadcastReceiver);
        broadcastManager.unregisterReceiver(channelUpdatedBroadcastReceiver);
    }

    public void onClose(final View view) {
        NinchatDeleteUser.executeAsync(
                NinchatScopeHandler.getIOScope(),
                NinchatSessionManager.getInstance().getSession(),
                aLong -> null
        );
        setResult(RESULT_CANCELED, null);
        finish();
    }
}
