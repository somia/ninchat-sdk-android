package com.ninchat.sdk.adapters.holders.formview;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
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


    @SuppressLint("NewApi")
    public void bind(JSONObject item) {
        final String text = item.optString("label", "");
        mContent.setAutoLinkMask(0);
        mContent.setText(NinchatQuestionnaire.fromHTML(text));
        mContent.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
