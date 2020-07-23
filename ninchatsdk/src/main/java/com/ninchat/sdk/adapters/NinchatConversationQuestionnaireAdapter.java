package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.conversationview.NinchatConversationViewHolder;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.setViewAndChildrenEnabled;

public class NinchatConversationQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String TAG = NinchatConversationQuestionnaireAdapter.class.getSimpleName();
    private NinchatQuestionnaire questionnaire;

    public NinchatConversationQuestionnaireAdapter(NinchatQuestionnaire questionnaire) {
        // expect list of questionnaire with object. later from the bot view holder we will expand to elements
        this.questionnaire = questionnaire;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        JSONObject currentItem = questionnaire.getItem(position);
        return new NinchatConversationViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_conversation_item, parent, false),
                currentItem, position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (position + 1 == getItemCount()) {
            setViewAndChildrenEnabled(viewHolder.itemView, true);
        } else {
            setViewAndChildrenEnabled(viewHolder.itemView, false);
        }

    }

    public void addContent(JSONObject questionnaireList) {
        this.questionnaire.addQuestionnaire(questionnaireList);
    }

    public JSONObject getLastElement() {
        return this.questionnaire.getLastElement();
    }

    public JSONObject getSecondLastElement() {
        return this.questionnaire.getSecondLastElement();
    }

    public void removeLast() {
        int position = getItemCount() - 1;
        this.questionnaire.removeQuestionnaireList(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return questionnaire == null ? 0 : questionnaire.size();
    }

    public NinchatQuestionnaire getQuestionnaire() {
        return this.questionnaire;
    }

    public JSONArray getQuestionnaireList() {
        return this.questionnaire.getQuestionnaireList();
    }
}
