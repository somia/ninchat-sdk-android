package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.formview.NinchatButtonViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatCheckboxViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatDropDownSelectViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatInputFieldViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatRadioBtnViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextViewHolder;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatFormQuestionnaireAdapter extends NinchatQuestionnaireBaseAdapter {
    private String TAG = NinchatFormQuestionnaireAdapter.class.getSimpleName();

    public NinchatFormQuestionnaireAdapter(NinchatQuestionnaire ninchatQuestionnaire, boolean isFormLikeQuestionnaire) {
        super(ninchatQuestionnaire, isFormLikeQuestionnaire);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        JSONObject currentItem = questionnaire.getItem(position);
        int viewType = getItemType(currentItem);
        switch (viewType) {
            case TEXT:
                return new NinchatTextViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view, parent, false),
                        currentItem, isFormLikeQuestionnaire, position);
            case INPUT:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_field_with_label, parent, false),
                        currentItem, false, isFormLikeQuestionnaire, position);
            case TEXT_AREA:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_area_with_label, parent, false),
                        currentItem, true, isFormLikeQuestionnaire, position);
            case RADIO:
                return new NinchatRadioBtnViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.multichoice_with_label, parent, false),
                        currentItem, isFormLikeQuestionnaire, position);
            case SELECT:
            case LIKERT:
                return new NinchatDropDownSelectViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        currentItem, isFormLikeQuestionnaire, position);
            case CHECKBOX:
                return new NinchatCheckboxViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_simple, parent, false),
                        currentItem, isFormLikeQuestionnaire, position);
            case BUTTON:
                return new NinchatButtonViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.control_buttons, parent, false),
                        currentItem, isFormLikeQuestionnaire, position);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        JSONObject currentItem = questionnaire.getItem(position);
        if (viewHolder instanceof NinchatTextViewHolder) {
            ((NinchatTextViewHolder) viewHolder).bind(currentItem, this.isFormLikeQuestionnaire, position, true);
        } else if (viewHolder instanceof NinchatInputFieldViewHolder) {
            ((NinchatInputFieldViewHolder) viewHolder).bind(currentItem, this.isFormLikeQuestionnaire, position, true);
        } else if (viewHolder instanceof NinchatDropDownSelectViewHolder) {
            ((NinchatDropDownSelectViewHolder) viewHolder).bind(currentItem, this.isFormLikeQuestionnaire, position, true);
        } else if (viewHolder instanceof NinchatRadioBtnViewHolder) {
            ((NinchatRadioBtnViewHolder) viewHolder).bind(currentItem, this.isFormLikeQuestionnaire, position, true);
        } else if (viewHolder instanceof NinchatCheckboxViewHolder) {
            ((NinchatCheckboxViewHolder) viewHolder).bind(currentItem, this.isFormLikeQuestionnaire, position, true);
        } else if (viewHolder instanceof NinchatButtonViewHolder) {
            ((NinchatButtonViewHolder) viewHolder).bind(currentItem, this.isFormLikeQuestionnaire, position, true);
        }
    }
}
