package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnNextQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;

public class NinchatDropDownSelectViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatDropDownSelectViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final Spinner mSpinner;
    private int itemPosition;
    WeakReference<NinchatQuestionnaire> questionnaire;
    private final boolean isFormLikeQuestionnaire;

    public NinchatDropDownSelectViewHolder(@NonNull View itemView, final int position,
                                           final NinchatQuestionnaire ninchatQuestionnaire,
                                           final boolean isFormLikeQuestionnaire) {
        super(itemView);
        mLabel = itemView.findViewById(R.id.dropdown_text_label);
        mSpinner = itemView.findViewById(R.id.ninchat_dropdown_list);
        itemPosition = position;
        questionnaire = new WeakReference(ninchatQuestionnaire);
        this.isFormLikeQuestionnaire = isFormLikeQuestionnaire;
        bind();
    }


    public void bind() {
        final int previouslySelected = preFill();
        final ArrayAdapter<String> dataAdapter = getRTDataAdapter();
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background));
        }
        mSpinner.setAdapter(dataAdapter);
        mSpinner.setSelection(previouslySelected);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final JSONObject rootItem = questionnaire.get().getItem(itemPosition);
                final String result = getOptionValueByIndex(rootItem, position - 1);
                setResult(rootItem, result);
                final TextView mTextView = (TextView) parent.getChildAt(0);
                if (position != 0) {
                    setError(rootItem, false);
                    onSelected(true, mTextView);
                    mayBeFireComplete();
                } else {
                    onSelected(false, mTextView);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "nothing is selected");
            }
        });
    }

    public void onSelected(boolean selected, final TextView mTextView) {
        final JSONObject item = questionnaire.get().getItem(itemPosition);
        final boolean hasError = getError(item);

        ((RelativeLayout) itemView.findViewById(R.id.dropdown_select_layout)).setBackground(
                ContextCompat.getDrawable(itemView.getContext(),
                        selected ? R.drawable.ninchat_dropdown_border_select : R.drawable.ninchat_dropdown_border_not_selected));

        ((ImageView) itemView.findViewById(R.id.ninchat_dropdown_list_icon)).setColorFilter(
                ContextCompat.getColor(itemView.getContext(),
                        selected ? R.color.ninchat_color_dropdown_selected_text : R.color.ninchat_color_dropdown_unselected_text)
        );

        mTextView.setTextColor(ContextCompat.getColor(
                itemView.getContext(),
                selected ?
                        R.color.ninchat_color_dropdown_selected_text :
                        R.color.ninchat_color_dropdown_unselected_text));

        if (hasError) {
            ((RelativeLayout) itemView.findViewById(R.id.dropdown_select_layout)).setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_dropdown_border_with_error));
            ((ImageView) itemView.findViewById(R.id.ninchat_dropdown_list_icon)).setColorFilter(
                    ContextCompat.getColor(itemView.getContext(), R.color.ninchat_color_error_background)
            );
            mTextView.setTextColor(ContextCompat.getColor(
                    itemView.getContext(), R.color.ninchat_color_error_background));
        }
    }

    private int preFill() {
        final JSONObject item = questionnaire.get().getItem(itemPosition);
        final String label = getLabel(item);
        mLabel.setText(label);
        final int index = getOptionIndex(item, getResultString(item));
        return Math.max(0, index + 1);
    }

    private ArrayAdapter<String> getRTDataAdapter() {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.dropdown_item_text_view);
        final JSONObject item = questionnaire.get().getItem(itemPosition);
        final JSONArray options = getOptions(item);
        dataAdapter.add(NinchatSessionManager.getInstance().getTranslation("Select"));
        for (int i = 0; i < options.length(); i += 1) {
            final JSONObject curOption = options.optJSONObject(i);
            final String label = curOption.optString("label");
            dataAdapter.add(NinchatSessionManager.getInstance().getTranslation(label));
        }
        return dataAdapter;
    }

    private void mayBeFireComplete() {
        final JSONObject rootItem = questionnaire.get().getItem(itemPosition);
        if (rootItem.optBoolean("fireEvent", false)) {
            EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }
}
