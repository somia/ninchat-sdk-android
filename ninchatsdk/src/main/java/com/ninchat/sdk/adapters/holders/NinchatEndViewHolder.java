package com.ninchat.sdk.adapters.holders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatMessage;

public class NinchatEndViewHolder extends NinchatBaseViewHolder {
    public NinchatEndViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(final NinchatMessageViewHolder.Callback callback) {
        itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
        //todo fix ordering beautify
        setEndText();
        setButtonListener(callback);
        itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.VISIBLE);

    }

    protected void setEndText() {
        final TextView end = itemView.findViewById(R.id.ninchat_chat_message_end_text);
        end.setText(NinchatSessionManager.getInstance().getChatEnded());
    }

    protected void setButtonListener(final NinchatMessageViewHolder.Callback callback) {
        final Button closeButton = itemView.findViewById(R.id.ninchat_chat_message_close);
        closeButton.setText(NinchatSessionManager.getInstance().getCloseChat());
        closeButton.setOnClickListener(v -> callback.onClickListener());
    }
}
