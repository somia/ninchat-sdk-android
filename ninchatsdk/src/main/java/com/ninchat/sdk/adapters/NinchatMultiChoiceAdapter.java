package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.holders.NinchatMessageViewHolder;
import com.ninchat.sdk.adapters.holders.NinchatMultiChoiceViewholder;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatOption;

import java.lang.ref.WeakReference;
import java.util.List;

public final class NinchatMultiChoiceAdapter extends RecyclerView.Adapter<NinchatMultiChoiceViewholder> {

    private NinchatMessage message;
    private WeakReference<NinchatMessageViewHolder> viewHolderWeakReference;
    private boolean sendActionImmediately;

    public NinchatMultiChoiceAdapter(final NinchatMessage message, final NinchatMessageViewHolder viewHolder, final boolean sendActionImmediately) {
        this.message = message;
        this.viewHolderWeakReference = new WeakReference<>(viewHolder);
        this.sendActionImmediately = sendActionImmediately;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public NinchatMultiChoiceViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        final NinchatOption option = message.getOptions().get(position);
        return new NinchatMultiChoiceViewholder(LayoutInflater.from(viewGroup.getContext()).inflate(option.isSelected() ? R.layout.item_chat_multichoice_selected : R.layout.item_chat_multichoice_unselected, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMultiChoiceViewholder ninchatMultiChoiceViewholder, int position) {
        ninchatMultiChoiceViewholder.bind(message, sendActionImmediately, callback);
    }

    @Override
    public int getItemCount() {
        final List<NinchatOption> options = message.getOptions();
        return options != null ? options.size() : 0;
    }

    private final NinchatMultiChoiceViewholder.Callback callback = new NinchatMultiChoiceViewholder.Callback() {
        @Override
        public void onMultiChoiceOptionToggled(NinchatMessage message, final int choiceIndex) {
            // notifyDataSetChanged();
            final NinchatMessageViewHolder viewHolder = viewHolderWeakReference.get();
            if (viewHolder != null) {
                viewHolder.onMultiChoiceOptionToggled(message, choiceIndex);
            }
        }
    };
}
