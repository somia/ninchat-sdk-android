package com.ninchat.sdk.adapters.holders;

import android.support.annotation.NonNull;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatAvatar;
import com.ninchat.sdk.models.NinchatMessage;

import org.jetbrains.annotations.NotNull;

public class NinchatGeneralViewHolder extends NinchatBaseViewHolder {

    public NinchatGeneralViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(@NotNull final NinchatMessage data,
                     final NinchatAvatar ninchatAvatar,
                     final boolean isContinuedMessage) {
        itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
        bindMessage(R.id.ninchat_chat_message_user,
                R.id.ninchat_chat_message_user_title,
                R.id.ninchat_chat_message_user_name,
                R.id.ninchat_chat_message_user_timestamp,
                R.id.ninchat_chat_message_user_message,
                R.id.ninchat_chat_message_user_image,
                R.id.ninchat_chat_message_user_video_play_image,
                R.id.ninchat_chat_message_user_avatar,
                data, isContinuedMessage,
                R.drawable.ninchat_chat_bubble_right,
                R.drawable.ninchat_chat_bubble_right_repeated,
                ninchatAvatar);
    }

}
