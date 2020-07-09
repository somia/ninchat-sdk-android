package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.NinchatBaseViewHolder;
import com.ninchat.sdk.adapters.holders.conversationview.NinchaBotViewHolder;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatConversationLikeQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = NinchatConversationLikeQuestionnaireAdapter.class.getSimpleName();
    private NinchatQuestionnaire questionnaire;

    public NinchatConversationLikeQuestionnaireAdapter(final NinchatQuestionnaire ninchatPreAudienceQuestionnaire) {
        // expect list of questionnaire with object. later from the bot view holder we will expand to elements
        this.questionnaire = ninchatPreAudienceQuestionnaire;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        final JSONObject currentItem = questionnaire.getItem(position);
        return new NinchaBotViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_conversation_item, parent, false),
                position,
                currentItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Log.d(TAG, "on bind view holder " + position);
    }

    public void updateContent(final JSONArray questionnaireList) {
        this.questionnaire.updateQuestionnaireList(questionnaireList);
    }

    public void addContent(final JSONObject questionnaireList) {
        this.questionnaire.addQuestionnaireList(questionnaireList);
    }

    @Override
    public int getItemCount() {
        return questionnaire.size();
    }
}
