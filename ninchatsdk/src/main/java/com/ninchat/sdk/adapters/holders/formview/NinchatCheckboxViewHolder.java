package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class NinchatCheckboxViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatCheckboxViewHolder.class.getSimpleName();

    private final CheckBox mCheckbox;
    private final int itemPosition;
    WeakReference<NinchatPreAudienceQuestionnaire> preAudienceQuestionnaire;

    public NinchatCheckboxViewHolder(@NonNull View itemView, final int position,
                                     final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire) {
        super(itemView);
        itemPosition = position;
        preAudienceQuestionnaire = new WeakReference<>(ninchatPreAudienceQuestionnaire);
        mCheckbox = (CheckBox) itemView.findViewById(R.id.ninchat_checkbox);
        mCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
        update();
    }

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
            preAudienceQuestionnaire.get().setResult(item, isChecked);
            preAudienceQuestionnaire.get().setError(item, false);
            updateUI(item);
        }
    };

    public void update() {
        final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
        setLabel(item);
        setChecked(item);
        updateUI(item);
    }

    private void setLabel(final JSONObject item) {
        final String text = preAudienceQuestionnaire.get().getLabel(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mCheckbox.setText(text);
    }

    private void setChecked(final JSONObject item) {
        final boolean result = preAudienceQuestionnaire.get().getResultBoolean(item);
        mCheckbox.setChecked(result);
    }

    private void updateUI(final JSONObject item) {
        final boolean hasError = preAudienceQuestionnaire.get().getError(item);
        final boolean isChecked = preAudienceQuestionnaire.get().getResultBoolean(item);
        mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(),
                isChecked ? R.color.checkbox_text_selected : R.color.checkbox_text_not_selected));

        // focus will get priority
        if (hasError) {
            mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ninchat_color_error_background));
        }
    }

}
