package com.ninchat.sdk.adapters;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatQuestionnaireActivity;
import com.ninchat.sdk.ninchatqueue.model.NinchatQueueModel;
import com.ninchat.sdk.ninchatqueue.presenter.NinchatQueuePresenter;
import com.ninchat.sdk.ninchatqueue.view.NinchatQueueActivity;
import com.ninchat.sdk.models.NinchatQueue;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil.PRE_AUDIENCE_QUESTIONNAIRE;

public final class NinchatQueueListAdapter extends RecyclerView.Adapter<NinchatQueueListAdapter.NinchatQueueViewHolder> {

    final class NinchatQueueViewHolder extends RecyclerView.ViewHolder {

        NinchatQueueViewHolder(final View itemView) {
            super(itemView);
        }

        void bind(final NinchatQueue queue) {
            final Button button = itemView.findViewById(R.id.queue_name);
            // If queue is closed, disable button and set alpha for look & feel
            if (queue.isClosed()) {
                button.setEnabled(false);
                button.setAlpha(0.5f);
                button.setText(NinchatSessionManager.getInstance().ninchatState.getSiteConfig().getQueueName(
                        queue.getName(), queue.isClosed()
                ));
            } else {
                button.setAlpha(1f);
                button.setText(NinchatSessionManager.getInstance().ninchatState.getSiteConfig().getQueueName(
                        queue.getName(), false
                ));
                button.setOnClickListener(v -> {
                    final Activity activity = activityWeakReference.get();
                    if (activity == null) {
                        return;
                    }
                    final NinchatSessionManager ninchatSessionManager = NinchatSessionManager.getInstance();
                    if (ninchatSessionManager.ninchatState.getNinchatQuestionnaire() != null &&
                            ninchatSessionManager.ninchatState.getNinchatQuestionnaire().hasPreAudienceQuestionnaire() &&
                            !ninchatSessionManager.ninchatSessionHolder.isResumedSession()) {
                        activity.startActivityForResult(
                                NinchatQuestionnaireActivity.getLaunchIntent(activity, queue.getId(), PRE_AUDIENCE_QUESTIONNAIRE),
                                NinchatQuestionnaireActivity.REQUEST_CODE);
                        return;
                    }
                    // after click a particular queue we should check if there are pre-audience questionnaires
                    activity.startActivityForResult(
                            NinchatQueuePresenter.getLaunchIntentWithQueueId(activity, queue.getId()),
                            NinchatQueueModel.REQUEST_CODE);
                });
            }
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

    public void clear() {
        this.queues.clear();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
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