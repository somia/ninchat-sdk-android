package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class NinchatTextFieldViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatTextFieldViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final EditText mEditText;
    private Boolean hasError;
    private final int itemPosition;
    WeakReference<NinchatPreAudienceQuestionnaire> preAudienceQuestionnaire;

    public NinchatTextFieldViewHolder(@NonNull View itemView, final int position,
                                      final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire) {
        super(itemView);
        hasError = false;
        itemPosition = position;
        preAudienceQuestionnaire = new WeakReference<>(ninchatPreAudienceQuestionnaire);
        mLabel = (TextView) itemView.findViewById(R.id.simple_text_label);
        mEditText = (EditText) itemView.findViewById(R.id.simple_text_field);
        this.bind(position);
    }

    public void bind(final int position) {
        mEditText.addTextChangedListener(onTextChange);
        mEditText.setOnFocusChangeListener(onFocusChangeListener);
        preFill();
    }

    public final TextWatcher onTextChange = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // try to validate the current input if there is a pattern
            final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
            final String pattern = preAudienceQuestionnaire.get().getPattern(item);
            final boolean isValid = preAudienceQuestionnaire.get().isValidInput(s == null ? null : s.toString(), pattern);
            preAudienceQuestionnaire.get().setResult(item, s.toString());
            mEditText.setBackgroundResource(isValid ?
                    R.drawable.ninchat_border_with_focus : R.drawable.ninchat_border_with_error);
            hasError = !isValid;
        }
    };

    private final View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            mEditText.setBackgroundResource(hasFocus ?
                    R.drawable.ninchat_border_with_focus : R.drawable.ninchat_border_with_unfocus);

            if (hasError) {
                mEditText.setBackgroundResource(R.drawable.ninchat_border_with_error);
            }
        }
    };

    private void preFill() {
        final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
        final String pattern = preAudienceQuestionnaire.get().getPattern(item);
        final String label = preAudienceQuestionnaire.get().getLabel(item);
        final String result = preAudienceQuestionnaire.get().getResult(item);
        mLabel.setText(label);
        if (result == null) {
            return;
        }
        mEditText.setText(result);
        final boolean isValid = preAudienceQuestionnaire.get().isValidInput(result, pattern);
        hasError = !isValid;
        mEditText.setBackgroundResource(hasError ?
                R.drawable.ninchat_border_with_error : R.drawable.ninchat_border_with_unfocus);
    }
}
