package com.ninchat.sdk.adapters;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.conversationview.NinchatConversationViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextViewHolder;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.setViewAndChildrenEnabled;

public class NinchatConversationQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = NinchatConversationQuestionnaireAdapter.class.getSimpleName();
    private NinchatQuestionnaire questionnaire;

    public NinchatConversationQuestionnaireAdapter(final NinchatQuestionnaire ninchatPreAudienceQuestionnaire) {
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
        return new NinchatConversationViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_conversation_item, parent, false),
                currentItem, position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (position + 1 == getItemCount()) {
            return;
        }
        setViewAndChildrenEnabled(viewHolder.itemView, false);
    }

    public void addContent(final JSONObject questionnaireList) {
        this.questionnaire.addQuestionnaireList(questionnaireList);
    }

    @Override
    public int getItemCount() {
        return questionnaire == null ? 0 : questionnaire.size();
    }

}
