package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class NinchatRadioBtnViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatRadioBtnViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final RecyclerView mRecycleView;
    private final int rootItemPosition;

    WeakReference<NinchatPreAudienceQuestionnaire> preAudienceQuestionnaire;

    public NinchatRadioBtnViewHolder(@NonNull View itemView, final int position,
                                     final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire) {
        super(itemView);
        rootItemPosition = position;
        preAudienceQuestionnaire = new WeakReference<>(ninchatPreAudienceQuestionnaire);
        mLabel = (TextView) itemView.findViewById(R.id.radio_option_label);
        mRecycleView = (RecyclerView) itemView.findViewById(R.id.ninchat_chat_radio_options);
        this.bind(position);
    }

    public void bind(final int position) {
        final JSONObject item = preAudienceQuestionnaire.get().getItem(rootItemPosition);
        mRecycleView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mRecycleView.setAdapter(new NinchatRadioBtnAdapter(
                preAudienceQuestionnaire.get().getOptions(item)));
        preFill(item);
    }

    private void preFill(final JSONObject item) {
        final String label = preAudienceQuestionnaire.get().getLabel(item);
        mLabel.setText(label);
    }

    public class NinchatRadioBtnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private JSONArray options;

        public NinchatRadioBtnAdapter(final JSONArray options) {
            this.options = options;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            final JSONObject currentItem = this.options.optJSONObject(position);
            return new NinchatRadioBtnTextViewHolder(
                    LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.radio_item, viewGroup, false),
                    currentItem, position);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            final JSONObject currentItem = this.options.optJSONObject(position);
            if (viewHolder instanceof NinchatRadioBtnTextViewHolder) {
                ((NinchatRadioBtnTextViewHolder) viewHolder).bind(currentItem, position);
            }
        }

        @Override
        public int getItemCount() {
            return options.length();
        }

        public class NinchatRadioBtnTextViewHolder extends RecyclerView.ViewHolder {
            private final TextView mOptionLabel;

            public NinchatRadioBtnTextViewHolder(@NonNull View itemView,
                                                 final JSONObject items, final int currentPosition) {
                super(itemView);
                mOptionLabel = (TextView) itemView.findViewById(R.id.single_radio_item);
                this.bind(items, currentPosition);
            }

            public void bind(final JSONObject item, final int currentPosition) {
                mOptionLabel.setOnClickListener(v -> {
                    final JSONObject rootItem = preAudienceQuestionnaire.get().getItem(rootItemPosition);
                    final int previouslySelected = preAudienceQuestionnaire.get().getResultInt(rootItem);
                    preAudienceQuestionnaire.get().setResult(rootItem, previouslySelected == currentPosition ? -1 : currentPosition);
                    notifyDataSetChanged();
                });
                preFill(item, currentPosition);
            }

            public void onSelectionChange(boolean selected) {
                mOptionLabel.setTextColor(
                        ContextCompat.getColor(
                                itemView.getContext(),
                                selected ?
                                        R.color.checkbox_selected :
                                        R.color.ninchat_color_ui_compose_select_unselected_text)
                );
                mOptionLabel.setBackground(
                        ContextCompat.getDrawable(
                                itemView.getContext(),
                                selected ?
                                        R.drawable.ninchat_radio_select_button :
                                        R.drawable.ninchat_ui_compose_select_button)
                );
            }

            private void preFill(final JSONObject item, final int currentPosition) {
                final JSONObject rootItem = preAudienceQuestionnaire.get().getItem(rootItemPosition);
                final String label = preAudienceQuestionnaire.get().getLabel(item);
                mOptionLabel.setText(label);
                onSelectionChange(preAudienceQuestionnaire.get().getResultInt(rootItem) == currentPosition);
            }
        }
    }
}
