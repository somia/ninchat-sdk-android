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

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public final class NinchatMessageAdapter extends RecyclerView.Adapter<NinchatMessageViewHolder> {
    private final String TAG = NinchatMessageAdapter.class.getSimpleName();
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
        final int index = ninchatMessageList.add(writingId, new NinchatMessage(NinchatMessage.Type.WRITING, sender, System.currentTimeMillis()));
        messagesUpdated(index, MessageType.INSERT);
    }

    public void addMessage(final String messageId, final NinchatMessage message) {
        if (ninchatMessageList.contains(messageId)) {
            return;
        }
        final int index = ninchatMessageList.add(messageId, message);
        messagesUpdated(index, MessageType.INSERT);
    }

    public String getLastMessageId(final boolean allowMeta) {
        return ninchatMessageList.getLastMessageId(allowMeta);
    }

    public void addMetaMessage(final String messageId, final String message) {
        final int index = ninchatMessageList.add(messageId, new NinchatMessage(NinchatMessage.Type.META, message, System.currentTimeMillis()));
        messagesUpdated(index, MessageType.NOCHANGE);
    }

    public void clear() {
        ninchatMessageList.clear();
    }

    public void removeWritingMessage(final String sender) {
        final int position = ninchatMessageList.getPosition(sender);
        ninchatMessageList.remove(WRITING_MESSAGE_ID_PREFIX, sender);
        messagesUpdated(position, MessageType.DELETE);
    }

    private void messagesUpdated(final int index, final MessageType messageType) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (index < 0) {
                notifyDataSetChanged();
            } else if (messageType == MessageType.UPDATE) {
                notifyItemChanged(index);
            } else if (messageType == MessageType.INSERT) {
                if (index < getItemCount()) {
                    // todo (pallab) fix this hack
                    notifyItemChanged(index);
                } else {
                    notifyItemInserted(index);
                }
            } else if (messageType == MessageType.DELETE) {
                notifyItemRemoved(index);
            }
            scrollToBottom(true);
        });
    }

    public void scrollToBottom(boolean requireSmooth) {
        final RecyclerView recyclerView = recyclerViewWeakReference.get();
        if (recyclerView != null) {
            if (requireSmooth) {
                recyclerView.smoothScrollToPosition(getItemCount());
            } else {
                recyclerView.scrollToPosition(getItemCount());
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
                notifyItemInserted(position);
                scrollToBottom(true);
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
        return ninchatMessageList == null ? 0 : ninchatMessageList.size();
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
        public void onOptionsToggled(NinchatMessage message, final int choiceIndex, final int position) {
            final RecyclerView recyclerView = recyclerViewWeakReference.get();
            if (recyclerView != null) {
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
            }
            message.toggleOption(choiceIndex);
            notifyItemChanged(position);
        }

        @Override
        public void onRequiredAnimationChange() {
            final RecyclerView recyclerView = recyclerViewWeakReference.get();
            if (recyclerView != null) {
                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(true);
            }
        }
    };


    NinchatEndlessRecyclerViewScrollListener scrollListener = new NinchatEndlessRecyclerViewScrollListener() {
        @Override
        public void onUpdateView(RecyclerView recyclerView, int newState) {
            // todo implements me later
        }
    };

    public enum MessageType {
        UPDATE,
        DELETE,
        INSERT,
        NOCHANGE
    }
}
