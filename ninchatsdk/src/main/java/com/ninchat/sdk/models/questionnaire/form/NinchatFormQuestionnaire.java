package com.ninchat.sdk.models.questionnaire.form;

import android.os.Handler;
import android.support.v7.widget.RecyclerView;

import com.ninchat.sdk.adapters.NinchatFormQuestionnaireAdapter;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireBase;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.*;

public class NinchatFormQuestionnaire extends NinchatQuestionnaireBase<NinchatFormQuestionnaireAdapter> {
    private NinchatQuestionnaire mQuestionnaireAnswerList;

    public NinchatFormQuestionnaire(String queueId,
                                    int questionnaireType,
                                    RecyclerView recyclerView) {

        super(queueId, questionnaireType, recyclerView, null);
        // all answers will be stored here. and in conversation like questionnaire they are store in adapter ds
        mQuestionnaireAnswerList = new NinchatQuestionnaire(new JSONArray());
        ninchatQuestionnaireAdapter = new NinchatFormQuestionnaireAdapter(getInitialQuestionnaire(), true);
    }

    @Override
    protected NinchatQuestionnaire getInitialQuestionnaire() {
        JSONObject currentElement = getSlowCopy(mQuestionnaireList.getElement(0));
        mQuestionnaireAnswerList.addQuestionnaire(currentElement);
        return new NinchatQuestionnaire(getElements(currentElement));
    }

    @Override
    protected NinchatQuestionnaire getQuestionnaireAnswerList() {
        return mQuestionnaireAnswerList;
    }

    @Override
    protected void handleNext(int index) {
        if (index == -1) {
            return;
        }
        JSONObject currentElement = getSlowCopy(getElementByIndex(mQuestionnaireList.getQuestionnaireList(), index));
        mQuestionnaireAnswerList.addQuestionnaire(currentElement);
        ninchatQuestionnaireAdapter.updateContent(getElements(currentElement));
        ninchatQuestionnaireAdapter.notifyDataSetChanged();
        mRecyclerViewWeakReference.get().setAdapter(ninchatQuestionnaireAdapter);
        // scroll to top
        new Handler().post(() -> mRecyclerViewWeakReference.get().scrollToPosition(0));
    }

    @Override
    protected void handlePrevious() {
        mQuestionnaireAnswerList.removeLastElement();
        JSONObject currentElement = mQuestionnaireAnswerList.getLastElement();
        ninchatQuestionnaireAdapter.updateContent(getElements(currentElement));
        ninchatQuestionnaireAdapter.notifyDataSetChanged();
        mRecyclerViewWeakReference.get().setAdapter(ninchatQuestionnaireAdapter);
        // scroll to top
        new Handler().post(() -> mRecyclerViewWeakReference.get().scrollToPosition(0));

    }

    @Override
    protected void handleError() {
        JSONObject lastElement = mQuestionnaireAnswerList.getLastElement();
        updateRequiredFieldStats(lastElement);
        clearElementResult(lastElement);
        ninchatQuestionnaireAdapter.notifyDataSetChanged();
        int errorIndex = getFirstErrorIndex(lastElement);
        mRecyclerViewWeakReference.get().clearFocus();
        mRecyclerViewWeakReference.get().scrollToPosition(errorIndex);
    }
}
