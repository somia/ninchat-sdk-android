package com.ninchat.sdk.adapters.holders;

import android.support.annotation.NonNull;
import android.view.View;

import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatMessage;

public class NinchatPaddingViewHolder extends NinchatBaseViewHolder {
    public NinchatPaddingViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(final NinchatMessage data, final boolean isContinuedMessage) {
        itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.VISIBLE);
    }
}
