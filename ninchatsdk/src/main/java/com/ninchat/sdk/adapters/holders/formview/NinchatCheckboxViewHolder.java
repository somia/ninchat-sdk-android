package com.ninchat.sdk.adapters.holders.formview;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnNextQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.setAnimation;

public class NinchatCheckboxViewHolder extends RecyclerView.ViewHolder {
    private String TAG = NinchatCheckboxViewHolder.class.getSimpleName();

    private CheckBox mCheckbox;

    public NinchatCheckboxViewHolder(@NonNull View itemView,
                                     JSONObject questionnaireElement,
                                     boolean isFormLikeQuestionnaire,
                                     int position) {
        super(itemView);
        mCheckbox = itemView.findViewById(R.id.ninchat_checkbox);
        bind(questionnaireElement, isFormLikeQuestionnaire, position, false);
    }

    public void bind(JSONObject questionnaireElement, boolean isFormLikeQuestionnaire, int position, boolean isUpdate) {
        mCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setResult(questionnaireElement, isChecked);
            setError(questionnaireElement, false);
            updateUI(questionnaireElement);
            if (isChecked) {
                mayBeFireComplete(questionnaireElement);
            }
        });
        setLabel(questionnaireElement);
        setChecked(questionnaireElement);
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background));
        }
        updateUI(questionnaireElement);
        if (!isUpdate)
            setAnimation(itemView, position, position != 0);
    }

    private void setLabel(JSONObject item) {
        String text = getLabel(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mCheckbox.setText(NinchatSessionManager.getInstance().getTranslation(text));
    }

    private void setChecked(JSONObject item) {
        boolean result = getResultBoolean(item);
        mCheckbox.setChecked(result);
    }

    private void updateUI(JSONObject item) {
        boolean hasError = getError(item);
        boolean isChecked = getResultBoolean(item);
        mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(),
                isChecked ? R.color.ninchat_color_checkbox_selected : R.color.ninchat_color_checkbox_unselected));

        // focus will get priority
        if (hasError) {
            mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ninchat_color_error_background));
        }
    }

    private void mayBeFireComplete(JSONObject item) {
        if (item != null && item.optBoolean("fireEvent", false)) {
            EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }

}
