package com.ninchat.sdk.models.questionnaire.conversation;

import android.os.Handler;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ninchat.sdk.adapters.NinchatConversationQuestionnaireAdapter;
import com.ninchat.sdk.events.OnComponentError;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaireBase;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireNavigationUtil.*;

public class NinchatConversationQuestionnaire extends NinchatQuestionnaireBase<NinchatConversationQuestionnaireAdapter> {
    private boolean markDirty = false;
    public NinchatConversationQuestionnaire(String queueId,
                                            int questionnaireType,
                                            Pair<String, String> botDetails,
                                            RecyclerView recyclerView,
                                            LinearLayoutManager linearLayout) {
        super(queueId, questionnaireType, recyclerView, linearLayout);
        ninchatQuestionnaireAdapter = new NinchatConversationQuestionnaireAdapter(getInitialQuestionnaire(), botDetails);
    }

    @Override
    protected NinchatQuestionnaire getInitialQuestionnaire() {
        JSONArray retval = new JSONArray();
        JSONObject currentElement = getSlowCopy(mQuestionnaireList.getElement(0));
        retval.put(getSlowCopy(currentElement));
        return new NinchatQuestionnaire(retval);
    }

    protected NinchatQuestionnaire getQuestionnaireAnswerList() {
        return ninchatQuestionnaireAdapter.getQuestionnaire();
    }

    @Override
    protected void handleNext(int index) {
        if (index == -1) {
            // if it is still a no match then do nothing
            return;
        }
        if (markDirty) {
            mRecyclerViewWeakReference.get().getRecycledViewPool().clear();
        }
        markDirty = false;
        JSONObject currentElement = getSlowCopy(getElementByIndex(mQuestionnaireList.getQuestionnaireList(), index));
        ninchatQuestionnaireAdapter.addContent(currentElement);
        ninchatQuestionnaireAdapter.notifyDataSetChanged();
        mRecyclerViewWeakReference.get().scrollToPosition(ninchatQuestionnaireAdapter.getItemCount() - 1);
    }

    @Override
    protected void handlePrevious() {
        // remove last element
        markDirty = true;
        ninchatQuestionnaireAdapter.removeLast();
        ninchatQuestionnaireAdapter.notifyDataSetChanged();
        new Handler().postDelayed(()-> {
            mRecyclerViewWeakReference.get().scrollToPosition(ninchatQuestionnaireAdapter.getItemCount() - 1);
        },200);
    }

    @Override
    protected void handleError() {
        JSONObject lastElement = ninchatQuestionnaireAdapter.getLastElement();
        updateRequiredFieldStats(lastElement);
        String itemName = getErrorItemName(lastElement);
        clearElementResult(lastElement);
        // mRecyclerViewWeakReference.get().clearFocus();
        EventBus.getDefault().post(new OnComponentError(itemName));
    }
}
