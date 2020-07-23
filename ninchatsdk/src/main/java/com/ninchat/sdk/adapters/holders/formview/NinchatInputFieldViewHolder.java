package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ninchat.sdk.R;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.matchPattern;

public class NinchatInputFieldViewHolder extends RecyclerView.ViewHolder {
    private String TAG = NinchatInputFieldViewHolder.class.getSimpleName();

    private TextView mLabel;
    private EditText mEditText;

    public NinchatInputFieldViewHolder(@NonNull View itemView,
                                       JSONObject questionnaireElement,
                                       boolean multilineText,
                                       boolean isFormLikeQuestionnaire) {
        super(itemView);
        mLabel = itemView.findViewById(multilineText ? R.id.multiline_text_label : R.id.simple_text_label);
        mEditText = itemView.findViewById(multilineText ? R.id.multiline_text_area : R.id.simple_text_field);
        bind(questionnaireElement, isFormLikeQuestionnaire);
    }

    public void bind(JSONObject questionnaireElement, boolean isFormLikeQuestionnaire) {
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // pass
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // pass
            }

            @Override
            public void afterTextChanged(Editable s) {
                setResult(questionnaireElement, s.toString());
                setError(questionnaireElement, matchPattern(questionnaireElement) == false);
                updateUI(questionnaireElement, true);
            }
        });
        mEditText.setOnFocusChangeListener((v, hasFocus) -> {
            // update to error if input is not valid
            updateUI(questionnaireElement, hasFocus);
        });
        setLabel(questionnaireElement);
        setText(questionnaireElement);
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background));
        }
        updateUI(questionnaireElement, false);
    }

    private void setLabel(JSONObject item) {
        String text = getLabel(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mLabel.setText(text);
    }

    private void setText(JSONObject item) {
        String text = getResultString(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mEditText.setText(text);
    }

    private void updateUI(JSONObject item, boolean hasFocus) {
        boolean hasError = getError(item);
        mEditText.setBackgroundResource(hasFocus ?
                R.drawable.ninchat_border_with_focus : R.drawable.ninchat_border_with_unfocus);
        if (hasError) {
            mEditText.setBackgroundResource(R.drawable.ninchat_border_with_error);
        }
    }
}
