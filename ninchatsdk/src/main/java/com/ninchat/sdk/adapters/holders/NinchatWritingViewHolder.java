package com.ninchat.sdk.adapters.holders;

import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.adapters.NinchatMultiChoiceAdapter;
import com.ninchat.sdk.helper.NinchatAvatar;
import com.ninchat.sdk.models.NinchatMessage;

import org.json.JSONException;

public class NinchatWritingViewHolder extends NinchatBaseViewHolder {

    public NinchatWritingViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public void bind(final NinchatMessage data,
                     final NinchatAvatar ninchatAvatar,
                     final NinchatMessageViewHolder messageViewHolder,
                     final boolean isContinuedMessage) {
        itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_image).setVisibility(View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(isContinuedMessage ? View.GONE : View.VISIBLE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_writing).setVisibility(View.GONE);
        ninchatAvatar.setAvatar(itemView.getContext(), itemView.findViewById(R.id.ninchat_chat_message_agent_avatar), data, isContinuedMessage);

        String agentNameOverride = NinchatSessionManager.getInstance().getName(true);
        final TextView agentName = itemView.findViewById(R.id.ninchat_chat_message_agent_name);
        agentName.setText(agentNameOverride != null ? agentNameOverride : data.getSender());

        itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper)
                .setBackgroundResource(isContinuedMessage ?
                        R.drawable.ninchat_chat_bubble_left_repeated :
                        R.drawable.ninchat_chat_bubble_left);
        final TextView message = itemView.findViewById(R.id.ninchat_chat_message_agent_message);
        final Spanned messageText = data.getMessage();
        message.setText(messageText);
        message.setVisibility(messageText != null ? View.VISIBLE : View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.VISIBLE);
        final RecyclerView options = itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice_options);
        options.setLayoutManager(messageText != null ? new LinearLayoutManager(itemView.getContext()) : new GridLayoutManager(itemView.getContext(), 2));
        if (messageText == null) {
            options.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.top = 0;
                    outRect.bottom = 0;
                    if (parent.getChildLayoutPosition(view) % 2 == 0) {
                        outRect.left = 0;
                        outRect.right = 2;
                    } else {
                        outRect.left = 2;
                        outRect.right = 0;
                    }
                }
            });
        }
        options.setAdapter(new NinchatMultiChoiceAdapter(data, messageViewHolder, messageText == null));
        final Button sendButton = itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice_send);
        sendButton.setText(NinchatSessionManager.getInstance().getSubmitButtonText());
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    NinchatSessionManager.getInstance().sendUIAction(data.getMultiChoiceData());
                } catch (final JSONException e) {
                    Log.e(NinchatMessageAdapter.class.getSimpleName(), "Error when sending multichoice answer!", e);
                }
            }
        });
        sendButton.setVisibility(messageText != null ? View.VISIBLE : View.GONE);
        itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper).getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        if (isContinuedMessage) {
            itemView.findViewById(R.id.ninchat_chat_message_agent).setPadding(0, 0, 0, 0);
        }
    }
}
