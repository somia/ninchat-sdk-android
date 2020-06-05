package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ninchat.sdk.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class NinchatCheckboxViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatCheckboxViewHolder.class.getSimpleName();

    private final CheckBox mCheckbox;

    public NinchatCheckboxViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mCheckbox = (CheckBox) itemView.findViewById(R.id.ninchat_checkbox);
        this.bind(item);
    }

    public void bind(JSONObject item) {
        mCheckbox.setText(item.optString("label", ""));
    }
}
