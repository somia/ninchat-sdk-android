package com.ninchat.sdk.adapters.holders;

import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatAvatar;
import com.ninchat.sdk.models.NinchatMessage;

public class NinchatMultipleChoiceViewHolder extends NinchatBaseViewHolder {
    private final NinchatMessageViewHolder.Callback callback;
    private final NinchatAvatar ninchatAvatar;

    public NinchatMultipleChoiceViewHolder(@NonNull View itemView,
                                           NinchatAvatar ninchatAvatar,
                                           NinchatMessageViewHolder.Callback callback) {
        super(itemView);
        this.callback = callback;
        this.ninchatAvatar = ninchatAvatar;
    }

    public void bind(final NinchatMessage data, final boolean isContinuedMessage) {
        itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_image).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(isContinuedMessage ? View.GONE : View.VISIBLE);
        ninchatAvatar.setAvatar(itemView.getContext(), itemView.findViewById(R.id.ninchat_chat_message_agent_avatar), data, isContinuedMessage);
        itemView.findViewById(R.id.ninchat_chat_message_agent_message).setVisibility(View.GONE);

        String agentNameOverride = NinchatSessionManager.getInstance().getName(true);
        final TextView agentName = itemView.findViewById(R.id.ninchat_chat_message_agent_name);
        agentName.setText(agentNameOverride != null ? agentNameOverride : data.getSender());

        itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper)
                .setBackgroundResource(isContinuedMessage ?
                        R.drawable.ninchat_chat_bubble_left_repeated :
                        R.drawable.ninchat_chat_bubble_left);
        final ImageView image = itemView.findViewById(R.id.ninchat_chat_message_agent_writing);
        image.setVisibility(View.VISIBLE);
        GlideApp.with(image.getContext()).clear(image);
        image.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator);
        final AnimationDrawable animationDrawable = (AnimationDrawable) image.getBackground();
        animationDrawable.start();
        if (isContinuedMessage) {
            itemView.findViewById(R.id.ninchat_chat_message_agent).setPadding(0, 0, 0, 0);
        }
    }
}
