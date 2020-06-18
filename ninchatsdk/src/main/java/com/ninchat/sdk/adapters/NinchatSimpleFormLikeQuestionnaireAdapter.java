package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.ninchat.sdk.helper.NinchatQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONObject;

public class NinchatSimpleFormLikeQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = NinchatSimpleFormLikeQuestionnaireAdapter.class.getSimpleName();
    private NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire;
    private Callback callback;

    public NinchatSimpleFormLikeQuestionnaireAdapter(final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire,
                                                     final Callback callback) {
        this.ninchatPreAudienceQuestionnaire = ninchatPreAudienceQuestionnaire;
        this.callback = callback;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        final JSONObject currentItem = ninchatPreAudienceQuestionnaire.getItem(position);
        final int viewType = NinchatQuestionnaire.getItemType(currentItem);
        switch (viewType) {
            case NinchatQuestionnaire.UNKNOWN:
                return null;
            case NinchatQuestionnaire.TEXT:
                return new NinchatTextViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_view, parent, false),
                        position, ninchatPreAudienceQuestionnaire);
            case NinchatQuestionnaire.INPUT:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_field_with_label, parent, false),
                        position, ninchatPreAudienceQuestionnaire, false);
            case NinchatQuestionnaire.TEXT_AREA:
                return new NinchatInputFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_area_with_label, parent, false),
                        position, ninchatPreAudienceQuestionnaire, true);
            case NinchatQuestionnaire.RADIO:
                // a button like element with single choice
                return new NinchatRadioBtnViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.multichoice_with_label, parent, false),
                        position, ninchatPreAudienceQuestionnaire);
            case NinchatQuestionnaire.SELECT:
                return new NinchatDropDownSelectViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        position, ninchatPreAudienceQuestionnaire);
            case NinchatQuestionnaire.CHECKBOX:
                return new NinchatCheckboxViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.checkbox_simple, parent, false),
                        position, ninchatPreAudienceQuestionnaire);
            case NinchatQuestionnaire.LIKERT:
                return new NinchatLikeRtViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        position, ninchatPreAudienceQuestionnaire);
            case NinchatQuestionnaire.EOF:
                return new NinchatControlFlowViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.control_buttons, parent, false),
                        currentItem, controlFlowCallback);

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof NinchatTextViewHolder) {
            ((NinchatTextViewHolder) viewHolder).bind(position, ninchatPreAudienceQuestionnaire);
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
        return ninchatPreAudienceQuestionnaire.size();
    }

    private NinchatControlFlowViewHolder.Callback controlFlowCallback = new NinchatControlFlowViewHolder.Callback() {
        @Override
        public void onClickNext() {
            final int errorIndex = ninchatPreAudienceQuestionnaire.updateRequiredFieldStats();
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
