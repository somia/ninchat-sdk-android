package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnNextQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getError;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getLabel;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getOptions;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getResultString;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getValue;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.setError;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.setResult;

public class NinchatRadioBtnViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatRadioBtnViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final RecyclerView mRecycleView;
    private final int rootItemPosition;
    private boolean firstTime = true;
    WeakReference<NinchatQuestionnaire> questionnaire;
    private final boolean isFormLikeQuestionnaire;

    public NinchatRadioBtnViewHolder(@NonNull View itemView, final int position,
                                     final NinchatQuestionnaire ninchatQuestionnaire,
                                     final boolean isFormLikeQuestionnaire) {
        super(itemView);
        firstTime = true;
        rootItemPosition = position;
        questionnaire = new WeakReference(ninchatQuestionnaire);
        this.isFormLikeQuestionnaire = isFormLikeQuestionnaire;
        mLabel = itemView.findViewById(R.id.radio_option_label);
        mRecycleView = itemView.findViewById(R.id.ninchat_chat_radio_options);
        update();
    }

    public void update() {
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background));
        }
        final JSONObject item = questionnaire.get().getItem(rootItemPosition);
        setLabel(item);
        mRecycleView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mRecycleView.setAdapter(new NinchatRadioBtnAdapter(getOptions(item)));
    }

    private void setLabel(final JSONObject item) {
        final String text = getLabel(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mLabel.setText(text);
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
                    final JSONObject rootItem = questionnaire.get().getItem(rootItemPosition);
                    final String value = getValue(item);
                    final String previouslySelected = getResultString(rootItem);
                    setResult(rootItem, value.equalsIgnoreCase(previouslySelected) ? null : value);
                    setError(rootItem, false);
                    firstTime = false;
                    notifyDataSetChanged();
                });
                preFill(item);
            }

            public void onSelectionChange(boolean selected) {
                mOptionLabel.setTextColor(
                        ContextCompat.getColor(
                                itemView.getContext(),
                                selected ?
                                        R.color.ninchat_color_radio_item_selected_text :
                                        R.color.ninchat_color_radio_item_unselected_text)
                );
                mOptionLabel.setBackground(
                        ContextCompat.getDrawable(
                                itemView.getContext(),
                                selected ?
                                        R.drawable.ninchat_radio_select_button :
                                        R.drawable.ninchat_ui_compose_select_button)
                );
                final JSONObject item = questionnaire.get().getItem(rootItemPosition);
                final boolean hasError = getError(item);
                if (hasError) {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.ninchat_color_error_background));
                } else {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.ninchat_colorPrimary));
                }

                if (selected && !firstTime) {
                    mayBeFireComplete();
                }
            }

            private void mayBeFireComplete() {
                final JSONObject rootItem = questionnaire.get().getItem(rootItemPosition);
                if (rootItem.optBoolean("fireEvent", false)) {
                    EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.other));
                }
            }

            private void preFill(final JSONObject item) {
                final JSONObject rootItem = questionnaire.get().getItem(rootItemPosition);
                final String label = getLabel(item);
                final String value = getValue(item);
                mOptionLabel.setText(NinchatSessionManager.getInstance().getTranslation(label));
                onSelectionChange((value == null ? "" : value).equalsIgnoreCase(getResultString(rootItem)));
            }
        }
    }

}
