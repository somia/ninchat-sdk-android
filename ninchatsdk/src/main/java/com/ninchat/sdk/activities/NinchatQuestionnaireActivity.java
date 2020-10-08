package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnCompleteQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireHolder;
import com.ninchat.sdk.models.questionnaire.conversation.NinchatConversationQuestionnaire;
import com.ninchat.sdk.models.questionnaire.form.NinchatFormQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;


public class NinchatQuestionnaireActivity extends NinchatBaseActivity {
    private String TAG = NinchatQuestionnaireActivity.class.getSimpleName();
    public static int REQUEST_CODE = NinchatQuestionnaireActivity.class.hashCode() & 0xffff;
    protected static String QUEUE_ID = "queueId";
    protected static String QUESTIONNAIRE_TYPE = "questionType";
    protected static String OPEN_QUEUE = "openQueue";

    private NinchatConversationQuestionnaire ninchatConversationQuestionnaire;
    private NinchatFormQuestionnaire ninchatFormQuestionnaire;
    private RecyclerView mRecyclerView;
    private String queueId;
    private int questionnaireType;
    private LinearLayoutManager mLayoutManager;
    private boolean isConversationLike;
    private Pair<String, String> botDetails;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_questionnaire;
    }

    public static Intent getLaunchIntent(Context context, String queueId, int questionnaireType) {
        return new Intent(context, NinchatQuestionnaireActivity.class)
                .putExtra(QUEUE_ID, queueId)
                .putExtra(QUESTIONNAIRE_TYPE, questionnaireType);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        questionnaireType = intent.getIntExtra(QUESTIONNAIRE_TYPE, DEFAULT_INT_VALUE);
        queueId = intent.getStringExtra(QUEUE_ID);
        mRecyclerView = findViewById(R.id.questionnaire_form_rview);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        isConversationLike = getFormOrConvLikeQuestionnaireType();
        botDetails = getBotDetails();
        if (isConversationLike) {
            ninchatConversationQuestionnaire = new NinchatConversationQuestionnaire(
                    queueId,
                    questionnaireType,
                    botDetails,
                    mRecyclerView,
                    mLayoutManager
            );
            ninchatConversationQuestionnaire.setAdapter(getApplicationContext());
        } else {
            ninchatFormQuestionnaire = new NinchatFormQuestionnaire(queueId,
                    questionnaireType,
                    mRecyclerView);
            ninchatFormQuestionnaire.setAdapter(getApplicationContext());
        }

    }

    @Override
    protected void onDestroy() {
        // dispose
        if (ninchatConversationQuestionnaire != null) {
            ninchatConversationQuestionnaire.dispose();
        }
        if (ninchatFormQuestionnaire != null) {
            ninchatFormQuestionnaire.dispose();
        }


        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onClose(View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean getFormOrConvLikeQuestionnaireType() {
        NinchatQuestionnaireHolder questionnaires = NinchatSessionManager
                .getInstance()
                .ninchatState.getNinchatQuestionnaire();
        if (questionnaires != null) {
            return questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                    questionnaires.conversationLikePreAudienceQuestionnaire() : questionnaires.conversationLikePostAudienceQuestionnaire();
        }
        return false;
    }

    private Pair<String, String> getBotDetails() {
        NinchatQuestionnaireHolder questionnaires = NinchatSessionManager
                .getInstance()
                .ninchatState.getNinchatQuestionnaire();
        if (questionnaires == null) {
            return Pair.create(null, null);
        }
        return Pair.create(questionnaires.getBotQuestionnaireName(), questionnaires.getBotQuestionnaireAvatar());
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onCompleteQuestionnaire(OnCompleteQuestionnaire onCompleteQuestionnaire) {
        Intent currentIntent = new Intent();
        currentIntent.putExtra(NinchatQuestionnaireActivity.QUEUE_ID, onCompleteQuestionnaire.queueId);
        currentIntent.putExtra(NinchatQuestionnaireActivity.OPEN_QUEUE, onCompleteQuestionnaire.openQueueView);
        setResult(RESULT_OK, currentIntent);
        finish();
    }
}
