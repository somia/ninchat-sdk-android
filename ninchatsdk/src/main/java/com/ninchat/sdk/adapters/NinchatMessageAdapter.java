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
import com.ninchat.sdk.models.NinchatMessageList;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;


/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatMessageAdapter extends RecyclerView.Adapter<NinchatMessageViewHolder> {

    protected NinchatEndlessRecyclerViewScrollListener scrollListener = new NinchatEndlessRecyclerViewScrollListener((index, updated, removed) -> messagesUpdated(index, updated, removed));
    protected WeakReference<NinchatChatActivity> activityWeakReference;
    protected WeakReference<RecyclerView> recyclerViewWeakReference;
    private NinchatMessageList ninchatMessageList;

    public NinchatMessageAdapter() {
        ninchatMessageList = new NinchatMessageList();
        this.recyclerViewWeakReference = new WeakReference<>(null);
    }

    protected static final String WRITING_MESSAGE_ID_PREFIX = "zzzzzwriting";

    public void addWriting(final String sender) {
        final String writingId = WRITING_MESSAGE_ID_PREFIX + sender;
        new Handler(Looper.getMainLooper()).post(() -> {
            final int index = ninchatMessageList.add(writingId, new NinchatMessage(NinchatMessage.Type.WRITING, sender, System.currentTimeMillis()));
            messagesUpdated(index, false, false);
        });
    }

    public void addMessage(final String messageId, final NinchatMessage message) {
        if (ninchatMessageList.contains(messageId)) {
            return;
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            final int oldNumberOfMessages = ninchatMessageList.size();
            ninchatMessageList.remove(WRITING_MESSAGE_ID_PREFIX, message.getSenderId());
            final int index = ninchatMessageList.add(messageId, message);
            messagesUpdated(index, ninchatMessageList.size() == oldNumberOfMessages, false);
        });
    }

    public String getLastMessageId(final boolean allowMeta) {
        return ninchatMessageList.getLastMessageId(allowMeta);
    }

    public void addMetaMessage(final String messageId, final String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            final int index = ninchatMessageList.add(messageId, new NinchatMessage(NinchatMessage.Type.META, message, System.currentTimeMillis()));
            messagesUpdated(index, false, false);
        });
    }

    public void clear() {
        ninchatMessageList.clear();
    }

    public void removeWritingMessage(final String sender) {
        final String writingId = WRITING_MESSAGE_ID_PREFIX + sender;
        new Handler(Looper.getMainLooper()).post(() -> {
            final int position = ninchatMessageList.getPosition(writingId);
            ninchatMessageList.remove(null, writingId);
            messagesUpdated(position, false, true);
        });
    }

    private void messagesUpdated(final int index, final boolean updated, final boolean removed) {
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
                if (nextIndex < ninchatMessageList.size()) {
                    notifyItemRangeChanged(nextIndex, ninchatMessageList.size() - nextIndex);
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
                recyclerView.smoothScrollToPosition(ninchatMessageList.size());
            } else {
                recyclerView.scrollToPosition(ninchatMessageList.size());
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
                final int position = ninchatMessageList.add(endMessageId, new NinchatMessage(NinchatMessage.Type.END, System.currentTimeMillis()));
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
        return ninchatMessageList.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return ninchatMessageList.size() + 1; // There is a synthetic item at the end.
    }

    @NonNull
    @Override
    public NinchatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false), callback);
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMessageViewHolder holder, int position) {
        if (position == ninchatMessageList.size()) { // The synthetic item.
            holder.bind(new NinchatMessage(NinchatMessage.Type.PADDING, System.currentTimeMillis()), false);
        } else if (position < ninchatMessageList.size()) {
            boolean isContinuedMessage = false;
            final NinchatMessage message = ninchatMessageList.getMessage(position);
            try {
                final NinchatMessage previousMessage = ninchatMessageList.getMessage(position - 1);
                isContinuedMessage = message.getSenderId().equals(previousMessage.getSenderId());
            } catch (final Exception e) {
                // Ignore
            }
            holder.bind(message, isContinuedMessage);
        }
    }

    private final NinchatMessageViewHolder.Callback callback = new NinchatMessageViewHolder.Callback() {
        @Override
        public void onChatClosed() {
            final NinchatChatActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.chatClosed();
            }
        }

        @Override
        public void onOptionsToggled(NinchatMessage message, final int choiceIndex, final int position){
            final RecyclerView recyclerView = recyclerViewWeakReference.get();
            if (recyclerView != null) {
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            }
            message.toggleOption(choiceIndex);
            if (recyclerView == null || recyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                notifyItemChanged(position);
            } else {
                scrollListener.setData(position, true, false);
            }
        }

        @Override
        public void onRequiredAnimationChange() {
            final RecyclerView recyclerView = recyclerViewWeakReference.get();
            if (recyclerView != null) {
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(true);
            }
        }
    };

}
