package com.ninchat.sdk.ninchatchatmessage

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.models.NinchatMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

const val WRITING_MESSAGE_ID_PREFIX = "zzzzzwriting"

class NinchatMessageList(private val mAdapter: INinchatMessageList) {
    private var messageIds: List<String> = emptyList()
    private var messageMap: MutableMap<String, NinchatMessage> = mutableMapOf()
    private val pendingMessageList: ArrayDeque<NinchatPendingMessage> = ArrayDeque()

    private fun handleIncomingMessage() {
        if (pendingMessageList.isEmpty()) return
        // get the first element
        val pendingMessage = pendingMessageList.first()
        when (pendingMessage.messageType) {
            NinchatMessage.Type.WRITING -> {
                if (messageIds.contains(pendingMessage.sender)) {
                    pendingMessageList.removeFirst()
                    handleIncomingMessage()
                    return
                }
                val newList = messageIds.plus(pendingMessage.sender)
                messageMap[pendingMessage.sender] = pendingMessage.message!!
                diffUtilAsync(newList = newList)
            }
            NinchatMessage.Type.REMOVE_WRITING -> {
                if (!messageIds.contains(pendingMessage.sender)) {
                    pendingMessageList.removeFirst()
                    handleIncomingMessage()
                    return
                }
                // remove the message from `messageIds` and  `messageMap`
                val newList = messageIds.filter { it != pendingMessage.sender }
                messageMap.remove(pendingMessage.sender)
                diffUtilAsync(newList = newList)
            }
            NinchatMessage.Type.MESSAGE -> {
                if (messageIds.contains(pendingMessage.sender)) {
                    pendingMessageList.removeFirst()
                    handleIncomingMessage()
                    return
                }
                val writingId = "$WRITING_MESSAGE_ID_PREFIX ${pendingMessage.message!!.senderId}"
                val newList = messageIds
                        // remove writing if there is any
                        .filter { it != writingId }
                        // add message id
                        .plus(pendingMessage.sender)
                messageMap.apply {
                    // remote writing id
                    remove(writingId)
                    // add message id
                    put(pendingMessage.sender, pendingMessage.message)
                }
                diffUtilAsync(newList = newList)
            }
            NinchatMessage.Type.META -> {
                val newList = messageIds.plus(pendingMessage.sender)
                messageMap[pendingMessage.sender] = pendingMessage.message!!
                diffUtilAsync(newList = newList)
            }
            NinchatMessage.Type.END -> {
                // get end message id from here since it require calling messageIds
                val endMessageId = getLastMessageId(true) + "zzzzz"
                val newList = messageIds.plus(endMessageId)
                messageMap[endMessageId] = pendingMessage.message!!
                diffUtilAsync(newList = newList)
            }
            else -> {
                Log.d("NinchatMessageList", "Unknown message ${pendingMessage.messageType}")
            }
        }
    }

    private fun applyDiffUtil(newList: List<String> = emptyList(), diffResult: DiffUtil.DiffResult) {
        pendingMessageList.removeFirst()
        // assign to new list
        messageIds = newList
        // Call diff callback
        mAdapter.callback(diffResult = diffResult, position = size())
        // handle from pending message
        handleIncomingMessage()
    }

    private fun diffUtilAsync(newList: List<String> = emptyList()) {
        Single
                .create<Pair<List<String>, DiffUtil.DiffResult>> { emitter ->
                    val sortedNewList = newList.sorted()
                    val diffResult = DiffUtil.calculateDiff(NinchatMessageDiffUtil(oldList = messageIds, newList = sortedNewList))
                    emitter.onSuccess(Pair(sortedNewList, diffResult))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    // send callback
                    applyDiffUtil(newList = result.first, diffResult = result.second)
                }, {
                    Log.e("diffUtlAsync", it.message ?: "Error in diffUtil Async task")
                })
    }

    fun addWriting(sender: String) {
        pendingMessageList.addLast(NinchatPendingMessage(
                messageType = NinchatMessage.Type.WRITING,
                sender = "$WRITING_MESSAGE_ID_PREFIX $sender",
                message = NinchatMessage(NinchatMessage.Type.WRITING, sender, System.currentTimeMillis())))
        if (pendingMessageList.size > 1) return
        handleIncomingMessage()
    }

    fun removeWriting(sender: String) {
        pendingMessageList.addLast(NinchatPendingMessage(
                messageType = NinchatMessage.Type.REMOVE_WRITING,
                sender = "$WRITING_MESSAGE_ID_PREFIX $sender",
        ))
        if (pendingMessageList.size > 1) return
        handleIncomingMessage()
    }

    fun add(messageId: String, message: NinchatMessage) {
        pendingMessageList.addLast(NinchatPendingMessage(
                messageType = NinchatMessage.Type.MESSAGE,
                sender = messageId,
                message = message))
        if (pendingMessageList.size > 1) return
        handleIncomingMessage()
    }

    fun addMetaMessage(messageId: String, message: String) {
        pendingMessageList.addLast(NinchatPendingMessage(
                messageType = NinchatMessage.Type.META,
                sender = messageId,
                message = NinchatMessage(NinchatMessage.Type.META, message, System.currentTimeMillis())))
        if (pendingMessageList.size > 1) return
        handleIncomingMessage()
    }

    fun addEndMessage() {
        pendingMessageList.addLast(NinchatPendingMessage(
                messageType = NinchatMessage.Type.END,
                sender = "",
                message = NinchatMessage(NinchatMessage.Type.END, System.currentTimeMillis())))
        if (pendingMessageList.size > 1) return
        handleIncomingMessage()
    }

    fun clear() {
        messageIds = emptyList()
        messageMap.clear()
    }

    fun getLastMessageId(allowMeta: Boolean): String {
        if (messageIds.isEmpty()) {
            return "" // Imaginary message id preceding all actual ids.
        }
        val iterator = messageIds.listIterator(messageIds.size - 1)
        while (iterator.hasPrevious() && !allowMeta) {
            val id = iterator.previous()
            val message = messageMap[id]
            if (message != null && (message.type == NinchatMessage.Type.MESSAGE || message.type == NinchatMessage.Type.MULTICHOICE)) {
                return id
            }
        }
        return messageIds[messageIds.size - 1]
    }

    fun size(): Int {
        return messageIds.size
    }

    fun getItemId(position: Int): Long {
        return 1L * messageIds[position].hashCode()
    }

    fun getMessage(position: Int): NinchatMessage? {
        val id = messageIds[position]
        return messageMap[id]
    }

    private data class NinchatPendingMessage(val messageType: NinchatMessage.Type, val sender: String, val message: NinchatMessage? = null)
    private inner class NinchatMessageDiffUtil(private val oldList: List<String> = emptyList(), private val newList: List<String> = emptyList()) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

    }
}

interface INinchatMessageList {
    fun callback(diffResult: DiffUtil.DiffResult, position: Int)
}