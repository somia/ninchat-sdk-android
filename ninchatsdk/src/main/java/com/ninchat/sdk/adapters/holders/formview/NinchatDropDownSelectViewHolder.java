package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnNextQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemSetter.*;

public class NinchatDropDownSelectViewHolder extends RecyclerView.ViewHolder {
    private String TAG = NinchatDropDownSelectViewHolder.class.getSimpleName();

    private TextView mLabel;
    private Spinner mSpinner;

    public NinchatDropDownSelectViewHolder(@NonNull View itemView,
                                           JSONObject questionnaireElement,
                                           boolean isFormLikeQuestionnaire) {
        super(itemView);
        mLabel = itemView.findViewById(R.id.dropdown_text_label);
        mSpinner = itemView.findViewById(R.id.ninchat_dropdown_list);
        bind(questionnaireElement, isFormLikeQuestionnaire);
    }


    public void bind(JSONObject questionnaireElement, boolean isFormLikeQuestionnaire) {
        int previouslySelected = preFill(questionnaireElement);
        ArrayAdapter<String> dataAdapter = getRTDataAdapter(questionnaireElement);
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background));
        }
        mSpinner.setAdapter(dataAdapter);
        mSpinner.setSelection(previouslySelected);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String result = getOptionValueByIndex(questionnaireElement, position - 1);
                setResult(questionnaireElement, result);
                TextView mTextView = (TextView) parent.getChildAt(0);
                if (position != 0) {
                    setError(questionnaireElement, false);
                    onSelected(questionnaireElement, true, mTextView);
                    mayBeFireComplete(questionnaireElement);
                } else {
                    onSelected(questionnaireElement, false, mTextView);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "nothing is selected");
            }
        });
    }

    public void onSelected(JSONObject questionnaireElement, boolean selected, TextView mTextView) {
        boolean hasError = getError(questionnaireElement);
        ((LinearLayout) itemView.findViewById(R.id.dropdown_select_layout)).setBackground(
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
            ((LinearLayout) itemView.findViewById(R.id.dropdown_select_layout)).setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_dropdown_border_with_error));
            ((ImageView) itemView.findViewById(R.id.ninchat_dropdown_list_icon)).setColorFilter(
                    ContextCompat.getColor(itemView.getContext(), R.color.ninchat_color_error_background)
            );
            mTextView.setTextColor(ContextCompat.getColor(
                    itemView.getContext(), R.color.ninchat_color_error_background));
        }
    }

    private int preFill(JSONObject questionnaireElement) {
        String label = getLabel(questionnaireElement);
        mLabel.setText(label);
        int index = getOptionIndex(questionnaireElement, getResultString(questionnaireElement));
        return Math.max(0, index + 1);
    }

    private ArrayAdapter<String> getRTDataAdapter(JSONObject questionnaireElement) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.dropdown_item_text_view);
        JSONArray options = getOptions(questionnaireElement);
        dataAdapter.add(NinchatSessionManager.getInstance().getTranslation("Select"));
        for (int i = 0; i < options.length(); i += 1) {
            JSONObject curOption = options.optJSONObject(i);
            String label = curOption.optString("label");
            dataAdapter.add(NinchatSessionManager.getInstance().getTranslation(label));
        }
        return dataAdapter;
    }

    private void mayBeFireComplete(JSONObject questionnaireElement) {
        if (questionnaireElement != null && questionnaireElement.optBoolean("fireEvent", false)) {
            EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.other));
        }
    }
}
