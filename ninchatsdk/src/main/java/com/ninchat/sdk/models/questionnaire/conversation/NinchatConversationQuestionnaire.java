package com.ninchat.sdk.models.questionnaire.conversation;

import android.content.Context;
import android.os.Handler;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatConversationQuestionnaireAdapter;
import com.ninchat.sdk.events.OnAudienceRegistered;
import com.ninchat.sdk.events.OnCompleteQuestionnaire;
import com.ninchat.sdk.events.OnComponentError;
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
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatConversationQuestionnaire {

    private NinchatConversationQuestionnaireAdapter mNinchatConversationQuestionnaireAdapter;
    private WeakReference<RecyclerView> mRecyclerViewWeakReference;
    private WeakReference<LinearLayoutManager> mLinearLayoutWeakReference;
    private String queueId;
    private final int questionnaireType;
    private final NinchatQuestionnaire mQuestionnaire;
    private final String mAudienceRegisterText;
    private final String mAudienceRegisterClosedText;
    private Stack<Integer> historyList;

    public NinchatConversationQuestionnaire(final String queueId,
                                            final int questionnaireType,
                                            final RecyclerView recyclerView,
                                            final LinearLayoutManager linearLayout) {
        mRecyclerViewWeakReference = new WeakReference<>(recyclerView);
        mLinearLayoutWeakReference = new WeakReference<>(linearLayout);
        this.queueId = queueId;
        this.questionnaireType = questionnaireType;
        this.mQuestionnaire = getQuestionnaire(questionnaireType);
        this.mAudienceRegisterText = getAudienceRegisteredText(questionnaireType);
        this.mAudienceRegisterClosedText = getAudienceRegisteredClosedText(questionnaireType);
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
        mNinchatConversationQuestionnaireAdapter = new
                NinchatConversationQuestionnaireAdapter(
                new NinchatQuestionnaire(
                        getQuestionnaireAsList()));
    }

    private void handleNext() {
        clearElement(mQuestionnaire.getQuestionnaireList(), historyList, historyList.peek());
        final JSONObject currentElement = getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek());
        mNinchatConversationQuestionnaireAdapter.addContent(currentElement);
        mNinchatConversationQuestionnaireAdapter.notifyDataSetChanged();
        mRecyclerViewWeakReference.get().scrollToPosition(mNinchatConversationQuestionnaireAdapter.getItemCount() - 1);
    }

    private void handlePrevious() {
        clearElement(mQuestionnaire.getQuestionnaireList(), historyList, historyList.peek());
        if (!historyList.empty()) {
            historyList.pop();
        }
        mNinchatConversationQuestionnaireAdapter.removeLast();
        mNinchatConversationQuestionnaireAdapter.notifyDataSetChanged();
        mRecyclerViewWeakReference.get().scrollToPosition(mNinchatConversationQuestionnaireAdapter.getItemCount() - 1);
    }

    private JSONArray getQuestionnaireAsList() {
        JSONArray elementList = new JSONArray();
        for (int currentIndex : historyList) {
            final JSONObject currentElement = getCurrentElement(mQuestionnaire.getQuestionnaireList(), currentIndex);
            elementList.put(currentElement);
        }
        return elementList;
    }

    private void close(final boolean isRegister) {
        final JSONObject answerList = getQuestionnaireAnswers(mQuestionnaire.getQuestionnaireList(), historyList);
        final JSONArray tagList = getQuestionnaireAnswersTags(mQuestionnaire.getQuestionnaireList(), historyList);
        final JSONObject answers = mergeAnswersAndTags(answerList, tagList);
        if (questionnaireType == POST_AUDIENCE_QUESTIONNAIRE) {
            // a post audience questionnaire
            NinchatSessionManager.getInstance().sendPostAnswers(answers);
            // send an event via event bus now that the questionnaire list are completed and filled
            EventBus.getDefault().post(new OnCompleteQuestionnaire(false, queueId));
        } else {
            // a complete
            if (NinchatSessionManager.getInstance().getAudienceMetadata() == null) {
                NinchatSessionManager.getInstance().setAudienceMetadata(new Props());
            }
            NinchatSessionManager.getInstance().getAudienceMetadata().setObject("pre_answers", getPreAnswers(answers));
            // a register
            if (isRegister) {
                NinchatRegisterAudienceTask.start(queueId);
                // send an event via event bus now that the questionnaire list are completed and filled
                // wait for audience register event and do everything else from there "onAudienceRegistered"
                return;
            }
            // send an event via event bus now that the questionnaire list are completed and filled
            EventBus.getDefault().post(new OnCompleteQuestionnaire(true, queueId));
        }
    }

    private void handleRegister() {
        close(true);
    }

    private void handleComplete() {
        if (isClosedQueue(queueId)) {
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
        updateRequiredFieldStats(currentElement);
        final String itemName = getErrorItemName(mQuestionnaire.getQuestionnaireList(), currentElement);
        clearElementResult(currentElement);
        mRecyclerViewWeakReference.get().clearFocus();
        EventBus.getDefault().post(new OnComponentError(itemName));
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
        mRecyclerViewWeakReference.get().setAdapter(this.mNinchatConversationQuestionnaireAdapter);
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
            if (!historyList.empty()) {
                historyList.pop();
            }
        } else if (onNextQuestionnaire.moveType == OnNextQuestionnaire.register) {
            handleRegister();
            return;
        } else if (onNextQuestionnaire.moveType == OnNextQuestionnaire.complete) {
            handleComplete();
            return;
        } else {
            if (formHasError(getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek()))) {
                handleError();
                return;
            }
            final JSONObject matchingLogic = getCurrentlyMatchedLogicElement();
            setTagsAndQueue(matchingLogic);
            final Pair<String, Integer> target = getTargetElementAndIndex(matchingLogic);
            int thankYouElementIndex = -1;
            if (isRegister(target.first)) {
                // if has audience register text
                if (TextUtils.isEmpty(mAudienceRegisterText)) {
                    handleRegister();
                    return;
                }
                thankYouElementIndex = mQuestionnaire.updateQuestionWithThankYouElement(mAudienceRegisterText, true);
            } else if (isComplete(target.first) || (target.second == -1 && getNextElementIndex(mQuestionnaire.getQuestionnaireList(), historyList.peek()) == -1)) {
                // if completed or it is a last element or there is no other matching element then it can be a complete
                final String currentQueueId = getQuestionnaireAnswersQueue(mQuestionnaire.getQuestionnaireList(), historyList);
                if (!TextUtils.isEmpty(currentQueueId)) {
                    this.queueId = currentQueueId;
                }
                if (!isClosedQueue(this.queueId) || TextUtils.isEmpty(mAudienceRegisterClosedText)) {
                    handleComplete();
                    return;
                }
                thankYouElementIndex = mQuestionnaire.updateQuestionWithThankYouElement(mAudienceRegisterClosedText, false);
            }
            historyList.push(
                    thankYouElementIndex != -1 ?
                            thankYouElementIndex :
                            target.second != -1 ?
                                    target.second : getNextElementIndex(mQuestionnaire.getQuestionnaireList(), historyList.peek()));
        }
        handleNext();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onItemLoaded(OnItemLoaded onItemLoaded) {
        mLinearLayoutWeakReference.get().scrollToPositionWithOffset(mNinchatConversationQuestionnaireAdapter.getItemCount() - 1, 0);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onAudienceRegistered(OnAudienceRegistered onAudienceRegistered) {
        // call finish after session close
        NinchatSessionManager.getInstance().close();
        EventBus.getDefault().post(new OnCompleteQuestionnaire(false, queueId));
    }
}
