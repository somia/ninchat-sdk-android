package com.ninchat.sdk.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ninchat.client.JSON;
import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatQueueListAdapter;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;
import com.ninchat.sdk.tasks.NinchatRegisterAudienceTask;

import org.json.JSONArray;


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
        final RecyclerView queueList = (RecyclerView) findViewById(R.id.ninchat_activity_queue_list);
        final NinchatQueueListAdapter ninchatQueueListAdapter = sessionManager.getNinchatQueueListAdapter(NinchatActivity.this);
        queueList.setAdapter(ninchatQueueListAdapter);
        if (ninchatQueueListAdapter.getItemCount() == 0) {
            findViewById(R.id.ninchat_activity_no_queues).setVisibility(View.VISIBLE);
            final TextView motd = findViewById(R.id.ninchat_activity_motd);
            motd.setText(sessionManager.getNoQueues());
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
        final TextView topHeader = findViewById(R.id.ninchat_activity_header);
        topHeader.setText(sessionManager.getWelcome());
        final Button closeButton = findViewById(R.id.ninchat_activity_close);
        closeButton.setText(sessionManager.getCloseWindow());
        closeButton.setVisibility(sessionManager.showNoThanksButton() ? View.VISIBLE : View.GONE);
        final TextView motd = findViewById(R.id.ninchat_activity_motd);
        motd.setText(sessionManager.getMOTD());
        final TextView noQueues = findViewById(R.id.ninchat_activity_no_queues);
        noQueues.setText(sessionManager.getNoQueues());
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();

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
            if (ninchatSessionManager.getNinchatQuestionnaires().hasPreAudienceQuestionnaire()) {
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
        startActivityForResult(NinchatQueueActivity.getLaunchIntent(this, queueId), NinchatQueueActivity.REQUEST_CODE);
    }

    private void openPreAudienceQuestionnairesActivity() {
        startActivityForResult(NinchatPreAudienceQuestionnaireActivity.getLaunchIntent(this, queueId), NinchatPreAudienceQuestionnaireActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatQueueActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK || queueId != null) {
                sessionManager.close();
                setResult(resultCode, data);
                finish();
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        } else if (requestCode == NinchatPreAudienceQuestionnaireActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (!TextUtils.isEmpty(data.getStringExtra(NinchatPreAudienceQuestionnaireActivity.QUEUE_ID))){
                    queueId = data.getStringExtra(NinchatPreAudienceQuestionnaireActivity.QUEUE_ID);
                }
                if ("_complete".equalsIgnoreCase(data.getStringExtra(NinchatPreAudienceQuestionnaireActivity.COMMAND_TYPE))) {
                    // takes user to the queue. is queue is closed register the user and end chat
                    setResult(resultCode, data);
                    if(NinchatSessionManager.getInstance().getQueue(queueId) != null && !NinchatSessionManager.getInstance().getQueue(queueId).isClosed()){
                        setResult(resultCode, data);
                        openQueueActivity();
                        return ;
                    }
                }
                NinchatRegisterAudienceTask.start(queueId);
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
