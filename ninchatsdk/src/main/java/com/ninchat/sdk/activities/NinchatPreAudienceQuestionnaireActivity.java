package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.ninchat.client.JSON;
import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatFormLikeQuestionnaireAdapter;
import com.ninchat.sdk.events.OnRequireStepChange;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Stack;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.clearElement;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.clearLastElement;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getAllFilledElements;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getCurrentElement;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getElements;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getMatchingLogic;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getMatchingLogicTarget;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getNextElementIndex;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireAnswers;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireAnswersQueue;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireAnswersTags;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireElementIndexByName;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isComplete;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isRegister;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.updateRequiredFieldStats;


public final class NinchatPreAudienceQuestionnaireActivity extends NinchatBaseActivity {
    private final String TAG = NinchatPreAudienceQuestionnaireActivity.class.getSimpleName();
    public static final int REQUEST_CODE = NinchatPreAudienceQuestionnaireActivity.class.hashCode() & 0xffff;
    protected static final String COMMAND_TYPE = "commandType";
    protected static final String ANSWER_LIST = "answerList";
    protected static final String TAG_LIST = "tagList";
    protected static final String QUEUE_ID = "queueId";
    private Stack<Integer> historyList;
    private NinchatFormLikeQuestionnaireAdapter mPreAudienceQuestionnaireAdapter;
    private RecyclerView mRecyclerView;
    private String queueId;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_form_questionnaire;
    }

    public static Intent getLaunchIntent(final Context context, final String queueId) {
        return new Intent(context, NinchatPreAudienceQuestionnaireActivity.class).putExtra(QUEUE_ID, queueId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        historyList = new Stack<>();

        EventBus.getDefault().register(this);
        final Intent intent = getIntent();
        queueId = intent.getStringExtra(QUEUE_ID);
        mRecyclerView = (RecyclerView) findViewById(R.id.questionnaire_form_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        historyList.push(0);
        mPreAudienceQuestionnaireAdapter = new NinchatFormLikeQuestionnaireAdapter(
                new NinchatQuestionnaire(
                        getElements(
                                getQuestionnaire(historyList.peek()))));
        final int spaceInPixel = getResources().getDimensionPixelSize(R.dimen.items_margin_top);
        mRecyclerView.addItemDecoration(new NinchatQuestionnaireItemDecoration(spaceInPixel));
        mRecyclerView.setAdapter(mPreAudienceQuestionnaireAdapter);
        mRecyclerView.setItemViewCacheSize(mPreAudienceQuestionnaireAdapter.getItemCount());
    }

    @Override
    protected void onDestroy() {
        this.historyList.clear();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onClose(final View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void close(final boolean isComplete, final String queueId) {
        Intent currentIntent = new Intent();
        if (!TextUtils.isEmpty(queueId)) {
            this.queueId = queueId;
        }
        currentIntent.putExtra(NinchatPreAudienceQuestionnaireActivity.QUEUE_ID, this.queueId);
        currentIntent.putExtra(NinchatPreAudienceQuestionnaireActivity.COMMAND_TYPE, isComplete ?
                "_compete" : "_register");
        setResult(RESULT_OK, currentIntent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(@NotNull OnRequireStepChange requireStateChange) {
        final NinchatQuestionnaire questionnaire = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires()
                .getNinchatPreAudienceQuestionnaire();

        if (requireStateChange.moveType == OnRequireStepChange.back) {
            // remove last element
            if (!historyList.empty()) {
                historyList.pop();
            }
        } else {
            // a next button or a logic
            final JSONObject currentElement = getCurrentElement(questionnaire.getQuestionnaireList(), historyList.peek());
            final int errorIndex = updateRequiredFieldStats(currentElement);
            if (errorIndex != -1) {
                clearLastElement(currentElement);
                mRecyclerView.setAdapter(mPreAudienceQuestionnaireAdapter);
                return;
            }
            final JSONArray filledElements = getAllFilledElements(questionnaire.getQuestionnaireList(), historyList);
            final JSONObject matchingLogic = getMatchingLogic(questionnaire.getQuestionnaireList(), filledElements, currentElement);
            final String targetElementName = getMatchingLogicTarget(matchingLogic);
            final int targetElementIndex = getQuestionnaireElementIndexByName(questionnaire.getQuestionnaireList(), targetElementName);
            if (isComplete(targetElementName)) {
                handleComplete();
                return;
            }
            if (isRegister(targetElementName)) {
                handleRegister();
                return;
            }
            // if does not found a match
            if (targetElementIndex == -1) {
                // If there is also no next element
                final int nextElementIndex = getNextElementIndex(questionnaire.getQuestionnaireList(), historyList.peek());
                if (nextElementIndex == -1) {
                    // there is also no more questionnaire. - complete the form and exit
                    handleComplete();
                    return;
                } else {
                    historyList.push(nextElementIndex);
                }
            } else {
                historyList.push(targetElementIndex);
            }
        }
        clearElement(questionnaire.getQuestionnaireList(), historyList, historyList.peek());
        mPreAudienceQuestionnaireAdapter.updateContent(getElements(getQuestionnaire(historyList.peek())));
        mRecyclerView.setAdapter(mPreAudienceQuestionnaireAdapter);
    }


    private final JSONObject getQuestionnaire(final int index) {
        final NinchatQuestionnaire questionnaire = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires()
                .getNinchatPreAudienceQuestionnaire();
        if (getCurrentElement(questionnaire.getQuestionnaireList(), index) != null) {
            return getCurrentElement(questionnaire.getQuestionnaireList(), index);
        }
        final int nextElementIndex = getNextElementIndex(questionnaire.getQuestionnaireList(), index);
        // if it does not have any current element then just pick the next element from the list
        return nextElementIndex == -1 ?
                null : getCurrentElement(questionnaire.getQuestionnaireList(), nextElementIndex);
    }

    public void handleComplete() {
        final NinchatQuestionnaire questionnaire = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires()
                .getNinchatPreAudienceQuestionnaire();
        final String queueName = getQuestionnaireAnswersQueue(questionnaire.getQuestionnaireList(), historyList);
        final JSONObject answerList = getQuestionnaireAnswers(questionnaire.getQuestionnaireList(), historyList);
        final JSONArray tagList = getQuestionnaireAnswersTags(questionnaire.getQuestionnaireList(), historyList);
        updateAudienceMetaData(answerList, tagList);
        close(true, queueName);
    }

    public void handleRegister() {
        final NinchatQuestionnaire questionnaire = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires()
                .getNinchatPreAudienceQuestionnaire();
        final String queueName = getQuestionnaireAnswersQueue(questionnaire.getQuestionnaireList(), historyList);
        final JSONObject answerList = getQuestionnaireAnswers(questionnaire.getQuestionnaireList(), historyList);
        final JSONArray tagList = getQuestionnaireAnswersTags(questionnaire.getQuestionnaireList(), historyList);
        updateAudienceMetaData(answerList, tagList);
        close(false, queueName);
    }

    public void updateAudienceMetaData(final JSONObject answerList, final JSONArray tagList) {
        if (answerList == null || answerList.length() == 0) {
            return;
        }
        try {
            answerList.putOpt("tags", tagList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(NinchatSessionManager.getInstance().getAudienceMetadata() == null) {
            NinchatSessionManager.getInstance().setAudienceMetadata(new Props());
        }
        NinchatSessionManager.getInstance().getAudienceMetadata().setJSON("pre_answers", new JSON(answerList.toString()));
    }
}
