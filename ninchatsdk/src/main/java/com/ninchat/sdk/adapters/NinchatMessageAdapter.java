package com.ninchat.sdk.adapters;

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
import java.util.Date;
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

        void bind(final Pair<NinchatMessage, Boolean> data, final boolean isContinuedMessage) {
            if (data.first.getType() == NinchatMessage.Type.START) {
                final TextView start = itemView.findViewById(R.id.ninchat_chat_message_start);
                start.setText(NinchatSessionManager.getInstance().getChatStarted());
                start.setVisibility(View.VISIBLE);
            } else if (data.first.getType() == NinchatMessage.Type.END) {
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
            } else if (data.second) {
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
                final TextView agent = itemView.findViewById(R.id.ninchat_chat_message_agent_name);
                agent.setText(data.first.getSender());
                final TextView agentTimestamp = itemView.findViewById(R.id.ninchat_chat_message_agent_timestamp);
                agentTimestamp.setText(TIMESTAMP_FORMATTER.format(data.first.getTimestamp()));
                final TextView agentMessage = itemView.findViewById(R.id.ninchat_chat_message_agent_message);
                final Spanned message = data.first.getMessage();
                final NinchatFile file = NinchatSessionManager.getInstance().getFile(data.first.getFileId());
                if (message != null) {
                    agentMessage.setText(message);
                } else if (file.isPDF()) {
                    agentMessage.setText(file.getUrl());
                } else {
                    agentMessage.setVisibility(View.GONE);
                    final ImageView agentImage = itemView.findViewById(R.id.ninchat_chat_message_agent_image);
                    Glide.with(agentImage.getContext())
                            .load(file.getUrl())
                            .into(agentImage);
                    agentImage.setVisibility(View.VISIBLE);
                    if (file.isVideo()) {
                        itemView.findViewById(R.id.ninchat_chat_message_agent_video_play_image).setVisibility(View.VISIBLE);
                    }
                    agentImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.getContext().startActivity(NinchatMediaActivity.getLaunchIntent(v.getContext(), data.first.getFileId()));
                        }
                    });
                }
                if (isContinuedMessage) {
                    itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(View.GONE);
                    // TODO: Override the background resource
                }
            } else {
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.VISIBLE);
                final TextView user = itemView.findViewById(R.id.ninchat_chat_message_user_name);
                user.setText(NinchatSessionManager.getInstance().getUserName());
                final TextView userTimestamp = itemView.findViewById(R.id.ninchat_chat_message_user_timestamp);
                userTimestamp.setText(TIMESTAMP_FORMATTER.format(data.first.getTimestamp()));
                final TextView userMessage = itemView.findViewById(R.id.ninchat_chat_message_user_message);
                final Spanned message = data.first.getMessage();
                final NinchatFile file = NinchatSessionManager.getInstance().getFile(data.first.getFileId());
                if (message != null) {
                    userMessage.setText(message);
                } else {
                    userMessage.setVisibility(View.GONE);
                    final ImageView userImage = itemView.findViewById(R.id.ninchat_chat_message_user_image);
                    Glide.with(userImage.getContext())
                            .load(file.getUrl())
                            .into(userImage);
                    if (file.isVideo()) {
                        itemView.findViewById(R.id.ninchat_chat_message_user_video_play_image).setVisibility(View.VISIBLE);
                    }
                    userImage.setVisibility(View.VISIBLE);
                    userImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.getContext().startActivity(NinchatMediaActivity.getLaunchIntent(v.getContext(), data.first.getFileId()));
                        }
                    });
                }
                if (isContinuedMessage) {
                    itemView.findViewById(R.id.ninchat_chat_message_user_title).setVisibility(View.GONE);
                    // TODO: Override the background resource
                }
            }
        }
    }

    protected static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("HH:MM", new Locale("fi-FI"));

    private List<Pair<NinchatMessage, Boolean>> data;

    protected WeakReference<NinchatChatActivity> activityWeakReference;
    protected WeakReference<RecyclerView> recyclerViewWeakReference;

    public NinchatMessageAdapter() {
        this.data = new ArrayList<>();
        this.data.add(new Pair<>(new NinchatMessage(NinchatMessage.Type.START), false));
        this.recyclerViewWeakReference = new WeakReference<>(null);
    }

    public void add(final String data, final String fileId, final String sender, long timestamp, final boolean isRemoteMessage) {
        this.data.add(new Pair<>(new NinchatMessage(data, fileId, sender, timestamp, isRemoteMessage), isRemoteMessage));
        final int position = this.data.size() - 1;
        notifyItemInserted(position);
        final RecyclerView recyclerView = recyclerViewWeakReference.get();
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(position);
        }
    }

    public void close(final NinchatChatActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
        this.data.add(new Pair<>(new NinchatMessage(NinchatMessage.Type.END), false));
        final int position = this.data.size() - 1;
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
        return data.size();
    }

    @NonNull
    @Override
    public NinchatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMessageViewHolder holder, int position) {
        final Pair<NinchatMessage, Boolean> message = data.get(position);
        boolean isContinuedMessage = false;
        if (position > 0) {
            final Pair<NinchatMessage, Boolean> previousMessage = data.get(position - 1);
            if (previousMessage.first != null && message.first != null) {
                isContinuedMessage = message.first.isRemoteMessage() == previousMessage.first.isRemoteMessage();
            }
        }
        holder.bind(data.get(position), isContinuedMessage);
    }
}
