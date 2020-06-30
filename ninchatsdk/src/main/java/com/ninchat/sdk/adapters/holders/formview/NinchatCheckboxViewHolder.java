package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnRequireStepChange;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getError;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getLabel;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getResultBoolean;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.setError;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.setResult;

public class NinchatCheckboxViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatCheckboxViewHolder.class.getSimpleName();

    private final CheckBox mCheckbox;
    private final int itemPosition;
    WeakReference<NinchatQuestionnaire> questionnaire;

    public NinchatCheckboxViewHolder(@NonNull View itemView, final int position,
                                     final NinchatQuestionnaire ninchatQuestionnaire) {
        super(itemView);
        itemPosition = position;
        questionnaire = new WeakReference(ninchatQuestionnaire);
        mCheckbox = (CheckBox) itemView.findViewById(R.id.ninchat_checkbox);
        mCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
        update();
    }

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            final JSONObject item = questionnaire.get().getItem(itemPosition);
            setResult(item, isChecked);
            setError(item, false);
            updateUI(item);
            if (isChecked) {
                mayBeFireComplete();
            }
        }
    };

    public void update() {
        final JSONObject item = questionnaire.get().getItem(itemPosition);
        setLabel(item);
        setChecked(item);
        updateUI(item);
    }

    private void setLabel(final JSONObject item) {
        final String text = getLabel(item);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        mCheckbox.setText(text);
    }

    private void setChecked(final JSONObject item) {
        final boolean result = getResultBoolean(item);
        mCheckbox.setChecked(result);
    }

    private void updateUI(final JSONObject item) {
        final boolean hasError = getError(item);
        final boolean isChecked = getResultBoolean(item);
        mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(),
                isChecked ? R.color.checkbox_text_selected : R.color.checkbox_text_not_selected));

        // focus will get priority
        if (hasError) {
            mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.ninchat_color_error_background));
        }
    }

    private void mayBeFireComplete() {
        final JSONObject rootItem = questionnaire.get().getItem(itemPosition);
        if (rootItem.optBoolean("fireEvent", false)) {
            EventBus.getDefault().post(new OnRequireStepChange(OnRequireStepChange.other));
        }
    }

}
