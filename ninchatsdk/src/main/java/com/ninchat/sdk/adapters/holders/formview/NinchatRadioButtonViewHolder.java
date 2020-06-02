package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ninchat.sdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatRadioButtonViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatRadioButtonViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final RadioGroup mRadioGroup;

    public NinchatRadioButtonViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mLabel = (TextView) itemView.findViewById(R.id.radio_button_label);
        mRadioGroup = (RadioGroup) itemView.findViewById(R.id.radio_btn_group);
        this.bind(item);
    }


    public void bind(JSONObject item) {
        mLabel.setText(item.optString("label", ""));
        final JSONArray options = item.optJSONArray("options");
        if (options == null) {
            return;
        }
        for (int i = 0; i < options.length(); i += 1) {
            final JSONObject curOption = options.optJSONObject(i);
            final String label = curOption.optString("label");
            final String value = curOption.optString("value");
            final RadioButton btn = new RadioButton(itemView.getContext());
            btn.setId(View.generateViewId());
            btn.setText(label);
            mRadioGroup.addView(btn);
            mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                Log.d(TAG, value);
            });
        }
    }
}
