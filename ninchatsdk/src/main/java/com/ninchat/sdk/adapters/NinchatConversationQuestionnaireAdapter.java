package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.conversationview.NinchatConversationViewHolder;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.setViewAndChildrenEnabled;

public class NinchatConversationQuestionnaireAdapter extends NinchatQuestionnaireBaseAdapter {
    private String TAG = NinchatConversationQuestionnaireAdapter.class.getSimpleName();

    public NinchatConversationQuestionnaireAdapter(NinchatQuestionnaire ninchatQuestionnaire, Pair<String, String> botDetails) {
        // expect list of questionnaire with object. later from the bot view holder we will expand to elements
        super(ninchatQuestionnaire, botDetails, false);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        JSONObject currentItem = questionnaire.getItem(position);
        return new NinchatConversationViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_conversation_item, parent, false),
                currentItem, botDetails, position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (position + 1 >= getItemCount()) {
            setViewAndChildrenEnabled(viewHolder.itemView, true);
        } else {
            setViewAndChildrenEnabled(viewHolder.itemView, false);
        }
    }
}
