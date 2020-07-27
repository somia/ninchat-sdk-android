package com.ninchat.sdk.models.questionnaire.form;

import android.content.Context;
import android.os.Handler;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatFormQuestionnaireAdapter;
import com.ninchat.sdk.events.OnAudienceRegistered;
import com.ninchat.sdk.events.OnCompleteQuestionnaire;
import com.ninchat.sdk.events.OnNextQuestionnaire;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaires;
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

public class NinchatFormQuestionnaire {
    private NinchatFormQuestionnaireAdapter mNinchatFormQuestionnaireAdapter;
    private WeakReference<RecyclerView> mRecyclerViewWeakReference;
    private NinchatQuestionnaire mQuestionnaireAnswerList;
    private String queueId;
    private int questionnaireType;
    private NinchatQuestionnaire mQuestionnaireList;
    private String mAudienceRegisterText;
    private String mAudienceRegisterClosedText;

    public NinchatFormQuestionnaire(String queueId,
                                    int questionnaireType,
                                    RecyclerView recyclerView) {
        // register event bus
        EventBus.getDefault().register(this);
        mRecyclerViewWeakReference = new WeakReference<>(recyclerView);
        mQuestionnaireAnswerList = new NinchatQuestionnaire(new JSONArray());
        this.queueId = queueId;
        this.questionnaireType = questionnaireType;
        this.mAudienceRegisterText = getAudienceRegisteredText(questionnaireType);
        this.mAudienceRegisterClosedText = getAudienceRegisteredClosedText(questionnaireType);
        NinchatQuestionnaires questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires();
        mQuestionnaireList = questionnaireType == PRE_AUDIENCE_QUESTIONNAIRE ?
                questionnaires.getNinchatPreAudienceQuestionnaire() : questionnaires.getNinchatPostAudienceQuestionnaire();
        mNinchatFormQuestionnaireAdapter = new NinchatFormQuestionnaireAdapter(getQuestionnaire(), true);
    }

    private NinchatQuestionnaire getQuestionnaire() {
        JSONObject currentElement = getSlowCopy(mQuestionnaireList.getElement(0));
        mQuestionnaireAnswerList.addQuestionnaire(currentElement);
        return new NinchatQuestionnaire(getElements(currentElement));
    }

    private void handleNext(int index) {
        if (index == -1) {
            return;
        }
        JSONObject currentElement = getSlowCopy(getElementByIndex(mQuestionnaireList.getQuestionnaireList(), index));
        mQuestionnaireAnswerList.addQuestionnaire(currentElement);
        mNinchatFormQuestionnaireAdapter.updateContent(getElements(currentElement));
        mNinchatFormQuestionnaireAdapter.notifyDataSetChanged();
        mRecyclerViewWeakReference.get().setAdapter(mNinchatFormQuestionnaireAdapter);
        // scroll to top
        new Handler().post(() -> mRecyclerViewWeakReference.get().scrollToPosition(0));
    }

    private void handlePrevious() {
        JSONObject previousElement = mQuestionnaireAnswerList.getSecondLastElement();
        int currentElementIndex = getQuestionnaireElementIndexByName(mQuestionnaireList.getQuestionnaireList(), getName(previousElement));
        JSONObject currentElement = getSlowCopy(getElementByIndex(mQuestionnaireList.getQuestionnaireList(), currentElementIndex));
        mQuestionnaireAnswerList.addQuestionnaire(currentElement);
        mNinchatFormQuestionnaireAdapter.updateContent(getElements(currentElement));
        mNinchatFormQuestionnaireAdapter.notifyDataSetChanged();
        mRecyclerViewWeakReference.get().setAdapter(mNinchatFormQuestionnaireAdapter);
        // scroll to top
        new Handler().post(() -> mRecyclerViewWeakReference.get().scrollToPosition(0));
    }

    private void handleError() {
        JSONObject lastElement = mQuestionnaireAnswerList.getLastElement();
        updateRequiredFieldStats(lastElement);
        clearElementResult(lastElement);
        mRecyclerViewWeakReference.get().clearFocus();
        mNinchatFormQuestionnaireAdapter.notifyDataSetChanged();
    }

    private void close(boolean isRegister) {
        JSONObject answerList = getQuestionnaireAnswers(mQuestionnaireAnswerList.getQuestionnaireList());
        JSONArray tagList = getQuestionnaireAnswersTags(mQuestionnaireAnswerList.getQuestionnaireList());
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

    public void setAdapter(Context mContext) {
        int spaceInPixelTop = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_top);
        int spaceLeft = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_left);
        int spaceRight = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_questionnaire_items_margin_right);
        mRecyclerViewWeakReference.get().addItemDecoration(new NinchatQuestionnaireItemDecoration(
                spaceInPixelTop,
                spaceLeft,
                spaceRight
        ));
        mRecyclerViewWeakReference.get().setAdapter(this.mNinchatFormQuestionnaireAdapter);
    }

    public void dispose() {
        EventBus.getDefault().unregister(this);
    }

    private void setTagsAndQueue(JSONObject matchingLogic) {
        JSONObject currentElement = mQuestionnaireAnswerList.getLastElement();
        setTags(matchingLogic, currentElement);
        setQueue(matchingLogic, currentElement);
    }

    @NotNull
    private Pair<String, Integer> getTargetElementAndIndex(JSONObject matchingLogic) {
        String elementName = getMatchingLogicTarget(matchingLogic);
        int elementIndex = getQuestionnaireElementIndexByName(mQuestionnaireList.getQuestionnaireList(), elementName);
        return Pair.create(elementName, elementIndex);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onNextQuestionnaire(@NotNull OnNextQuestionnaire onNextQuestionnaire) {
        JSONObject previousElement = mQuestionnaireAnswerList.getLastElement();
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
        if (formHasError(mQuestionnaireAnswerList.getLastElement())) {
            handleError();
            return;
        }
        JSONObject matchingLogic = getMatchingLogic(mQuestionnaireList.getQuestionnaireList(),
                mQuestionnaireAnswerList.getQuestionnaireList(), previousElement);
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
            String currentQueueId = getQuestionnaireAnswersQueue(mQuestionnaireAnswerList.getQuestionnaireList());
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
    public void onAudienceRegistered(OnAudienceRegistered onAudienceRegistered) {
        // call finish after session close
        NinchatSessionManager.getInstance().close();
        EventBus.getDefault().post(new OnCompleteQuestionnaire(false, queueId));
    }
}
