package com.ninchat.sdk.adapters;

import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatChatActivity;
import com.ninchat.sdk.activities.NinchatMediaActivity;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatMessage;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatMessageAdapter extends RecyclerView.Adapter<NinchatMessageAdapter.NinchatMessageViewHolder> {

    public class NinchatMessageViewHolder extends RecyclerView.ViewHolder {

        public NinchatMessageViewHolder(View itemView) {
            super(itemView);
        }

        private void bindMessage(final @IdRes int wrapperId, final @IdRes int headerId, final @IdRes int senderId, final @IdRes int timestampId, final @IdRes int messageView, final @IdRes int imageId, final @IdRes int playIconId, final NinchatMessage ninchatMessage, final boolean isContinuedMessage) {
            itemView.findViewById(wrapperId).setVisibility(View.VISIBLE);
            final TextView sender = itemView.findViewById(senderId);
            sender.setText(ninchatMessage.getSender());
            final TextView agentTimestamp = itemView.findViewById(timestampId);
            agentTimestamp.setText(TIMESTAMP_FORMATTER.format(ninchatMessage.getTimestamp()));
            final TextView message = itemView.findViewById(messageView);
            final Spanned messageContent = ninchatMessage.getMessage();
            final NinchatFile file = NinchatSessionManager.getInstance().getFile(ninchatMessage.getFileId());
            if (messageContent != null) {
                message.setText(messageContent);
            } else if (file.isPDF()) {
                message.setText(file.getUrl());
            } else {
                message.setVisibility(View.GONE);
                final ImageView image = itemView.findViewById(imageId);
                Glide.with(image.getContext())
                        .load(file.getUrl())
                        .into(image);
                image.setVisibility(View.VISIBLE);
                if (file.isVideo()) {
                    itemView.findViewById(playIconId).setVisibility(View.VISIBLE);
                }
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.getContext().startActivity(NinchatMediaActivity.getLaunchIntent(v.getContext(), ninchatMessage.getFileId()));
                    }
                });
            }
            if (isContinuedMessage) {
                itemView.findViewById(headerId).setVisibility(View.GONE);
                // TODO: Override the background resource
            }
        }

        void bind(final NinchatMessage data, final boolean isContinuedMessage) {
            if (data.getType() == NinchatMessage.Type.START) {
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                final TextView start = itemView.findViewById(R.id.ninchat_chat_message_start);
                start.setText(NinchatSessionManager.getInstance().getChatStarted());
                start.setVisibility(View.VISIBLE);
            } else if (data.getType() == NinchatMessage.Type.END) {
                itemView.findViewById(R.id.ninchat_chat_message_start).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                final TextView end = itemView.findViewById(R.id.ninchat_chat_message_end_text);
                end.setText(NinchatSessionManager.getInstance().getChatEnded());
                final Button closeButton = itemView.findViewById(R.id.ninchat_chat_message_close);
                closeButton.setText(NinchatSessionManager.getInstance().getCloseChat());
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final NinchatChatActivity activity = activityWeakReference.get();
                        if (activity != null) {
                            activity.chatClosed();
                        }
                    }
                });
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.VISIBLE);
            } else if (data.getType() == NinchatMessage.Type.WRITING) {
                itemView.findViewById(R.id.ninchat_chat_message_start).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_message).setVisibility(View.GONE);
                final ImageView image = itemView.findViewById(R.id.ninchat_chat_message_agent_image);
                image.setBackgroundResource(R.drawable.ninchat_writing_animation);
                final AnimationDrawable animationDrawable = (AnimationDrawable) image.getBackground();
                animationDrawable.start();
            } else if (data.isRemoteMessage()) {
                itemView.findViewById(R.id.ninchat_chat_message_start).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                bindMessage(R.id.ninchat_chat_message_agent,
                        R.id.ninchat_chat_message_agent_title,
                        R.id.ninchat_chat_message_agent_name,
                        R.id.ninchat_chat_message_agent_timestamp,
                        R.id.ninchat_chat_message_agent_message,
                        R.id.ninchat_chat_message_agent_image,
                        R.id.ninchat_chat_message_agent_video_play_image,
                        data, isContinuedMessage);
            } else {
                itemView.findViewById(R.id.ninchat_chat_message_start).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                bindMessage(R.id.ninchat_chat_message_user,
                        R.id.ninchat_chat_message_user_title,
                        R.id.ninchat_chat_message_user_name,
                        R.id.ninchat_chat_message_user_timestamp,
                        R.id.ninchat_chat_message_user_message,
                        R.id.ninchat_chat_message_user_image,
                        R.id.ninchat_chat_message_user_video_play_image,
                        data, isContinuedMessage);
            }
        }
    }

    protected static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("HH:MM", new Locale("fi-FI"));

    protected WeakReference<NinchatChatActivity> activityWeakReference;
    protected WeakReference<RecyclerView> recyclerViewWeakReference;

    public NinchatMessageAdapter() {
        NinchatSessionManager.getInstance().getMessages().add(new NinchatMessage(NinchatMessage.Type.START));
        this.recyclerViewWeakReference = new WeakReference<>(null);
    }

    public void newMessage(final int index) {
        if (index < 0) {
            notifyDataSetChanged();
        } else {
            notifyItemInserted(index);
        }
        final int position = NinchatSessionManager.getInstance().getMessages().size() - 1;
        final RecyclerView recyclerView = recyclerViewWeakReference.get();
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(position);
        }
    }

    public void updatedMessage(final int index) {
        if (index < 0) {
            notifyDataSetChanged();
        } else {
            notifyItemChanged(index);
        }
        final int position = NinchatSessionManager.getInstance().getMessages().size() - 1;
        final RecyclerView recyclerView = recyclerViewWeakReference.get();
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(position);
        }
    }

    public void close(final NinchatChatActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
        NinchatSessionManager.getInstance().getMessages().add(new NinchatMessage(NinchatMessage.Type.END));
        final int position = NinchatSessionManager.getInstance().getMessages().size() - 1;
        notifyItemInserted(position);
        final RecyclerView recyclerView = recyclerViewWeakReference.get();
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerViewWeakReference = new WeakReference<>(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerViewWeakReference = new WeakReference<>(null);
    }

    @Override
    public int getItemCount() {
        return NinchatSessionManager.getInstance().getMessages().size();
    }

    @NonNull
    @Override
    public NinchatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMessageViewHolder holder, int position) {
        final NinchatMessage message = NinchatSessionManager.getInstance().getMessages().get(position);
        boolean isContinuedMessage = false;
        if (position > 0 && position < NinchatSessionManager.getInstance().getMessages().size() - 1) {
            final NinchatMessage previousMessage = NinchatSessionManager.getInstance().getMessages().get(position - 1);
            if (previousMessage != null && message != null && previousMessage.getSender() != null) {
                isContinuedMessage = previousMessage.getSender().equals(message.getSender());
            }
        }
        holder.bind(message, isContinuedMessage);
    }
}
