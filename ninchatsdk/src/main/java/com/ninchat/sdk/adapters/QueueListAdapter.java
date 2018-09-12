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

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatActivity;
import com.ninchat.sdk.activities.NinchatQueueActivity;
import com.ninchat.sdk.models.NinchatQueue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class QueueListAdapter extends RecyclerView.Adapter<QueueListAdapter.QueueViewHolder> {

    public final class QueueViewHolder extends RecyclerView.ViewHolder {
        public QueueViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(final NinchatQueue queue) {
            final Button button = itemView.findViewById(R.id.queue_name);
            button.setText(queue.getName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Activity activity = activityWeakReference.get();
                    if (activity != null) {
                        activity.startActivityForResult(NinchatActivity.getLaunchIntent(activity, queue.getId()),  NinchatSession.NINCHAT_SESSION_REQUEST_CODE);
                    }
                }
            });
        }
    }

    protected List<NinchatQueue> queues = new ArrayList<>();
    protected WeakReference<Activity> activityWeakReference = new WeakReference<>(null);

    public List<NinchatQueue> getQueues() {
        return queues;
    }

    public void setActivity(final Activity activity) {
        this.activityWeakReference = new WeakReference<>(activity);
    }

    @NonNull
    @Override
    public QueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new QueueViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull QueueViewHolder holder, int position) {
        holder.bind(queues.get(position));
    }

    @Override
    public int getItemCount() {
        return queues.size();
    }

    public void addQueue(final NinchatQueue queue) {
        queues.add(queue);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyItemInserted(queues.size() - 1);
            }
        });
    }
}
