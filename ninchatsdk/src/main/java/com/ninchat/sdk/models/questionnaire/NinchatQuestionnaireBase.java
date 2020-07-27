package com.ninchat.sdk.models.questionnaire;

import android.content.Context;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatQuestionnaireBaseAdapter;
import com.ninchat.sdk.events.OnAudienceRegistered;
import com.ninchat.sdk.events.OnCompleteQuestionnaire;
import com.ninchat.sdk.events.OnItemLoaded;
import com.ninchat.sdk.events.OnNextQuestionnaire;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.tasks.NinchatRegisterAudienceTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public abstract class NinchatQuestionnaireBase<T extends NinchatQuestionnaireBaseAdapter> {
    protected WeakReference<RecyclerView> mRecyclerViewWeakReference;
    protected WeakReference<LinearLayoutManager> mLinearLayoutWeakReference;
    protected String queueId;
    protected int questionnaireType;
    protected String mAudienceRegisterText;
    protected String mAudienceRegisterClosedText;
    protected NinchatQuestionnaire mQuestionnaireList;
    protected T ninchatQuestionnaireAdapter;

    public NinchatQuestionnaireBase(String queueId,
                                    int questionnaireType,
                                    RecyclerView recyclerView,
                                    LinearLayoutManager linearLayout) {
        // register event bus
        EventBus.getDefault().register(this);
        mRecyclerViewWeakReference = new WeakReference<>(recyclerView);
        mLinearLayoutWeakReference = new WeakReference<>(linearLayout);
        this.queueId = queueId;
        this.questionnaireType = questionnaireType;
        this.mAudienceRegisterText = getAudienceRegisteredText(questionnaireType);
        this.mAudienceRegisterClosedText = getAudienceRegisteredClosedText(questionnaireType);
        NinchatQuestionnaireHolder questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaireHolder();
        mQuestionnaireList = questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getNinchatPreAudienceQuestionnaire() : questionnaires.getNinchatPostAudienceQuestionnaire();
    }

    public void setAdapter(Context mContext) {
        int spaceInPixelTop = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_top);
        int spaceLeft = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_left);
        int spaceRight = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_right);
        mRecyclerViewWeakReference.get().addItemDecoration(new NinchatQuestionnaireItemDecoration(
                spaceInPixelTop,
                spaceLeft,
                spaceRight
        ));
        mRecyclerViewWeakReference.get().setAdapter(ninchatQuestionnaireAdapter);
    }

    protected NinchatQuestionnaire getInitialQuestionnaire() {
        throw new RuntimeException("implement me");
    }

    protected void handleNext(int index) {
        throw new RuntimeException("implement me");
    }

    protected void handlePrevious() {
        throw new RuntimeException("implement me");
    }

    protected void handleError() {
        throw new RuntimeException("implement me");
    }

    protected NinchatQuestionnaire getQuestionnaireAnswerList() {
        throw new RuntimeException("implement me");
    }

    private void handleComplete() {
        if (isClosedQueue(queueId)) {
            handleRegister();
            return;
        }
        close(false);
    }

    private void handleRegister() {
        close(true);
    }


    private void close(boolean isRegister) {
        NinchatQuestionnaire answersList = getQuestionnaireAnswerList();
        JSONObject answerList = getQuestionnaireAnswers(answersList.getQuestionnaireList());
        JSONArray tagList = getQuestionnaireAnswersTags(answersList.getQuestionnaireList());
        JSONObject answers = mergeAnswersAndTags(answerList, tagList);
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

    public void dispose() {
        EventBus.getDefault().unregister(this);
    }

    private Pair<String, Integer> getTargetElementAndIndex(JSONObject matchingLogic) {
        String elementName = getMatchingLogicTarget(matchingLogic);
        int elementIndex = getQuestionnaireElementIndexByName(mQuestionnaireList.getQuestionnaireList(), elementName);
        return Pair.create(elementName, elementIndex);
    }

    private void setTagsAndQueue(JSONObject matchingLogic) {
        JSONObject currentElement = ninchatQuestionnaireAdapter.getLastElement();
        setTags(matchingLogic, currentElement);
        setQueue(matchingLogic, currentElement);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onNextQuestionnaire(@NotNull OnNextQuestionnaire onNextQuestionnaire) {
        NinchatQuestionnaire answersList = getQuestionnaireAnswerList();
        JSONObject previousElement = answersList.getLastElement();
        int currentElementIndex = getQuestionnaireElementIndexByName(mQuestionnaireList.getQuestionnaireList(), getName(previousElement));
        if (onNextQuestionnaire.moveType == OnNextQuestionnaire.back) {
            handlePrevious();
            return;
        }
        if (onNextQuestionnaire.moveType == OnNextQuestionnaire.register) {
            handleRegister();
            return;
        }
        if (onNextQuestionnaire.moveType == OnNextQuestionnaire.complete) {
            handleComplete();
            return;
        }
        if (formHasError(answersList.getLastElement())) {
            handleError();
            return;
        }
        JSONObject matchingLogic = getMatchingLogic(mQuestionnaireList.getQuestionnaireList(),
                answersList.getQuestionnaireList(), previousElement);
        setTagsAndQueue(matchingLogic);
        Pair<String, Integer> target = getTargetElementAndIndex(matchingLogic);
        int thankYouElementIndex = -1;
        if (isRegister(target.first)) {
            // if has audience register text
            if (TextUtils.isEmpty(mAudienceRegisterText)) {
                handleRegister();
                return;
            }
            thankYouElementIndex = mQuestionnaireList.updateQuestionWithThankYouElement(mAudienceRegisterText, true);
        } else if (isComplete(target.first) ||
                (target.second == -1 && getNextElementIndex(mQuestionnaireList.getQuestionnaireList(), currentElementIndex) == -1)) {
            // if completed or it is a last element or there is no other matching element then it can be a complete
            String currentQueueId = getQuestionnaireAnswersQueue(answersList.getQuestionnaireList());
            if (!TextUtils.isEmpty(currentQueueId)) {
                this.queueId = currentQueueId;
            }
            if (!isClosedQueue(this.queueId) || TextUtils.isEmpty(mAudienceRegisterClosedText)) {
                handleComplete();
                return;
            }
            thankYouElementIndex = mQuestionnaireList.updateQuestionWithThankYouElement(mAudienceRegisterText, true);
        }
        int currentIndex = thankYouElementIndex != -1 ? thankYouElementIndex :
                target.second != -1 ? target.second : getNextElementIndex(mQuestionnaireList.getQuestionnaireList(), currentElementIndex);
        handleNext(currentIndex);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onItemLoaded(OnItemLoaded onItemLoaded) {
        mLinearLayoutWeakReference.get().scrollToPositionWithOffset(ninchatQuestionnaireAdapter.getItemCount() - 1, 0);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onAudienceRegistered(OnAudienceRegistered onAudienceRegistered) {
        // call finish after session close
        NinchatSessionManager.getInstance().close();
        EventBus.getDefault().post(new OnCompleteQuestionnaire(false, queueId));
    }

}
