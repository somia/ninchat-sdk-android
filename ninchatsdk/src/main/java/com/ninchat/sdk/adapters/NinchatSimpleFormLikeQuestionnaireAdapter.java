package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.formview.NinchatCheckboxViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatControlFlowViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatDropDownSelectViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatLikeRtViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatRadioBtnViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatInputFieldViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextViewHolder;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.CHECKBOX;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.EOF;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.INPUT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.LIKERT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.RADIO;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.SELECT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.TEXT;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.TEXT_AREA;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.UNKNOWN;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getItemType;

public class NinchatSimpleFormLikeQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = NinchatSimpleFormLikeQuestionnaireAdapter.class.getSimpleName();
    private NinchatQuestionnaire questionnaire;
    private Callback callback;

    public NinchatSimpleFormLikeQuestionnaireAdapter(final NinchatQuestionnaire questionnaire,
                                                     final Callback callback) {
        this.questionnaire = questionnaire;
        this.callback = callback;
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
            case UNKNOWN:
                return null;
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
                return new NinchatDropDownSelectViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        position, questionnaire);
            case CHECKBOX:
                return new NinchatCheckboxViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_simple, parent, false),
                        position, questionnaire);
            case LIKERT:
                return new NinchatLikeRtViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        position, questionnaire);
            case EOF:
                return new NinchatControlFlowViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.control_buttons, parent, false),
                        currentItem, controlFlowCallback);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof NinchatTextViewHolder) {
            ((NinchatTextViewHolder) viewHolder).bind(position, questionnaire);
        } else if (viewHolder instanceof NinchatInputFieldViewHolder) {
            ((NinchatInputFieldViewHolder) viewHolder).bind();
        } else if (viewHolder instanceof NinchatRadioBtnViewHolder) {
            ((NinchatRadioBtnViewHolder) viewHolder).update();
        } else if (viewHolder instanceof NinchatDropDownSelectViewHolder) {
            ((NinchatDropDownSelectViewHolder) viewHolder).bind();
        } else if (viewHolder instanceof NinchatCheckboxViewHolder) {
            ((NinchatCheckboxViewHolder) viewHolder).update();
        } else if (viewHolder instanceof NinchatLikeRtViewHolder) {
            ((NinchatLikeRtViewHolder) viewHolder).bind();
        }
    }

    @Override
    public int getItemCount() {
        return questionnaire.size();
    }

    private NinchatControlFlowViewHolder.Callback controlFlowCallback = new NinchatControlFlowViewHolder.Callback() {
        @Override
        public void onClickNext() {
            final int errorIndex = questionnaire.updateRequiredFieldStats();
            if (errorIndex != -1) {
                notifyDataSetChanged();
                callback.onError(errorIndex);
            } else {
                callback.onComplete();
            }
        }

        @Override
        public void onClickPrevious() {
            // todo implement
        }
    };

    public interface Callback {
        void onError(final int position);

        void onComplete();
    }
}
