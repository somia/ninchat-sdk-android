package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class NinchatQuestionnaireBaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private String TAG = NinchatQuestionnaireBaseAdapter.class.getSimpleName();
    protected NinchatQuestionnaire questionnaire;
    protected boolean isFormLikeQuestionnaire;

    public NinchatQuestionnaireBaseAdapter(NinchatQuestionnaire ninchatQuestionnaire, boolean isFormLikeQuestionnaire) {
        this.questionnaire = ninchatQuestionnaire;
        this.isFormLikeQuestionnaire = isFormLikeQuestionnaire;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
    }

    public void addContent(JSONObject questionnaireList) {
        this.questionnaire.addQuestionnaire(questionnaireList);
    }

    public void updateContent(JSONArray questionnaireList) {
        this.questionnaire.updateQuestionnaireList(questionnaireList);
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

    public NinchatQuestionnaire getQuestionnaire() {
        return this.questionnaire;
    }

    public JSONArray getQuestionnaireList() {
        return this.questionnaire == null ? null : this.questionnaire.getQuestionnaireList();
    }

    @Override
    public int getItemCount() {
        return questionnaire == null ? 0 : questionnaire.size();
    }
}
