package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatQuestionnaire;

import org.json.JSONObject;

public class NinchatTextViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatTextViewHolder.class.getSimpleName();

    private final TextView mContent;

    public NinchatTextViewHolder(@NonNull View itemView, final JSONObject item) {
        super(itemView);
        mContent = (TextView) itemView.findViewById(R.id.text_view_content);
        this.bind(item);
    }


    public void bind(JSONObject item) {
        final String text = item.optString("label", "");
        mContent.setText(NinchatQuestionnaire.fromHTML(text));
    }
}
