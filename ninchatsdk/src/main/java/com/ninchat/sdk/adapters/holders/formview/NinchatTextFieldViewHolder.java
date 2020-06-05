package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ninchat.client.JSON;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class NinchatTextFieldViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatTextFieldViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final EditText mEditText;
    private Boolean hasError;
    private JSONObject currentItem;
    private WeakReference<NinchatPreAudienceQuestionnaire> questionnaireWeakReference;

    public NinchatTextFieldViewHolder(@NonNull View itemView, final JSONObject item, final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire) {
        super(itemView);
        hasError = false;
        currentItem = item;

        mLabel = (TextView) itemView.findViewById(R.id.simple_text_label);
        mEditText = (EditText) itemView.findViewById(R.id.simple_text_field);
        this.questionnaireWeakReference = new WeakReference<>(ninchatPreAudienceQuestionnaire);
        this.bind(item);
    }


    public void bind(JSONObject item) {
        mLabel.setText(item.optString("label", ""));
        mEditText.addTextChangedListener(textWatcher);
        mEditText.setOnFocusChangeListener(onFocusChangeListener);
    }

    public final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // try to validate the current input if there is a pattern
            final String pattern = questionnaireWeakReference.get().getPattern(currentItem);
            final boolean isValid = questionnaireWeakReference.get().isValidInput(s == null ? null : s.toString(), pattern);
            mEditText.setBackgroundResource(isValid ?
                    R.drawable.ninchat_border_with_focus : R.drawable.ninchat_border_with_error);
            hasError = !isValid;
        }
    };

    private final View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // is has an error then don't change the focus
            if (hasError) {
                return;
            }
            mEditText.setBackgroundResource(hasFocus ?
                    R.drawable.ninchat_border_with_focus : R.drawable.ninchat_border_with_unfocus);
        }
    };
}
