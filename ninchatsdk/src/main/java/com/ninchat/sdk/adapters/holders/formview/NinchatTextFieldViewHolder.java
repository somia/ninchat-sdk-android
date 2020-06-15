package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
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
    private final int itemPosition;
    WeakReference<NinchatPreAudienceQuestionnaire> preAudienceQuestionnaire;

    public NinchatTextFieldViewHolder(@NonNull View itemView, final int position,
                                      final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire) {
        super(itemView);
        itemPosition = position;
        preAudienceQuestionnaire = new WeakReference<>(ninchatPreAudienceQuestionnaire);
        mLabel = (TextView) itemView.findViewById(R.id.simple_text_label);
        mEditText = (EditText) itemView.findViewById(R.id.simple_text_field);
        mEditText.addTextChangedListener(onTextChange);
        mEditText.setOnFocusChangeListener(onFocusChangeListener);
        bind();
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
            final boolean isValid = preAudienceQuestionnaire.get().isValidInput(s.toString(), pattern);
            preAudienceQuestionnaire.get().setResult(item, s.toString());
            preAudienceQuestionnaire.get().setError(item, !isValid);
            updateUI(item, true);
        }
    };

    private final View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            // update to error if input is not valid
            final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
            updateUI(item, hasFocus);
        }
    };

    public void bind() {
        final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
        setLabel(item);
        setText(item);
        updateUI(item, false);
    }

    private void setLabel(final JSONObject item) {
        final String text = preAudienceQuestionnaire.get().getLabel(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mLabel.setText(text);
    }

    private void setText(final JSONObject item) {
        final String text = preAudienceQuestionnaire.get().getResultString(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mEditText.setText(text);
    }

    private void updateUI(final JSONObject item, final boolean hasFocus) {
        final boolean hasError = preAudienceQuestionnaire.get().getError(item);
        mEditText.setBackgroundResource(hasFocus ?
                R.drawable.ninchat_border_with_focus : R.drawable.ninchat_border_with_unfocus);

        if (hasError) {
            mEditText.setBackgroundResource(R.drawable.ninchat_border_with_error);
        }
    }
}
