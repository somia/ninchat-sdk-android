package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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
        mCheckbox = (CheckBox) itemView.findViewById(R.id.ninchat_checkbox);
        itemPosition = position;
        preAudienceQuestionnaire = new WeakReference<>(ninchatPreAudienceQuestionnaire);
        bind();
    }

    public void bind() {
        mCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
        preFill();
    }

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
            preAudienceQuestionnaire.get().setResult(item, isChecked);
            onCheckBoxStateChanged(isChecked);
        }
    };

    public void onCheckBoxStateChanged(final boolean isChecked) {
        mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(),
                isChecked ? R.color.checkbox_text_selected : R.color.checkbox_text_not_selected));
    }

    private void preFill() {
        final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
        final String label = preAudienceQuestionnaire.get().getLabel(item);
        final boolean result = preAudienceQuestionnaire.get().getResultBoolean(item);
        mCheckbox.setText(label);
        mCheckbox.setChecked(result);
    }
}
