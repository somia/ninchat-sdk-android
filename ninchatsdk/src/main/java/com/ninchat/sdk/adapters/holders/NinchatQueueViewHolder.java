package com.ninchat.sdk.adapters.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatQueue;

public class NinchatQueueViewHolder extends RecyclerView.ViewHolder {
    public NinchatQueueViewHolder(final View itemView) {
        super(itemView);
    }

    public void bind(final NinchatQueue queue, final Callback callback) {
        final Button button = itemView.findViewById(R.id.queue_name);

        // If queue is closed, disable button and set alpha for look & feel
        if (queue.isClosed()) {
            button.setEnabled(false);
            button.setAlpha(0.5f);
            button.setText(NinchatSessionManager
                    .getInstance()
                    .getQueueName(queue.getName(), queue.isClosed()));
        } else {
            button.setAlpha(1f);
            button.setText(NinchatSessionManager
                    .getInstance()
                    .getQueueName(queue.getName()));
            button.setOnClickListener(v -> {
                callback.onClickListener(queue.getId());
            });
        }
    }


    public interface Callback {
        void onClickListener(String queueId);
    }
}
