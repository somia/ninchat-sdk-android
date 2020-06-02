package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.NinchatMessageViewHolder;

import org.json.JSONObject;

public class NinchatTextAreaViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatMessageViewHolder.class.getSimpleName();

    private final TextView mLabel;
    private final EditText mEditText;

    public NinchatTextAreaViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mLabel = (TextView) itemView.findViewById(R.id.multiline_text_label);
        mEditText = (EditText) itemView.findViewById(R.id.multiline_text_area);
        this.bind(item);
    }


    public void bind(JSONObject item) {
        mLabel.setText(item.optString("label", ""));
    }
}
