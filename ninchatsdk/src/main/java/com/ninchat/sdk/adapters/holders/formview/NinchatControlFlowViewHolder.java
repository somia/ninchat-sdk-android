package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatQuestionnaire;

import org.json.JSONObject;

public class NinchatControlFlowViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatControlFlowViewHolder.class.getSimpleName();

    private final Button mPrevious;
    private final Button mNext;
    private Callback callback;
    public NinchatControlFlowViewHolder(@NonNull View itemView, final JSONObject item, final Callback callback) {
        super(itemView);
        mPrevious = (Button) itemView.findViewById(R.id.ninchat_button_previous);
        mNext = (Button) itemView.findViewById(R.id.ninchat_button_next);
        this.callback = callback;
        this.bind(item);
    }


    public void bind(JSONObject item) {
        mPrevious.setText("");
        mNext.setText("Continue to chat");
        mNext.setOnClickListener(v -> {
            callback.onClickNext();
        });
    }

    public interface Callback {
        void onClickNext();
    }
}
