package com.ninchat.sdk.adapters;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatQueueActivity;
import com.ninchat.sdk.models.NinchatQueue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class NinchatQueueListAdapter extends RecyclerView.Adapter<NinchatQueueListAdapter.NinchatQueueViewHolder> {

    final class NinchatQueueViewHolder extends RecyclerView.ViewHolder {

        NinchatQueueViewHolder(final View itemView) {
            super(itemView);
        }

        void bind(final NinchatQueue queue) {
            final Button button = itemView.findViewById(R.id.queue_name);
            button.setText(NinchatSessionManager.getInstance().getQueueName(queue.getName()));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Activity activity = activityWeakReference.get();
                    if (activity != null) {
                        activity.startActivityForResult(NinchatQueueActivity.getLaunchIntent(activity, queue.getId()),  NinchatQueueActivity.REQUEST_CODE);
                    }
                }
            });
        }
    }

    protected List<NinchatQueue> queues = new ArrayList<>();
    protected WeakReference<Activity> activityWeakReference;

    public NinchatQueueListAdapter(final Activity activity, final List<NinchatQueue> queues) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.queues.addAll(queues);
    }

    @NonNull
    @Override
    public NinchatQueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatQueueViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatQueueViewHolder holder, int position) {
        holder.bind(queues.get(position));
    }

    @Override
    public int getItemCount() {
        return queues.size();
    }

    public void addQueue(final NinchatQueue queue) {
        this.queues.add(queue);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(queues.size() - 1);
            }
        });
    }
}
