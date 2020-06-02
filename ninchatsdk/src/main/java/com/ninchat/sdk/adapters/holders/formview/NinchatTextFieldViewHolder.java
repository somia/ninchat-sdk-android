package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ninchat.sdk.R;

import org.json.JSONObject;

public class NinchatTextFieldViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatTextFieldViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final EditText mEditText;

    public NinchatTextFieldViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mLabel = (TextView) itemView.findViewById(R.id.simple_text_label);
        mEditText = (EditText) itemView.findViewById(R.id.simple_text_field);
        this.bind(item);
    }


    public void bind(JSONObject item) {
        mLabel.setText(item.optString("label", ""));
    }
}
