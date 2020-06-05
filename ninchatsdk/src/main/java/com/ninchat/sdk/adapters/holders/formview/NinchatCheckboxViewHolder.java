package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.ninchat.sdk.R;
import org.json.JSONObject;

public class NinchatCheckboxViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatCheckboxViewHolder.class.getSimpleName();

    private final CheckBox mCheckbox;
    private boolean checked;

    public NinchatCheckboxViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mCheckbox = (CheckBox) itemView.findViewById(R.id.ninchat_checkbox);
        this.bind(item);
    }

    public void bind(JSONObject item) {
        mCheckbox.setText(item.optString("label", ""));
        mCheckbox.setOnCheckedChangeListener(onCheckedChangeListener);
    }

    public CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checked = isChecked;
            onCheckBoxStateChanged(isChecked);
        }
    };

    public void onCheckBoxStateChanged(final boolean isChecked) {
        mCheckbox.setTextColor(ContextCompat.getColor(itemView.getContext(),
                isChecked ? R.color.checkbox_text_selected : R.color.checkbox_text_not_selected));
    }
}
