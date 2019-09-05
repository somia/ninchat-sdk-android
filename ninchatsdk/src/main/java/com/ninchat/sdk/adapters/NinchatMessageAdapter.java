package com.ninchat.sdk.adapters;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatChatActivity;
import com.ninchat.sdk.activities.NinchatMediaActivity;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatUser;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;


/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatMessageAdapter extends RecyclerView.Adapter<NinchatMessageAdapter.NinchatMessageViewHolder> {

    public class NinchatMessageViewHolder extends RecyclerView.ViewHolder {

        public NinchatMessageViewHolder(View itemView) {
            super(itemView);
        }

        private void setAvatar(final ImageView avatar, final NinchatMessage ninchatMessage, final boolean hideAvatar) {
            String userAvatar = null;
            final NinchatUser user = NinchatSessionManager.getInstance().getMember(ninchatMessage.getSenderId());
            if (user != null) {
                userAvatar = user.getAvatar();
            }
            if (TextUtils.isEmpty(userAvatar)) {
                userAvatar = NinchatSessionManager.getInstance().getDefaultAvatar(ninchatMessage.isRemoteMessage());
            }
            if (!TextUtils.isEmpty(userAvatar)) {
                GlideApp.with(itemView.getContext())
                        .load(userAvatar)
                        .circleCrop()
                        .into(avatar);
            }
            final boolean showAvatars = NinchatSessionManager.getInstance().showAvatars(ninchatMessage.isRemoteMessage());
            if (!showAvatars) {
                avatar.setVisibility(View.GONE);
            } else if (hideAvatar) {
                avatar.setVisibility(showAvatars ? View.INVISIBLE : View.GONE);
                return;
            }
        }

        private void bindMessage(final @IdRes int wrapperId, final @IdRes int headerId, final @IdRes int senderId, final @IdRes int timestampId, final @IdRes int messageView, final @IdRes int imageId, final @IdRes int playIconId, final @IdRes int avatarId, final NinchatMessage ninchatMessage, final boolean isContinuedMessage, int firstMessageBackground, final @DrawableRes int repeatedMessageBackground) {
            itemView.findViewById(wrapperId).setVisibility(View.VISIBLE);
            itemView.findViewById(headerId).setVisibility(View.GONE);
            final TextView sender = itemView.findViewById(senderId);
            sender.setText(ninchatMessage.getSender());
            final TextView timestamp = itemView.findViewById(timestampId);
            timestamp.setText(TIMESTAMP_FORMATTER.format(ninchatMessage.getTimestamp()));
            setAvatar(itemView.findViewById(avatarId), ninchatMessage, isContinuedMessage);
            final TextView message = itemView.findViewById(messageView);
            message.setVisibility(View.GONE);
            final Spanned messageContent = ninchatMessage.getMessage();
            final NinchatFile file = NinchatSessionManager.getInstance().getFile(ninchatMessage.getFileId());
            final ImageView image = itemView.findViewById(imageId);
            image.setVisibility(View.GONE);
            final View playIcon = itemView.findViewById(playIconId);
            playIcon.setVisibility(View.GONE);
            GlideApp.with(image.getContext()).clear(image);
            image.setBackground(null);
            if (messageContent != null) {
                message.setVisibility(View.VISIBLE);
                message.setAutoLinkMask(Linkify.ALL);
                message.setText(messageContent);
            } else if (file.isPDF()) {
                message.setVisibility(View.VISIBLE);
                message.setText(file.getPDFLInk());
                message.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                final int width = file.getWidth();
                final int height = file.getHeight();
                final float density = itemView.getResources().getDisplayMetrics().density;
                image.getLayoutParams().width = (int) (width * density);
                image.getLayoutParams().height = (int) (height * density);
                image.setBackgroundResource(isContinuedMessage ? repeatedMessageBackground : firstMessageBackground);
                image.setVisibility(View.VISIBLE);
                GlideApp.with(image.getContext())
                        .load(file.getUrl())
                        .placeholder(R.color.ninchat_colorPrimaryDark)
                        .override(width, height)
                        .into(image);
                if (file.isVideo()) {
                    playIcon.setVisibility(View.VISIBLE);
                }
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.getContext().startActivity(NinchatMediaActivity.getLaunchIntent(v.getContext(), ninchatMessage.getFileId()));
                    }
                });
            }
            itemView.findViewById(messageView).setBackgroundResource(isContinuedMessage ? repeatedMessageBackground : firstMessageBackground);
            if (isContinuedMessage) {
                itemView.findViewById(wrapperId).setPadding(0, 0, 0, 0);
            } else {
                itemView.findViewById(headerId).setVisibility(View.VISIBLE);
            }
        }

        void bind(final NinchatMessage data, final boolean isContinuedMessage) {
            if (data.getType() == NinchatMessage.Type.PADDING) {
                itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.VISIBLE);
            } else if (data.getType() == NinchatMessage.Type.META) {
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
                final TextView start = itemView.findViewById(R.id.ninchat_chat_message_meta);
                start.setText(data.getMessage());
                start.setVisibility(View.VISIBLE);
            } else if (data.getType() == NinchatMessage.Type.END) {
                itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
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
            } else if (data.getType().equals(NinchatMessage.Type.WRITING)) {
                itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_image).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(isContinuedMessage ? View.GONE : View.VISIBLE);
                setAvatar(itemView.findViewById(R.id.ninchat_chat_message_agent_avatar), data, isContinuedMessage);
                itemView.findViewById(R.id.ninchat_chat_message_agent_message).setVisibility(View.GONE);
                final TextView agentName = itemView.findViewById(R.id.ninchat_chat_message_agent_name);
                agentName.setText(data.getSender());
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
            } else if (data.getType().equals(NinchatMessage.Type.MULTICHOICE)) {
                itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_image).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(isContinuedMessage ? View.GONE : View.VISIBLE);
                setAvatar(itemView.findViewById(R.id.ninchat_chat_message_agent_avatar), data, isContinuedMessage);
                final TextView agentName = itemView.findViewById(R.id.ninchat_chat_message_agent_name);
                agentName.setText(data.getSender());
                itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper)
                        .setBackgroundResource(isContinuedMessage ?
                                R.drawable.ninchat_chat_bubble_left_repeated :
                                R.drawable.ninchat_chat_bubble_left);
                final TextView message = itemView.findViewById(R.id.ninchat_chat_message_agent_message);
                message.setVisibility(View.VISIBLE);
                message.setText(data.getMessage());
                itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.VISIBLE);
                final RecyclerView options = itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice_options);
                options.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
                options.setAdapter(new NinchatMultiChoiceAdapter(data, this));
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
                itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper).getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                if (isContinuedMessage) {
                    itemView.findViewById(R.id.ninchat_chat_message_agent).setPadding(0, 0, 0, 0);
                }
            } else if (data.isRemoteMessage()) {
                itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_writing).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.GONE);
                bindMessage(R.id.ninchat_chat_message_agent,
                        R.id.ninchat_chat_message_agent_title,
                        R.id.ninchat_chat_message_agent_name,
                        R.id.ninchat_chat_message_agent_timestamp,
                        R.id.ninchat_chat_message_agent_message,
                        R.id.ninchat_chat_message_agent_image,
                        R.id.ninchat_chat_message_agent_video_play_image,
                        R.id.ninchat_chat_message_agent_avatar,
                        data, isContinuedMessage,
                        R.drawable.ninchat_chat_bubble_left,
                        R.drawable.ninchat_chat_bubble_left_repeated);
            } else {
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
                        R.drawable.ninchat_chat_bubble_right_repeated);
            }
            final RecyclerView recyclerView = recyclerViewWeakReference.get();
            if (recyclerView != null) {
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(true);
            }
        }

        public void optionToggled(final NinchatMessage message, final int position) {
            final RecyclerView recyclerView = recyclerViewWeakReference.get();
            if (recyclerView != null) {
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            }
            message.toggleOption(position);
            if (recyclerView == null || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                notifyItemChanged(getAdapterPosition());
            } else {
                scrollListener.setData(getAdapterPosition(), true, false);
            }
        }
    }

    protected class ScrollListener extends RecyclerView.OnScrollListener {

        private boolean updateData = false;
        private int index;
        private boolean updated;
        private boolean removed;

        protected void setData(final int index, final boolean updated, final boolean removed) {
            updateData = true;
            this.index = index;
            this.updated = updated;
            this.removed = removed;
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            if (recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE && updateData) {
                messagesUpdated(index, updated, removed);
                updateData = false;
            }
            super.onScrollStateChanged(recyclerView, newState);
        }
    }

    protected ScrollListener scrollListener = new ScrollListener();

    protected static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("HH:mm", new Locale("fi-FI"));

    protected WeakReference<NinchatChatActivity> activityWeakReference;
    protected WeakReference<RecyclerView> recyclerViewWeakReference;
    protected List<NinchatMessage> messages;

    public NinchatMessageAdapter() {
        this.messages = new ArrayList<>();
        this.messages.add(new NinchatMessage(NinchatMessage.Type.PADDING));
        this.recyclerViewWeakReference = new WeakReference<>(null);
    }

    public void addWriting(final String sender) {
        final int position = getItemCount() - 1;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messages.add(position, new NinchatMessage(NinchatMessage.Type.WRITING, sender));
                messagesUpdated(position, false, false);
            }
        });
    }

    public void add(final NinchatMessage message) {
        int index = getItemCount() - 1;
        boolean updated = false;
        if (!message.getType().equals(NinchatMessage.Type.WRITING) && index > 0) {
            final ListIterator<NinchatMessage> iterator = messages.listIterator(getItemCount() - 1);
            int i = 0;
            while (iterator.hasPrevious() && i < NinchatSessionManager.getInstance().getMemberCount()) {
                final NinchatMessage previousMessage = iterator.previous();
                if (previousMessage.getType().equals(NinchatMessage.Type.WRITING)) {
                    index--;
                    i++;
                    if (previousMessage.getSenderId().equals(message.getSenderId())) {
                        iterator.remove();
                        updated = true;
                    }
                }
            }
        }
        final int position = index;
        final boolean messageUpdated = updated;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messages.add(position, message);
                messagesUpdated(position, messageUpdated, false);
            }
        });
    }

    public void addMetaMessage(final String message) {
        final int position = getItemCount() - 1;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messages.add(position, new NinchatMessage(NinchatMessage.Type.META, message));
                messagesUpdated(position, false, false);
            }
        });
    }

    public void clear() {
        messages.clear();
    }

    public void removeWritingMessage(final String sender) {
        final ListIterator<NinchatMessage> iterator = messages.listIterator(getItemCount() - 1);
        while (iterator.hasPrevious()) {
            final NinchatMessage message = iterator.previous();
            if (message.getType().equals(NinchatMessage.Type.WRITING) && sender.equals(message.getSenderId())) {
                final int position = iterator.nextIndex();
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        messages.remove(message);
                        messagesUpdated(position, false, true);
                    }
                });
                return;
            }
        }
    }

    public void messagesUpdated(final int index, final boolean updated, final boolean removed) {
        final RecyclerView recyclerView = recyclerViewWeakReference.get();
        if (recyclerView == null || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
            if (index < 0) {
                notifyDataSetChanged();
            } else if (updated) {
                notifyItemChanged(index);
            } else if (removed) {
                notifyItemRemoved(index);
            } else {
                notifyItemInserted(index);
                if (index < getItemCount() - 2) {
                    notifyItemRangeChanged(index + 1, getItemCount() - index);
                }
            }
            final int position = getItemCount() - 1;
            if (recyclerView != null) {
                recyclerView.smoothScrollToPosition(position);
            }
        } else {
            scrollListener.setData(index, updated, removed);
        }
    }

    public void close(final NinchatChatActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
        final int index = getItemCount() - 1;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messages.add(index > 0 ? index : getItemCount(), new NinchatMessage(NinchatMessage.Type.END));
                final int position = getItemCount() - (index > 0 ? 2 : 1);
                final RecyclerView recyclerView = recyclerViewWeakReference.get();
                if (recyclerView == null || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    notifyItemInserted(position);
                    if (recyclerView != null) {
                        recyclerView.smoothScrollToPosition(position);
                    }
                } else {
                    scrollListener.setData(position, false, false);
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(scrollListener);
        this.recyclerViewWeakReference = new WeakReference<>(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(scrollListener);
        this.recyclerViewWeakReference = new WeakReference<>(null);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).getTimestamp().getTime();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public NinchatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMessageViewHolder holder, int position) {
        if (position >= getItemCount()) {
            return;
        }
        boolean isContinuedMessage = false;
        final NinchatMessage message = messages.get(position);
        try {
            final NinchatMessage previousMessage = messages.get(position - 1);
            isContinuedMessage = message.getSenderId().equals(previousMessage.getSenderId());
        } catch (final Exception e) {
            // Ignore
        }
        holder.bind(message, isContinuedMessage);
    }
}
