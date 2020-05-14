package com.ninchat.sdk.adapters.holders;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatMessage;

public class NinchatRemoteMessageViewHolder extends NinchatBaseViewHolder {
    private final NinchatMessageViewHolder.Callback callback;

    public NinchatRemoteMessageViewHolder(@NonNull View itemView, NinchatMessageViewHolder.Callback callback) {
        super(itemView);
        this.callback = callback;
    }

    public void bind(final NinchatMessage data, final boolean isContinuedMessage) {
        itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_writing).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.GONE);

        /*bindMessage(R.id.ninchat_chat_message_agent,
                R.id.ninchat_chat_message_agent_title,
                R.id.ninchat_chat_message_agent_name,
                R.id.ninchat_chat_message_agent_timestamp,
                R.id.ninchat_chat_message_agent_message,
                R.id.ninchat_chat_message_agent_image,
                R.id.ninchat_chat_message_agent_video_play_image,
                R.id.ninchat_chat_message_agent_avatar,
                data, isContinuedMessage,
                R.drawable.ninchat_chat_bubble_left,
                R.drawable.ninchat_chat_bubble_left_repeated);*/

    }
}
