package com.ninchat.sdk.models.questionnaire;

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
import com.ninchat.sdk.adapters.NinchatQuestionnaireBaseAdapter;
import com.ninchat.sdk.events.OnAudienceRegistered;
import com.ninchat.sdk.events.OnCompleteQuestionnaire;
import com.ninchat.sdk.events.OnItemLoaded;
import com.ninchat.sdk.events.OnNextQuestionnaire;
import com.ninchat.sdk.events.OnPostAudienceQuestionnaire;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.tasks.NinchatDeleteUserTask;
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
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.setViewAndChildrenEnabled;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public abstract class NinchatQuestionnaireBase<T extends NinchatQuestionnaireBaseAdapter> {
    protected WeakReference<RecyclerView> mRecyclerViewWeakReference;
    protected WeakReference<LinearLayoutManager> mLinearLayoutWeakReference;
    protected String queueId;
    protected int questionnaireType;
    protected String thankYouText;
    protected NinchatQuestionnaire mQuestionnaireList;
    protected int pendingRequest;
    protected T ninchatQuestionnaireAdapter;

    private final int AUDIENCE_REGISTER = 1;
    private final int POST_ANSWERS = 2;
    private final int NONE = 3;

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
        if (ninchatQuestionnaireAdapter == null) return;
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
        JSONObject answers = updateQueueAndGetAnswers();
        if (isClosedQueue(queueId)) {
            handleRegister();
            return;
        }
        setAudienceMetadata(answers);
        EventBus.getDefault().post(new OnCompleteQuestionnaire(true, queueId));
    }

    private void handleRegister() {
        JSONObject answers = updateQueueAndGetAnswers();
        setAudienceMetadata(answers);
        pendingRequest = AUDIENCE_REGISTER;
        // a register
        NinchatRegisterAudienceTask.start(queueId);
        // wait for register to complete. Should get event from session manager
        // that register is complete with or without error onAudienceRegistered
    }

    private void handlePostAudienceQuestionnaire() {
        JSONObject answers = updateQueueAndGetAnswers();
        pendingRequest = POST_ANSWERS;
        // a post audience questionnaire
        NinchatSessionManager.getInstance().sendPostAnswers(answers);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNextQuestionnaire(@NotNull OnNextQuestionnaire onNextQuestionnaire) {
        NinchatQuestionnaire answersList = getQuestionnaireAnswerList();
        JSONObject previousElement = answersList.getLastElement();
        int currentElementIndex = getQuestionnaireElementIndexByName(mQuestionnaireList.getQuestionnaireList(), getName(previousElement));
        if (onNextQuestionnaire.moveType == OnNextQuestionnaire.back) {
            handlePrevious();
            return;
        }
        if (onNextQuestionnaire.moveType == OnNextQuestionnaire.thankYou) {
            NinchatSessionManager.getInstance().close();
            EventBus.getDefault().post(new OnCompleteQuestionnaire(false, queueId));
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
        if (isRegister(target.first)) {
            if (questionnaireType == POST_AUDIENCE_QUESTIONNAIRE) {
                handlePostAudienceQuestionnaire();
                return;
            }
            thankYouText = getAudienceRegisteredText(questionnaireType);
            handleRegister();
            return;
        }
        if (isComplete(target.first) ||
                (target.second == -1 && getNextElementIndex(mQuestionnaireList.getQuestionnaireList(), currentElementIndex) == -1)) {
            if (questionnaireType == POST_AUDIENCE_QUESTIONNAIRE) {
                handlePostAudienceQuestionnaire();
                return;
            }
            thankYouText = getAudienceRegisteredClosedText(questionnaireType);
            handleComplete();
            return;
        }
        int currentIndex = target.second != -1 ? target.second : getNextElementIndex(mQuestionnaireList.getQuestionnaireList(), currentElementIndex);
        handleNext(currentIndex);
    }

    private JSONObject updateQueueAndGetAnswers() {
        NinchatQuestionnaire answersList = getQuestionnaireAnswerList();
        JSONObject answerList = getQuestionnaireAnswers(answersList.getQuestionnaireList());
        JSONArray tagList = getQuestionnaireAnswersTags(answersList.getQuestionnaireList());
        JSONObject answers = mergeAnswersAndTags(answerList, tagList);
        String currentQueueId = getQuestionnaireAnswersQueue(answersList.getQuestionnaireList());
        if (!TextUtils.isEmpty(currentQueueId)) {
            this.queueId = currentQueueId;
        }
        return answers;
    }

    private void setAudienceMetadata(JSONObject answers) {
        // no audience meta data is set
        if (NinchatSessionManager.getInstance().getAudienceMetadata() == null) {
            NinchatSessionManager.getInstance().setAudienceMetadata(new Props());
        }
        NinchatSessionManager.getInstance().getAudienceMetadata().setObject("pre_answers", getPreAnswers(answers));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onItemLoaded(OnItemLoaded onItemLoaded) {
        for (int i = ninchatQuestionnaireAdapter.getItemCount() - 2; i >= 0; i -= 1) {
            View view = mLinearLayoutWeakReference.get().getChildAt(i);
            setViewAndChildrenEnabled(view, false);
        }
        mLinearLayoutWeakReference.get().scrollToPositionWithOffset(ninchatQuestionnaireAdapter.getItemCount() - 1, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAudienceRegistered(OnAudienceRegistered onAudienceRegistered) {
        if (pendingRequest == AUDIENCE_REGISTER) {
            pendingRequest = NONE;
            // if no audience register test is set or there is an error to register the audience
            // then skip audience text ( thank you text )
            if (TextUtils.isEmpty(thankYouText) || onAudienceRegistered.withError) {
                // close the session and exit
                NinchatSessionManager.getInstance().close();
                EventBus.getDefault().post(new OnCompleteQuestionnaire(false, queueId));
                return;
            }
            int thankYouElementIndex = mQuestionnaireList.updateQuestionWithThankYouElement(thankYouText);
            handleNext(thankYouElementIndex);
        } else if (pendingRequest == POST_ANSWERS) {
            onPostAudienceQuestion(null);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPostAudienceQuestion(OnPostAudienceQuestionnaire onPostAudienceQuestionnaire) {
        pendingRequest = NONE;
        if (NinchatSessionManager.getInstance() != null) {
            NinchatSessionManager.getInstance().partChannel();
            // delete the user if current user is a guest
            if (NinchatSessionManager.getInstance().isGuestMemeber()) {
                NinchatSessionManager.exitQueue();
            }
        }
        new Handler().postDelayed(() -> {
            if (NinchatSessionManager.getInstance() != null) {
                NinchatSessionManager.getInstance().close();
            }
            EventBus.getDefault().post(new OnCompleteQuestionnaire(false, queueId));
        }, 500);
    }

}
