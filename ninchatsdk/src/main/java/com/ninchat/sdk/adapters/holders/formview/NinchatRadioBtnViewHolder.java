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
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;

public class NinchatRadioBtnViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatRadioBtnViewHolder.class.getSimpleName();
    private final TextView mLabel;
    private JSONObject mRootQuestionnaireElement;
    private final WeakReference<RecyclerView> mRecyclerViewWeakReference;

    public NinchatRadioBtnViewHolder(@NonNull View itemView,
                                     final JSONObject questionnaireElement,
                                     final boolean isFormLikeQuestionnaire) {
        super(itemView);
        mLabel = itemView.findViewById(R.id.radio_option_label);
        mRecyclerViewWeakReference = new WeakReference<>(itemView.findViewById(R.id.ninchat_chat_radio_options));
        this.bind(questionnaireElement, isFormLikeQuestionnaire);
    }

    public void bind(final JSONObject questionnaireElement, final boolean isFormLikeQuestionnaire) {
        mRootQuestionnaireElement = questionnaireElement;
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background));
        }
        setLabel(questionnaireElement);
        mRecyclerViewWeakReference.get().setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mRecyclerViewWeakReference.get().setAdapter(new NinchatRadioBtnAdapter(getOptions(questionnaireElement)));
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
            return options == null ? 0 : options.length();
        }

        private void onOptionClicked(final int currentPosition) {
            final String currentSelectedValue = getValue(options.optJSONObject(currentPosition));

            final String previouslySelectedValue = getResultString(mRootQuestionnaireElement);
            final int previouslySelectedValueIndex = getPreviouslySelectedByValue(previouslySelectedValue);
            final boolean isSelected = previouslySelectedValueIndex != currentPosition;
            setResult(mRootQuestionnaireElement, isSelected ? currentSelectedValue : null);
            setError(mRootQuestionnaireElement, false);
            if (previouslySelectedValueIndex != -1)
                notifyItemChanged(previouslySelectedValueIndex);
            notifyItemChanged(currentPosition);
            if (isSelected) {
                mayBeFireComplete();
            }
        }

        private void mayBeFireComplete() {
            if (mRootQuestionnaireElement != null && mRootQuestionnaireElement.optBoolean("fireEvent", false)) {
                EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.other));
            }
        }

        private int getPreviouslySelectedByValue(final String value) {
            if (TextUtils.isEmpty(value)) {
                return -1;
            }
            for (int i = 0; i < options.length(); i += 1) {
                final JSONObject currentItem = options.optJSONObject(i);
                if (value.equalsIgnoreCase(currentItem.optString("value", ""))) {
                    return i;
                }
            }
            return -1;
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
                preFill(item);
                mOptionLabel.setOnClickListener(v -> onOptionClicked(currentPosition));
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
                final boolean hasError = getError(mRootQuestionnaireElement);
                if (hasError) {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.ninchat_color_error_background));
                } else {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.ninchat_colorPrimary));
                }
            }

            private void preFill(final JSONObject item) {
                final String label = getLabel(item);
                final String value = getValue(item);
                mOptionLabel.setText(NinchatSessionManager.getInstance().getTranslation(label));
                onSelectionChange((value == null ? "" : value).equalsIgnoreCase(getResultString(mRootQuestionnaireElement)));
            }
        }
    }

}
