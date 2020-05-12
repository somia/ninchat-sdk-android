package com.ninchat.sdk.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatQueueActivity;
import com.ninchat.sdk.adapters.holders.NinchatQueueViewHolder;
import com.ninchat.sdk.models.NinchatQueue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public final class NinchatQueueListAdapter extends RecyclerView.Adapter<NinchatQueueViewHolder> {

    private List<NinchatQueue> queues = new ArrayList<>();
    private WeakReference<Activity> activityWeakReference;

    public NinchatQueueListAdapter(final Activity activity, final List<NinchatQueue> queues) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.queues.addAll(queues);
    }

    @NonNull
    @Override
    public NinchatQueueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NinchatQueueViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_queue, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NinchatQueueViewHolder holder, int position) {
        holder.bind(queues.get(position), callback);
    }

    @Override
    public int getItemCount() {
        return queues == null ? 0 : queues.size();
    }

    public void clearData() {
        queues.clear();
        notifyDataSetChanged();
    }

    public void addData(final NinchatQueue queue) {
        final int previousSize = queues.size();
        queues.add(queue);
        notifyItemRangeInserted(previousSize, queues.size());
    }

    private final NinchatQueueViewHolder.Callback callback = queueId -> {
        final Activity activity = activityWeakReference.get();
        if (activity != null) {
            activity.startActivityForResult(
                    NinchatQueueActivity.getLaunchIntent(activity, queueId),
                    NinchatQueueActivity.REQUEST_CODE);
        }
    };
}
