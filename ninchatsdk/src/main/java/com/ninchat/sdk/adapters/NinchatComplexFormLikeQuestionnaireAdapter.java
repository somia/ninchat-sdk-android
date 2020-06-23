package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.formview.NinchatCheckboxViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatControlFlowViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatDropDownSelectViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatInputFieldViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatLikeRtViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatRadioBtnViewHolder;
import com.ninchat.sdk.adapters.holders.formview.NinchatTextViewHolder;
import com.ninchat.sdk.helper.NinchatQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONObject;

public class NinchatComplexFormLikeQuestionnaireAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String TAG = NinchatComplexFormLikeQuestionnaireAdapter.class.getSimpleName();
    private NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire;
    private Callback callback;

    public NinchatComplexFormLikeQuestionnaireAdapter(final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire,
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

        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

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
