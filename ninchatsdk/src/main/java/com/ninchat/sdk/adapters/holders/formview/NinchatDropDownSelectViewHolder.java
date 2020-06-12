package com.ninchat.sdk.adapters.holders.formview;

import android.provider.ContactsContract;
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

import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class NinchatDropDownSelectViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatDropDownSelectViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final Spinner mSpinner;
    private int itemPosition;
    WeakReference<NinchatPreAudienceQuestionnaire> preAudienceQuestionnaire;


    public NinchatDropDownSelectViewHolder(@NonNull View itemView, final int position,
                                           final NinchatPreAudienceQuestionnaire ninchatPreAudienceQuestionnaire) {
        super(itemView);
        mLabel = (TextView) itemView.findViewById(R.id.dropdown_text_label);
        mSpinner = (Spinner) itemView.findViewById(R.id.ninchat_dropdown_list);
        itemPosition = position;
        preAudienceQuestionnaire = new WeakReference<>(ninchatPreAudienceQuestionnaire);
        bind();
    }


    public void bind() {
        final int previouslySelected = preFill();
        final ArrayAdapter<String> dataAdapter = getRTDataAdapter();
        mSpinner.setAdapter(dataAdapter);
        mSpinner.setSelection(previouslySelected);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final JSONObject rootItem = preAudienceQuestionnaire.get().getItem(itemPosition);
                preAudienceQuestionnaire.get().setResult(rootItem, position);
                final TextView mTextView = (TextView) parent.getChildAt(0);
                if (position != 0) {
                    onSelected(true, mTextView);
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
        ((RelativeLayout) itemView.findViewById(R.id.dropdown_select_layout)).setBackground(
                ContextCompat.getDrawable(itemView.getContext(),
                        selected ? R.drawable.ninchat_dropdown_select : R.drawable.ninchat_dropdown_not_selected));

        ((ImageView) itemView.findViewById(R.id.ninchat_dropdown_list_icon)).setColorFilter(
                ContextCompat.getColor(itemView.getContext(),
                        selected ? R.color.checkbox_selected : R.color.checkbox_not_selected)
        );

        mTextView.setTextColor(ContextCompat.getColor(
                itemView.getContext(),
                selected ?
                        R.color.checkbox_selected :
                        R.color.ninchat_color_ui_compose_select_unselected_text));

    }

    private int preFill() {
        final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
        final String label = preAudienceQuestionnaire.get().getLabel(item);
        final int result = preAudienceQuestionnaire.get().getResultInt(item);
        mLabel.setText(label);
        return Math.max(result, 0);
    }

    private ArrayAdapter<String> getRTDataAdapter() {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.dropdown_item_text_view);
        final JSONObject item = preAudienceQuestionnaire.get().getItem(itemPosition);
        final JSONArray options = preAudienceQuestionnaire.get().getOptions(item);
        dataAdapter.add("Select");
        for (int i = 0; i < options.length(); i += 1) {
            final JSONObject curOption = options.optJSONObject(i);
            final String label = curOption.optString("label");
            dataAdapter.add(label);
        }
        return dataAdapter;
    }
}
