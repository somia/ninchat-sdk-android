package com.ninchat.sdk.models.questionnaire.form;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatFormQuestionnaireAdapter;
import com.ninchat.sdk.events.OnCompleteQuestionnaire;
import com.ninchat.sdk.events.OnItemLoaded;
import com.ninchat.sdk.events.OnNextQuestionnaire;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;
import com.ninchat.sdk.tasks.NinchatRegisterAudienceTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Stack;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatFormQuestionnaire {

    private NinchatFormQuestionnaireAdapter mNinchatFormQuestionnaireAdapter;
    private WeakReference<RecyclerView> mRecyclerViewWeakReference;
    private WeakReference<LinearLayoutManager> mLinearLayoutWeakReference;
    private String queueId;
    private final int questionnaireType;
    private final NinchatQuestionnaire mQuestionnaire;
    private Stack<Integer> historyList;

    public NinchatFormQuestionnaire(final String queueId,
                                    final int questionnaireType,
                                    final RecyclerView recyclerView,
                                    final LinearLayoutManager linearLayout) {
        mRecyclerViewWeakReference = new WeakReference<>(recyclerView);
        mLinearLayoutWeakReference = new WeakReference<>(linearLayout);
        this.queueId = queueId;
        this.questionnaireType = questionnaireType;
        this.mQuestionnaire = getQuestionnaire(questionnaireType);
        this.initialize();
        this.createAdapter();
    }

    private void initialize() {
        this.dispose();
        historyList = new Stack<>();
        EventBus.getDefault().register(this);
    }

    private void createAdapter() {
        historyList.push(0);
        mNinchatFormQuestionnaireAdapter = new
                NinchatFormQuestionnaireAdapter(
                new NinchatQuestionnaire(
                        getQuestionnaireAsList()), true);
    }

    private void handleNext() {
        clearElement(mQuestionnaire.getQuestionnaireList(), historyList, historyList.peek());
        for (int i = 0; i < mLinearLayoutWeakReference.get().getChildCount(); i++) {
            View child = mLinearLayoutWeakReference.get().getChildAt(i);
            setViewAndChildrenEnabled(child, false);
        }

        mNinchatFormQuestionnaireAdapter.updateContent(getQuestionnaireAsList());
        mRecyclerViewWeakReference.get().setAdapter(mNinchatFormQuestionnaireAdapter);
        // mRecyclerViewWeakReference.get().scrollToPosition(mNinchatFormQuestionnaireAdapter.getItemCount() - 1);
    }

    private JSONArray getQuestionnaireAsList() {
        final JSONObject currentElement = getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek());
        // get the first element and return
        return getElements(currentElement);
    }

    private void close(final boolean isRegister) {
        final JSONObject answerList = getQuestionnaireAnswers(mQuestionnaire.getQuestionnaireList(), historyList);
        final JSONArray tagList = getQuestionnaireAnswersTags(mQuestionnaire.getQuestionnaireList(), historyList);
        final JSONObject answers = mergeAnswersAndTags(answerList, tagList);
        boolean openQueueView = true;
        if (questionnaireType == POST_AUDIENCE_QUESTIONNAIRE) {
            // a post audience questionnaire
            NinchatSessionManager.getInstance().sendPostAnswers(answers);
            openQueueView = false;
        } else {
            // a complete
            if (NinchatSessionManager.getInstance().getAudienceMetadata() == null) {
                NinchatSessionManager.getInstance().setAudienceMetadata(new Props());
            }
            NinchatSessionManager.getInstance().getAudienceMetadata().setObject("pre_answers", getPreAnswers(answers));
            // a register
            if (isRegister) {
                NinchatRegisterAudienceTask.start(queueId);
                openQueueView = false;
            }
        }
        // send an event via event bus now that the questionnaire list are completed and filled
        EventBus.getDefault().post(new OnCompleteQuestionnaire(openQueueView, queueId));
    }

    private void handleRegister() {
        close(true);
    }

    private void handleComplete() {
        final String currentQueueId = getQuestionnaireAnswersQueue(mQuestionnaire.getQuestionnaireList(), historyList);
        if (!TextUtils.isEmpty(currentQueueId)) {
            this.queueId = currentQueueId;
        }
        if (NinchatSessionManager.getInstance().getQueue(queueId) == null || NinchatSessionManager.getInstance().getQueue(queueId).isClosed()) {
            handleRegister();
            return;
        }
        close(false);
    }

    private JSONObject getCurrentlyMatchedLogicElement() {
        final JSONObject currentElement = getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek());
        final JSONArray filledElements = getAllFilledElements(mQuestionnaire.getQuestionnaireList(), historyList);
        return getMatchingLogic(mQuestionnaire.getQuestionnaireList(), filledElements, currentElement);
    }

    private void setTagsAndQueue(final JSONObject matchingLogic) {
        final JSONObject currentElement = getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek());
        setTags(matchingLogic, currentElement);
        setQueue(matchingLogic, currentElement);
    }

    private Pair<String, Integer> getTargetElementAndIndex(final JSONObject matchingLogic) {
        final String elementName = getMatchingLogicTarget(matchingLogic);
        final int elementIndex = getQuestionnaireElementIndexByName(mQuestionnaire.getQuestionnaireList(), elementName);
        return Pair.create(elementName, elementIndex);
    }

    private void handleError() {
        final JSONObject currentElement = getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek());
        final int errorIndex = updateRequiredFieldStats(currentElement);

        clearElementResult(currentElement);
        mRecyclerViewWeakReference.get().clearFocus();
        mRecyclerViewWeakReference.get().setAdapter(mNinchatFormQuestionnaireAdapter);
        mNinchatFormQuestionnaireAdapter.notifyDataSetChanged();
    }

    public void setAdapter(final Context mContext) {
        final int spaceInPixelTop = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_items_margin_top);
        final int spaceLeft = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_items_margin_left);
        final int spaceRight = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_items_margin_right);
        mRecyclerViewWeakReference.get().addItemDecoration(new NinchatQuestionnaireItemDecoration(
                spaceInPixelTop,
                spaceLeft,
                spaceRight
        ));
        mRecyclerViewWeakReference.get().setAdapter(this.mNinchatFormQuestionnaireAdapter);
    }

    public void dispose() {
        if (this.historyList != null) {
            this.historyList.clear();
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onNextQuestionnaire(@NotNull OnNextQuestionnaire onNextQuestionnaire) {
        if (onNextQuestionnaire.moveType == OnNextQuestionnaire.back) {
            // remove last element
            if (!historyList.empty()) {
                historyList.pop();
            }
        } else {
            if (formHasError(getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek()))) {
                handleError();
                return;
            }
            final JSONObject matchingLogic = getCurrentlyMatchedLogicElement();
            setTagsAndQueue(matchingLogic);
            final Pair<String, Integer> target = getTargetElementAndIndex(matchingLogic);
            if (isComplete(target.first) || (target.second == -1 && getNextElementIndex(mQuestionnaire.getQuestionnaireList(), historyList.peek()) == -1)) {
                handleComplete();
                return;
            }
            if (isRegister(target.first)) {
                handleRegister();
                return;
            }
            historyList.push(target.second != -1 ? target.second : getNextElementIndex(mQuestionnaire.getQuestionnaireList(), historyList.peek()));
        }
        handleNext();
    }
}
