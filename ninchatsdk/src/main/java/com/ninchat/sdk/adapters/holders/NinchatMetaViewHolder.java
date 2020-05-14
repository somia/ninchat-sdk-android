package com.ninchat.sdk.adapters.holders;

import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatMessage;

import org.jetbrains.annotations.NotNull;

public class NinchatMetaViewHolder extends NinchatBaseViewHolder {
    public NinchatMetaViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(@NotNull final NinchatMessage data, final boolean isContinuedMessage) {
        itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
        setVisible(data.getMessage());
    }

    protected void setVisible(final Spanned message) {
        final TextView start = itemView.findViewById(R.id.ninchat_chat_message_meta);
        start.setText(message);
        start.setVisibility(View.VISIBLE);
    }

}
