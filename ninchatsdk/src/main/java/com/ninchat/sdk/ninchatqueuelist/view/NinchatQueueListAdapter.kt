package com.ninchat.sdk.ninchatqueuelist.view

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import com.ninchat.sdk.ninchatqueuelist.presenter.NinchatQueueListPresenter

class NinchatQueueListAdapter(
        val activity: Activity,
        queueList: List<NinchatQueue>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val ninchatQueueList = NinchatQueueListPresenter(queueList)

    override fun getItemViewType(position: Int): Int = position


    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RecyclerView.ViewHolder {
        val view = when {
            ninchatQueueList.isClosedQueue(position) -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_queue, parent, false)
                NinchatCloseQueue(
                        itemView = view,
                        queueName = ninchatQueueList.getQueueName(position))
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_queue, parent, false)
                NinchatOpenQueue(
                        itemView = view,
                        queueId = ninchatQueueList.getQueueId(position),
                        queueName = ninchatQueueList.getQueueName(position),
                        callback = onQueueSelected
                )
            }
        }
        return view
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder) {
            is NinchatCloseQueue -> viewHolder.bind(
                    queueName = ninchatQueueList.getQueueName(position))
            is NinchatOpenQueue -> viewHolder.bind(
                    queueName = ninchatQueueList.getQueueName(position),
                    queueId = ninchatQueueList.getQueueId(position),
                    callback = onQueueSelected
            )
        }
    }

    override fun getItemCount(): Int = ninchatQueueList.size()

    fun addQueue(queue: NinchatQueue) {
        ninchatQueueList.add(queue = queue) { queueSize ->
            Handler(Looper.getMainLooper()).post {
                notifyItemInserted(queueSize - 1)
            }
        }
    }

    val onQueueSelected = fun(queueId: String) {
        if (ninchatQueueList.requireOpenQuestionnaireActivity()) {
            ninchatQueueList.openQuestionnaireActivity(activity, queueId)
            return
        }
        ninchatQueueList.openQueueActivity(activity, queueId)
    }
}
