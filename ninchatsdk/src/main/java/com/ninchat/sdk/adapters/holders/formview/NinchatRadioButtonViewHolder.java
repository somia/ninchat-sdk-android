package com.ninchat.sdk.adapters.holders.formview;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
    private final float density = itemView.getResources().getDisplayMetrics().density;
    private final int marginStartInDp = 8;
    private final int marginBottomInDp = 5;
    private final int paddingStartInDp = 10;

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
        int marginStart = (int) (marginStartInDp * density);
        int marginBottom = (int) (marginBottomInDp * density);
        int paddingStart = (int) (paddingStartInDp * density);

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(marginStart, 0, 0, marginBottom);
        for (int i = 0; i < options.length(); i += 1) {
            final JSONObject curOption = options.optJSONObject(i);
            final String label = curOption.optString("label");
            final String value = curOption.optString("value");
            final RadioButton btn = new RadioButton(itemView.getContext());
            btn.setId(View.generateViewId());
            btn.setText(label);
            btn.setLayoutParams(params);
            btn.setPadding(paddingStart, 0, 0, 0);
            mRadioGroup.addView(btn);
            mRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                Log.d(TAG, value);
            });
        }
    }
}
