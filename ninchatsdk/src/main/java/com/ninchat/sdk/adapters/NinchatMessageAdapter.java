package com.ninchat.sdk.adapters;

import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatChatActivity;
import com.ninchat.sdk.activities.NinchatMediaActivity;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatUser;
import com.ninchat.sdk.networkdispatchers.NinchatSendMessage;
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes;
import com.ninchat.sdk.utils.misc.Misc;
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;


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
                final NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
                userAvatar = ninchatMessage.isRemoteMessage() ?
                        sessionManager.ninchatState.getSiteConfig().getAgentAvatar() :
                        sessionManager.ninchatState.getSiteConfig().getUserAvatar();
            }
            if (!TextUtils.isEmpty(userAvatar)) {
                GlideApp.with(itemView.getContext())
                        .load(userAvatar)
                        .circleCrop()
                        .into(avatar);
            }
            final NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();
            final boolean showAvatars = ninchatMessage.isRemoteMessage() ?
                    sessionManager.ninchatState.getSiteConfig().showAgentAvatar() :
                    sessionManager.ninchatState.getSiteConfig().showUserAvatar();
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
            String senderNameOverride = NinchatSessionManager.getInstance().getName(senderId == R.id.ninchat_chat_message_agent_name);
            sender.setText(senderNameOverride != null ? senderNameOverride : ninchatMessage.getSender());

            final TextView timestamp = itemView.findViewById(timestampId);
            timestamp.setText(TIMESTAMP_FORMATTER.format(ninchatMessage.getTimestamp()));
            setAvatar(itemView.findViewById(avatarId), ninchatMessage, isContinuedMessage);
            final TextView message = itemView.findViewById(messageView);
            message.setVisibility(View.GONE);
            final Spanned messageContent = ninchatMessage.getMessage();
            final NinchatFile file = NinchatSessionManager.getInstance().ninchatState.getFile(ninchatMessage.getFileId());
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
            } else if (file.isDownloadableFile()) {
                message.setVisibility(View.VISIBLE);
                message.setText(file.getFileLink());
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
                end.setText(Misc.toRichText(
                        NinchatSessionManager.getInstance().ninchatState.getSiteConfig().getConversationEndedText(), end));
                final Button closeButton = itemView.findViewById(R.id.ninchat_chat_message_close);
                final String closeText =
                        NinchatSessionManager.getInstance().ninchatState.getSiteConfig().getChatCloseText();
                closeButton.setText(closeText);
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
            } else if (data.getType().equals(NinchatMessage.Type.MULTICHOICE)) {
                itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_image).setVisibility(View.GONE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(isContinuedMessage ? View.GONE : View.VISIBLE);
                itemView.findViewById(R.id.ninchat_chat_message_agent_writing).setVisibility(View.GONE);
                setAvatar(itemView.findViewById(R.id.ninchat_chat_message_agent_avatar), data, isContinuedMessage);

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
                options.setAdapter(new NinchatMultiChoiceAdapter(data, this, messageText == null));
                final Button sendButton = itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice_send);
                final String submitButtonText =
                        NinchatSessionManager.getInstance().ninchatState.getSiteConfig().getSubmitButtonText();
                sendButton.setText(submitButtonText);
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            final JSONObject payload = new JSONObject();
                            payload.put("action", "click");
                            payload.put("target", data.getMultiChoiceData());
                            NinchatSendMessage.executeAsync(
                                    NinchatScopeHandler.getIOScope(),
                                    NinchatSessionManager.getInstance().getSession(),
                                    NinchatSessionManager.getInstance().ninchatState.getChannelId(),
                                    NinchatMessageTypes.UI_ACTION,
                                    payload.toString(),
                                    aLong -> null
                            );
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
    protected List<String> messageIds;
    protected Map<String, NinchatMessage> messageMap;

    public NinchatMessageAdapter() {
        this.messageIds = new ArrayList<>();
        this.messageMap = new HashMap<>();
        this.recyclerViewWeakReference = new WeakReference<>(null);
    }

    protected static final String WRITING_MESSAGE_ID_PREFIX = "zzzzzwriting";

    public void addWriting(final String sender) {
        final String writingId = WRITING_MESSAGE_ID_PREFIX + sender;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messageIds.add(writingId);
                messageMap.put(writingId, new NinchatMessage(NinchatMessage.Type.WRITING, sender, System.currentTimeMillis()));
                Collections.sort(messageIds);
                messagesUpdated(messageIds.indexOf(writingId), false, false);
            }
        });
    }

    public void add(final String messageId, final NinchatMessage message) {
        if (messageIds.contains(messageId)) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final int oldNumberOfMessages = messageIds.size();
                messageIds.remove(WRITING_MESSAGE_ID_PREFIX + message.getSenderId());
                messageIds.add(messageId);
                Collections.sort(messageIds);
                messageMap.put(messageId, message);
                messagesUpdated(messageIds.indexOf(messageId), messageIds.size() == oldNumberOfMessages, false);
            }
        });
    }

    public String getLastMessageId(final boolean allowMeta) {
        if (messageIds.isEmpty()) {
            return ""; // Imaginary message id preceding all actual ids.
        }

        final ListIterator<String> iterator = messageIds.listIterator(messageIds.size() - 1);
        while (iterator.hasPrevious() && !allowMeta) {
            final String id = iterator.previous();
            final NinchatMessage message = messageMap.get(id);
            if (message != null &&
                    (message.getType() == NinchatMessage.Type.MESSAGE ||
                            message.getType() == NinchatMessage.Type.MULTICHOICE)) {
                return id;
            }
        }
        return messageIds.get(messageIds.size() - 1);
    }

    public void addMetaMessage(final String messageId, final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                messageIds.add(messageId);
                Collections.sort(messageIds);
                messageMap.put(messageId, new NinchatMessage(NinchatMessage.Type.META, message, System.currentTimeMillis()));
                messagesUpdated(messageIds.indexOf(messageId), false, false);
            }
        });
    }

    public void clear() {
        messageIds.clear();
    }

    public void removeWritingMessage(final String sender) {
        final String writingId = WRITING_MESSAGE_ID_PREFIX + sender;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final int position = messageIds.indexOf(writingId);
                messageIds.remove(writingId);
                Collections.sort(messageIds);
                messagesUpdated(position, false, true);
            }
        });
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
                //todo I am a workaround. Should fix when rewrite the message adapter.
                notifyDataSetChanged();
                notifyItemInserted(index);

                final int nextIndex = index + 1;
                if (nextIndex < messageIds.size()) {
                    notifyItemRangeChanged(nextIndex, messageIds.size() - nextIndex);
                }
            }
            scrollToBottom(true);
        } else {
            scrollListener.setData(index, updated, removed);
        }
    }

    public void scrollToBottom(boolean requireSmooth) {
        final RecyclerView recyclerView = recyclerViewWeakReference.get();
        if (recyclerView != null) {
            if (requireSmooth) {
                recyclerView.smoothScrollToPosition(messageIds.size());
            } else {
                recyclerView.scrollToPosition(messageIds.size());
            }
        }
    }

    public void close(final NinchatChatActivity activity) {
        activityWeakReference = new WeakReference<>(activity);

        // Disable text input after chat has ended
        EditText editText = activity.findViewById(R.id.message);
        editText.setEnabled(false);
        LinearLayout linearLayout = activity.findViewById(R.id.send_message_container);
        linearLayout.setOnClickListener(null);

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final String endMessageId = getLastMessageId(true) + "zzzzz";
                messageIds.add(endMessageId);
                Collections.sort(messageIds);
                messageMap.put(endMessageId, new NinchatMessage(NinchatMessage.Type.END, System.currentTimeMillis()));
                final int position = messageIds.indexOf(endMessageId);
                final RecyclerView recyclerView = recyclerViewWeakReference.get();
                if (recyclerView == null || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    notifyItemInserted(position);
                    scrollToBottom(true);
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
        return messageIds.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return messageIds.size() + 1; // There is a synthetic item at the end.
    }

    @NonNull
    @Override
    public NinchatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMessageViewHolder holder, int position) {
        if (position == messageIds.size()) { // The synthetic item.
            holder.bind(new NinchatMessage(NinchatMessage.Type.PADDING, System.currentTimeMillis()), false);
        } else if (position < messageIds.size()) {
            boolean isContinuedMessage = false;
            final NinchatMessage message = messageMap.get(messageIds.get(position));
            try {
                final NinchatMessage previousMessage = messageMap.get(messageIds.get(position - 1));
                isContinuedMessage = message.getSenderId().equals(previousMessage.getSenderId());
            } catch (final Exception e) {
                // Ignore
            }
            holder.bind(message, isContinuedMessage);
        }
    }
}