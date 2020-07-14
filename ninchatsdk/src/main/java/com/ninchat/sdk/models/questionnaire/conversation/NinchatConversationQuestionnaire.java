package com.ninchat.sdk.models.questionnaire.conversation;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatConversationQuestionnaireAdapter;
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

import static com.ninchat.sdk.helper.NinchatQuestionnaire.POST_AUDIENCE_QUESTIONNAIRE;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.clearElement;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.clearElementResult;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.formHasError;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getAllFilledElements;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getCurrentElement;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getFinalAnswers;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getMatchingLogic;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getMatchingLogicTarget;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getNextElementIndex;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getPreAnswers;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaire;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireAnswers;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireAnswersQueue;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireAnswersTags;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireElementIndexByName;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isComplete;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isRegister;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.setQueue;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.setTags;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.setViewAndChildrenEnabled;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.updateRequiredFieldStats;

public class NinchatConversationQuestionnaire {

    private NinchatConversationQuestionnaireAdapter mNinchatConversationQuestionnaireAdapter;
    private WeakReference<RecyclerView> mRecyclerViewWeakReference;
    private WeakReference<RecyclerView.LayoutManager> mLinearLayoutWeakReference;
    private String queueId;
    private final int questionnaireType;
    private final NinchatQuestionnaire mQuestionnaire;
    private Stack<Integer> historyList;

    public NinchatConversationQuestionnaire(final String queueId,
                                            final int questionnaireType,
                                            final RecyclerView recyclerView,
                                            final RecyclerView.LayoutManager linearLayout) {
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
        mNinchatConversationQuestionnaireAdapter = new
                NinchatConversationQuestionnaireAdapter(
                new NinchatQuestionnaire(
                        getQuestionnaireAsList()));
    }

    private void handleNext() {
        clearElement(mQuestionnaire.getQuestionnaireList(), historyList, historyList.peek());
        for (int i = 0; i < mLinearLayoutWeakReference.get().getChildCount(); i++) {
            View child = mLinearLayoutWeakReference.get().getChildAt(i);
            setViewAndChildrenEnabled(child, false);
        }
        mNinchatConversationQuestionnaireAdapter.addContent(getCurrentElement(mQuestionnaire.getQuestionnaireList(), historyList.peek()));
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
        final JSONObject answers = getFinalAnswers(answerList, tagList);
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
        // mNinchatConversationQuestionnaireAdapter.notifyItemChanged(errorIndex);
        EventBus.getDefault().post(new OnComponentError(errorIndex));
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

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onItemLoaded(OnItemLoaded onItemLoaded) {
        mRecyclerViewWeakReference.get().post(() -> mRecyclerViewWeakReference.get().smoothScrollToPosition(mNinchatConversationQuestionnaireAdapter.getItemCount() - 1));

    }
}
