package com.ninchat.sdk.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatQueueListAdapter;
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel;
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter;
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity;
import com.ninchat.sdk.utils.misc.Misc;

import static com.ninchat.sdk.activities.NinchatQuestionnaireActivity.OPEN_QUEUE;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public final class NinchatActivity extends NinchatBaseActivity {

    protected static final String QUEUE_ID = "queueId";
    private final int TRANSITION_DELAY = 300;

    public static Intent getLaunchIntent(final Context context, final String queueId) {
        return new Intent(context, NinchatActivity.class)
                .putExtra(QUEUE_ID, queueId);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat;
    }

    protected String queueId;

    protected BroadcastReceiver queuesUpdatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSession.Broadcast.QUEUES_UPDATED.equals(action)) {
                setQueueAdapter();
            }
        }
    };

    private void setQueueAdapter() {
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        final RecyclerView queueList = (RecyclerView) findViewById(R.id.ninchat_activity_queue_list);
        final NinchatQueueListAdapter ninchatQueueListAdapter = sessionManager.getNinchatQueueListAdapter(NinchatActivity.this);
        queueList.setAdapter(ninchatQueueListAdapter);
        if (ninchatQueueListAdapter.getItemCount() == 0) {
            findViewById(R.id.ninchat_activity_no_queues).setVisibility(View.VISIBLE);
            final TextView motd = findViewById(R.id.ninchat_activity_motd);
            final String motDText = sessionManager
                    .ninchatState.getSiteConfig()
                    .getMOTDText();
            motd.setText(Misc.toRichText(motDText, motd));
            findViewById(R.id.ninchat_activity_close).setVisibility(View.VISIBLE);
        }
        ninchatQueueListAdapter.notifyDataSetChanged();
    }

    protected BroadcastReceiver configurationFetchedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NinchatSession.Broadcast.CONFIGURATION_FETCHED.equals(intent.getAction())) {
                setTexts();
            }
        }
    };

    private void setTexts() {
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        final String welcomeMessage = sessionManager
                .ninchatState.getSiteConfig()
                .getWelcomeText();

        final String noQueueText = sessionManager
                .ninchatState.getSiteConfig()
                .getNoQueuesText();


        final String motDText = sessionManager
                .ninchatState.getSiteConfig()
                .getMOTDText();

        final String closeWindowText = sessionManager
                .ninchatState.getSiteConfig()
                .getCloseWindowText();

        final TextView topHeader = findViewById(R.id.ninchat_activity_header);
        topHeader.setText(Misc.toRichText(welcomeMessage, topHeader));
        final Button closeButton = findViewById(R.id.ninchat_activity_close);
        closeButton.setText(closeWindowText);
        closeButton.setVisibility(View.VISIBLE);
        final TextView motd = findViewById(R.id.ninchat_activity_motd);
        motd.setText(Misc.toRichText(motDText, motd));
        final TextView noQueues = findViewById(R.id.ninchat_activity_no_queues);
        noQueues.setText(Misc.toRichText(noQueueText, noQueues));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
        // If the app is killed in the background sessionManager is not initialized the SDK must
        // be exited and the NinchatSession needs to be initialzed again
        if (sessionManager == null) {
            // Use a small delay before transition for UX purposes. Without the delay the app looks
            // like it's crashing since there can be 3 activities that will be finished.
            new android.os.Handler().postDelayed(() -> {
                setResult(Activity.RESULT_CANCELED, null);
                finish();
                // Use a slide transition just to minimize the impression that the app has crashed
                this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }, TRANSITION_DELAY);

            return;
        }
        if (intent != null) {
            queueId = intent.getStringExtra(QUEUE_ID);
        }
        if (queueId != null) {
            final NinchatSessionManager ninchatSessionManager = NinchatSessionManager.getInstance();
            if (ninchatSessionManager.ninchatState.getNinchatQuestionnaire() != null &&
                    ninchatSessionManager.ninchatState.getNinchatQuestionnaire().hasPreAudienceQuestionnaire() &&
                    !ninchatSessionManager.ninchatSessionHolder.isResumedSession()) {
                openPreAudienceQuestionnairesActivity();
            } else {
                openQueueActivity();
            }
        }
        registerQueueListener();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(queuesUpdatedReceiver);
        broadcastManager.unregisterReceiver(configurationFetchedReceiver);
    }

    public void onCloseClick(final View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void registerQueueListener() {
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(queuesUpdatedReceiver, new IntentFilter(NinchatSession.Broadcast.QUEUES_UPDATED));
        broadcastManager.registerReceiver(configurationFetchedReceiver, new IntentFilter(NinchatSession.Broadcast.CONFIGURATION_FETCHED));
        findViewById(R.id.ninchat_activity_close).setVisibility(View.VISIBLE);
        setQueueAdapter();
        setTexts();
    }

    private void openQueueActivity() {
        startActivityForResult(NinchatQueuePresenter.getLaunchIntentWithQueueId(this, queueId), NinchatQueueModel.REQUEST_CODE);
    }

    private void openPreAudienceQuestionnairesActivity() {
        startActivityForResult(NinchatQuestionnaireActivity.getLaunchIntent(
                this, queueId, PRE_AUDIENCE_QUESTIONNAIRE),
                NinchatQuestionnaireActivity.REQUEST_CODE);
    }

    private void openPostAudienceQuestionnairesActivity() {
        startActivityForResult(NinchatQuestionnaireActivity.getLaunchIntent(
                this, queueId, POST_AUDIENCE_QUESTIONNAIRE),
                NinchatQuestionnaireActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatQueueModel.REQUEST_CODE) {
            if (resultCode == RESULT_OK || queueId != null) {
                final NinchatSessionManager ninchatSessionManager = NinchatSessionManager.getInstance();
                if (resultCode == RESULT_OK &&
                        ninchatSessionManager.ninchatState.getNinchatQuestionnaire() != null &&
                        ninchatSessionManager.ninchatState.getNinchatQuestionnaire().hasPostAudienceQuestionnaire()) {
                    openPostAudienceQuestionnairesActivity();
                } else {
                    NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
                    sessionManager.close();
                    setResult(resultCode, data);
                    finish();
                }

            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == NinchatQuestionnaireActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setResult(resultCode, data);
                final boolean openQueue = data.getBooleanExtra(OPEN_QUEUE, false);
                final String newQueueId = data.getStringExtra(QUEUE_ID);
                if (openQueue && !TextUtils.isEmpty(newQueueId)) {
                    this.queueId = newQueueId;
                    openQueueActivity();
                } else {
                    finish();
                }
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
