package com.ninchat.sdk.adapters;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatChatActivity;
import com.ninchat.sdk.adapters.holders.NinchatMessageViewHolder;
import com.ninchat.sdk.helper.NinchatEndlessRecyclerViewScrollListener;
import com.ninchat.sdk.models.NinchatMessage;

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
public final class NinchatMessageAdapter extends RecyclerView.Adapter<NinchatMessageViewHolder> {

    protected NinchatEndlessRecyclerViewScrollListener scrollListener = new NinchatEndlessRecyclerViewScrollListener((index, updated, removed) -> messagesUpdated(index, updated, removed));
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
        return new NinchatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false),
                scrollListener,
                new NinchatMessageViewHolder.Callback() {
                    @Override
                    public void onClickListener() {
                        final NinchatChatActivity activity = activityWeakReference.get();
                        if (activity != null) {
                            activity.chatClosed();
                        }
                    }

                    @Override
                    public void onRequiredAnimationChange() {
                        final RecyclerView recyclerView = recyclerViewWeakReference.get();
                        if (recyclerView != null) {
                            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(true);
                        }
                    }

                    @Override
                    public void onOptionToggled(NinchatMessage message, int position) {
                        final RecyclerView recyclerView = recyclerViewWeakReference.get();
                        if (recyclerView != null) {
                            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                        }
                        message.toggleOption(position);
                        if (recyclerView == null || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                            notifyItemChanged(position);
                        } else {
                            scrollListener.setData(position, true, false);
                        }
                    }
                });
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
