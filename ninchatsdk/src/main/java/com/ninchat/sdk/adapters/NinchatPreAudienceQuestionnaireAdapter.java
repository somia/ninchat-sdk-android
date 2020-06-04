package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.formview.NinchatDropDownSelectViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatRadioBtnViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextAreaViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextFieldViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextViewHolder;
import com.ninchat.sdk.helper.NinchatQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONObject;

public class NinchatPreAudienceQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = NinchatPreAudienceQuestionnaireAdapter.class.getSimpleName();
    private NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire;

    public NinchatPreAudienceQuestionnaireAdapter(final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire) {
        this.ninchatPreAudienceQuestionnaire = ninchatPreAudienceQuestionnaire;
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
                        currentItem);
            case NinchatQuestionnaire.TEXT_AREA:
                return new NinchatTextAreaViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_area_with_label, parent, false),
                        currentItem);
            case NinchatQuestionnaire.INPUT:
                return new NinchatTextFieldViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.text_field_with_label, parent, false),
                        currentItem);
            case NinchatQuestionnaire.RADIO:
                // a button like element with single choice
                return new NinchatRadioBtnViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.multichoice_with_label, parent, false),
                    currentItem);
            case NinchatQuestionnaire.SELECT:
                return new NinchatDropDownSelectViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.dropdown_with_label, parent, false),
                        currentItem);
            case NinchatQuestionnaire.CHECKBOX:
                break;

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        final JSONObject currentItem = ninchatPreAudienceQuestionnaire.getItem(position);
        if (viewHolder instanceof NinchatTextViewHolder) {
            ((NinchatTextViewHolder) viewHolder).bind(currentItem);
        } else {
            Log.d(TAG, "Unknown view type");
        }
    }

    @Override
    public int getItemCount() {
        return ninchatPreAudienceQuestionnaire.size();
    }


}
