package com.ninchat.sdk.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatOption;

import java.lang.ref.WeakReference;
import java.util.List;

public final class NinchatMultiChoiceAdapter extends RecyclerView.Adapter<NinchatMultiChoiceAdapter.NinchatMultiChoiceViewholder> {

    public final class NinchatMultiChoiceViewholder extends RecyclerView.ViewHolder {
        public NinchatMultiChoiceViewholder(@NonNull View itemView, final NinchatMessage message) {
            super(itemView);
        }

        public void bind(final NinchatMessage message, final int position, final boolean sendAction) {
            final TextView button = (TextView) itemView;
            final List<NinchatOption> options = message.getOptions();
            final NinchatOption option = options.get(position);
            button.setText(option.getLabel());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sendAction) {
                        try {
                            option.toggle();
                            NinchatSessionManager.getInstance().sendUIAction(option.toJSON());
                            option.toggle();
                        } catch (final Exception e) {
                            // Ignore
                        }
                    } else {
                        final NinchatMessageAdapter.NinchatMessageViewHolder viewHolder = viewHolderWeakReference.get();
                        if (viewHolder != null) {
                            viewHolder.optionToggled(message, position);
                        }
                    }
                }
            });
        }
    }

    private NinchatMessage message;
    private WeakReference<NinchatMessageAdapter.NinchatMessageViewHolder> viewHolderWeakReference;
    private boolean sendActionImmediately;

    public NinchatMultiChoiceAdapter(final NinchatMessage message, final NinchatMessageAdapter.NinchatMessageViewHolder viewHolder, final boolean sendActionImmediately) {
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
        return new NinchatMultiChoiceViewholder(LayoutInflater.from(viewGroup.getContext()).inflate(option.isSelected() ? R.layout.item_chat_multichoice_selected : R.layout.item_chat_multichoice_unselected, viewGroup, false), message);
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatMultiChoiceViewholder ninchatMultiChoiceViewholder, int position) {
        ninchatMultiChoiceViewholder.bind(message, position, sendActionImmediately);
    }

    @Override
    public int getItemCount() {
        final List<NinchatOption> options = message.getOptions();
        return options != null ? options.size() : 0;
    }
}
