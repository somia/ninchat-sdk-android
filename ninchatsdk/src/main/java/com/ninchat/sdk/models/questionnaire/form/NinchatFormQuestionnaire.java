package com.ninchat.sdk.models.questionnaire.form;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatConversationQuestionnaireAdapter;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONArray;

public class NinchatFormQuestionnaire {
    private NinchatConversationQuestionnaireAdapter mNinchatConversationQuestionnaireAdapter;

    public NinchatFormQuestionnaire() {

    }

    public void createAdapter(final JSONArray questionnaireList) {
        mNinchatConversationQuestionnaireAdapter = new NinchatConversationQuestionnaireAdapter(
                new NinchatQuestionnaire(questionnaireList));

    }

    public void setAdapter(final Context mContext, final RecyclerView mRecyclerView) {
        final int spaceInPixelTop = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_items_margin_top);
        final int spaceLeft = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_items_margin_left);
        final int spaceRight = mContext.getResources().getDimensionPixelSize(R.dimen.ninchat_items_margin_right);
        mRecyclerView.addItemDecoration(new NinchatQuestionnaireItemDecoration(
                spaceInPixelTop,
                spaceLeft,
                spaceRight
        ));
        mRecyclerView.setAdapter(this.mNinchatConversationQuestionnaireAdapter);

    }
}
