package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.formview.NinchatButtonViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatCheckboxViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatDropDownSelectViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatInputFieldViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatRadioBtnViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextViewHolder;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.BUTTON;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.CHECKBOX;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.INPUT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.LIKERT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.RADIO;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.SELECT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.TEXT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.TEXT_AREA;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getItemType;

public class NinchatFormLikeQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = NinchatFormLikeQuestionnaireAdapter.class.getSimpleName();
    private NinchatQuestionnaire questionnaire;

    public NinchatFormLikeQuestionnaireAdapter(final com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire ninchatPreAudienceQuestionnaire) {
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
        final int viewType = getItemType(currentItem);
        switch (viewType) {
            case TEXT:
                return new NinchatTextViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view, parent, false),
                        position, questionnaire);
            case INPUT:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_field_with_label, parent, false),
                        position, questionnaire, false);
            case TEXT_AREA:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_area_with_label, parent, false),
                        position, questionnaire, true);
            case RADIO:
                // a button like element with single choice
                return new NinchatRadioBtnViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.multichoice_with_label, parent, false),
                        position, questionnaire);
            case SELECT:
            case LIKERT:
                return new NinchatDropDownSelectViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        position, questionnaire);
            case CHECKBOX:
                return new NinchatCheckboxViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_simple, parent, false),
                        position, questionnaire);
            case BUTTON:
                return new NinchatButtonViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.control_buttons, parent, false),
                        position, questionnaire);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Log.d(TAG, "on bind view holder " + position);
    }

    public void updateContent(final JSONArray questionnaireList) {
        this.questionnaire.updateQuestionnaireList(questionnaireList);
    }

    @Override
    public int getItemCount() {
        return questionnaire.size();
    }
}
