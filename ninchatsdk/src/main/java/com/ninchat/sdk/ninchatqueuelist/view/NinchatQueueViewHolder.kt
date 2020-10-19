package com.ninchat.sdk.ninchatqueuelist.view

import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_queue.view.*


class NinchatCloseQueue(itemView: View, queueName: String) : RecyclerView.ViewHolder(itemView) {
    var currentQueue: Button = itemView.queue_name

    init {
        bind(queueName = queueName)
    }

    fun bind(queueName: String) {
        currentQueue.isEnabled = false
        currentQueue.alpha = 0.5f
        currentQueue.text = queueName
    }
}

class NinchatOpenQueue(
        itemView: View,
        queueId: String,
        queueName: String,
        callback: (queueId: String) -> Unit,
) : RecyclerView.ViewHolder(itemView) {
    var currentQueue: Button = itemView.queue_name

    init {
        bind(queueName = queueName,
                queueId = queueId,
                callback = callback)
    }

    fun bind(
            queueName: String,
            queueId: String,
            callback: (queueId: String) -> Unit,
    ) {
        currentQueue.alpha = 1f
        currentQueue.text = queueName
        currentQueue.setOnClickListener {
            callback(queueId)
        }
    }
}