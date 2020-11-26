package com.ninchat.sdk.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.view.NinchatButtonViewHolder;
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.view.NinchatCheckboxViewHolder;
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view.NinchatDropDownSelectViewHolder;
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.view.NinchatInputFieldViewHolder;
import com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.view.NinchatRadioButtonListView;
import com.ninchat.sdk.ninchatquestionnaire.ninchattextviewholder.view.NinchatTextViewHolder;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.*;

public class NinchatFormQuestionnaireAdapter extends NinchatQuestionnaireBaseAdapter {
    private String TAG = NinchatFormQuestionnaireAdapter.class.getSimpleName();

    public NinchatFormQuestionnaireAdapter(NinchatQuestionnaire ninchatQuestionnaire, boolean isFormLikeQuestionnaire) {
        super(ninchatQuestionnaire, null, isFormLikeQuestionnaire);
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
                        currentItem,
                        isFormLikeQuestionnaire);
            case INPUT:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_field_with_label, parent, false),
                        currentItem, false, isFormLikeQuestionnaire);
            case TEXT_AREA:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_area_with_label, parent, false),
                        currentItem, true, isFormLikeQuestionnaire);
            case RADIO:
                return new NinchatRadioButtonListView(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.multichoice_with_label, parent, false),
                        currentItem, isFormLikeQuestionnaire);
            case SELECT:
            case LIKERT:
                return new NinchatDropDownSelectViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        currentItem, isFormLikeQuestionnaire);
            case CHECKBOX:
                return new NinchatCheckboxViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_simple, parent, false),
                        currentItem, isFormLikeQuestionnaire);
            case BUTTON:
                return new NinchatButtonViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.control_buttons, parent, false),
                        currentItem);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        JSONObject currentItem = questionnaire.getItem(position);
        if (viewHolder instanceof NinchatTextViewHolder) {
            ((NinchatTextViewHolder) viewHolder).update(currentItem);
        } else if (viewHolder instanceof NinchatInputFieldViewHolder) {
            ((NinchatInputFieldViewHolder) viewHolder).update(currentItem);
        } else if (viewHolder instanceof NinchatDropDownSelectViewHolder) {
            ((NinchatDropDownSelectViewHolder) viewHolder).update(currentItem);
        } else if (viewHolder instanceof NinchatRadioButtonListView) {
            ((NinchatRadioButtonListView) viewHolder).update(currentItem);
        } else if (viewHolder instanceof NinchatCheckboxViewHolder) {
            ((NinchatCheckboxViewHolder) viewHolder).update(currentItem);
        } else if (viewHolder instanceof NinchatButtonViewHolder) {
            ((NinchatButtonViewHolder) viewHolder).update(currentItem);
        }
    }
}
