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

import com.ninchat.sdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatLikeRtViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatLikeRtViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final Spinner mSpinner;
    private int selectedIndex;

    public NinchatLikeRtViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mLabel = (TextView) itemView.findViewById(R.id.dropdown_text_label);
        mSpinner = (Spinner) itemView.findViewById(R.id.ninchat_dropdown_list);
        this.bind(item);
    }


    public void bind(JSONObject item) {
        mLabel.setText(item.optString("label", ""));
        selectedIndex = 0;
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.dropdown_item_text_view);
        final JSONArray options = item.optJSONArray("options");
        if (options == null) {
            return;
        }
        dataAdapter.add("Select");
        for (int i = 0; i < options.length(); i += 1) {
            final JSONObject curOption = options.optJSONObject(i);
            final String label = curOption.optString("label");
            final String value = curOption.optString("value");
            dataAdapter.add(label);
        }
        mSpinner.setSelection(selectedIndex);
        mSpinner.setAdapter(dataAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedIndex = position;
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
}
