package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    private String TAG = NinchatRadioBtnViewHolder.class.getSimpleName();
    private TextView mLabel;
    private WeakReference<RecyclerView> mRecyclerViewWeakReference;
    private JSONObject rootElement;

    public NinchatRadioBtnViewHolder(@NonNull View itemView,
                                     JSONObject questionnaireElement,
                                     boolean isFormLikeQuestionnaire) {
        super(itemView);
        mLabel = itemView.findViewById(R.id.radio_option_label);
        mRecyclerViewWeakReference = new WeakReference<>(itemView.findViewById(R.id.ninchat_chat_radio_options));
        this.bind(questionnaireElement, isFormLikeQuestionnaire);
    }


    public void bind(JSONObject questionnaireElement, boolean isFormLikeQuestionnaire) {
        this.rootElement = questionnaireElement;
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background));
        }
        String labelText = getLabel(this.rootElement);
        JSONArray optionList = getOptions(this.rootElement);
        mLabel.setText(labelText);

        mRecyclerViewWeakReference.get().setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mRecyclerViewWeakReference.get().setAdapter(new NinchatRadioBtnAdapter(optionList));
    }

    public class NinchatRadioBtnAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private JSONArray optionList;

        public NinchatRadioBtnAdapter(JSONArray optionList) {
            this.optionList = optionList;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            JSONObject currentItem = optionList.optJSONObject(position);
            return new NinchatRadioBtnTextViewHolder(
                    LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.radio_item, viewGroup, false), currentItem, position);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            JSONObject currentItem = optionList.optJSONObject(position);
            ((NinchatRadioBtnTextViewHolder) viewHolder).bind(currentItem, position);
        }

        @Override
        public int getItemCount() {
            return optionList == null ? 0 : optionList.length();
        }

        private void notifyChange() {
            notifyDataSetChanged();
        }

        public class NinchatRadioBtnTextViewHolder extends RecyclerView.ViewHolder {
            private TextView mOption;

            public NinchatRadioBtnTextViewHolder(@NonNull View itemView, JSONObject currentItem, int position) {
                super(itemView);
                mOption = itemView.findViewById(R.id.single_radio_item);
                bind(currentItem, position);
            }

            public void bind(JSONObject currentItem, int position) {
                String label = getLabel(currentItem);
                String currentValue = getValue(currentItem);
                updateRadioView(getOptionPosition(rootElement) == position);
                mOption.setText(NinchatSessionManager.getInstance().getTranslation(label));
                mOption.setOnClickListener(v -> onOptionClicked(currentItem, position));
            }

            private void updateRadioView(boolean selected) {
                mOption.setTextColor(
                        ContextCompat.getColor(
                                itemView.getContext(),
                                selected ?
                                        R.color.ninchat_color_radio_item_selected_text :
                                        R.color.ninchat_color_radio_item_unselected_text)
                );
                mOption.setBackground(
                        ContextCompat.getDrawable(
                                itemView.getContext(),
                                selected ?
                                        R.drawable.ninchat_radio_select_button :
                                        R.drawable.ninchat_ui_compose_select_button)
                );
                boolean hasError = getError(rootElement);
                if (hasError) {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.ninchat_color_error_background));
                } else {
                    mLabel.setTextColor(ContextCompat.getColor(
                            itemView.getContext(), R.color.ninchat_colorPrimary));
                }
            }

            private void onOptionClicked(JSONObject currentItem, int position) {
                String selectedValue = getValue(currentItem);
                int previousPosition = getOptionPosition(rootElement);
                boolean isNewlySelected = position != previousPosition;
                updateRadioView(isNewlySelected);

                // for options list need to select current index as well
                setPosition(rootElement, isNewlySelected ? position : -1);
                setResult(rootElement, isNewlySelected ? selectedValue : null);
                setError(rootElement, false);
                notifyChange();
                if (isNewlySelected) {
                    mayBeFireComplete(rootElement);
                }
            }

            private void mayBeFireComplete(JSONObject rootElement) {
                if (rootElement != null && rootElement.optBoolean("fireEvent", false)) {
                    EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.other));
                }
            }
        }
    }
}
