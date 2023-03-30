package com.ninchat.sdk.ninchatchatmessage

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.ninchat.sdk.models.NinchatMessage
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit


const val WRITING_MESSAGE_ID_PREFIX = "zzzzzwriting"
const val END_MESSAGE_ID_SUFFIX = "zzzzz"

fun <T> bufferDebounce(
    time: Long, unit: TimeUnit?
): ObservableTransformer<T, List<T>> {
    return ObservableTransformer { o: Observable<T> ->
        o.publish { v: Observable<T> ->
            v.buffer(
                v.debounce(time, unit)
                    .takeUntil(v.ignoreElements().toObservable<Any>())
            )
        }
    }
}


class NinchatMessageList(private val mAdapter: INinchatMessageList) {
    private var messageIds: List<Pair<String, Long>> = emptyList()
    private var messageMap: MutableMap<String, NinchatMessage> = mutableMapOf()
    private val subject: PublishSubject<NinchatPendingMessage> = PublishSubject.create()

    init {
        subject.compose(bufferDebounce(200, TimeUnit.MILLISECONDS))
            .subscribeOn(Schedulers.io())
            .subscribe({
                handleBufferedMessage(pendingMessageList = it)
            }, { err -> Log.e("NinchatMessageList", "${err.message}") })
    }

    private fun handleBufferedMessage(pendingMessageList: List<NinchatPendingMessage> = emptyList()) {
        val newList = messageIds.toMutableList()
        val currentTime = System.currentTimeMillis()
        for (pendingMessage in pendingMessageList) {
            when (pendingMessage.messageType) {
                NinchatMessage.Type.CLEAR -> {
                    newList.clear()
                    messageMap.clear()
                }
                NinchatMessage.Type.WRITING -> {
                    if (newList.any { it.first == pendingMessage.sender }) {
                        continue
                    }
                    newList.add(Pair(pendingMessage.sender, currentTime))
                    messageMap[pendingMessage.sender] = pendingMessage.message!!
                }
                NinchatMessage.Type.REMOVE_WRITING -> {
                    val at = newList.indexOfFirst { it.first == pendingMessage.sender }
                    if (at == -1) {
                        continue
                    }
                    // remove the message from `messageIds` and  `messageMap`
                    newList.removeAt(at)
                    messageMap.remove(pendingMessage.sender)
                }

                NinchatMessage.Type.REMOVE_END -> {
                    val lastMessage = newList.last()
                    if (lastMessage.first.endsWith(END_MESSAGE_ID_SUFFIX)) {
                        // remove the message from `messageIds` and  `messageMap`
                        newList.removeLast()
                        messageMap.remove(lastMessage.first)
                    }
                }
                NinchatMessage.Type.MESSAGE -> {
                    if (newList.any { it.first == pendingMessage.sender }) {
                        continue
                    }
                    val writingId =
                        "$WRITING_MESSAGE_ID_PREFIX ${pendingMessage.message!!.senderId}"
                    newList.apply {
                        // remove writing if there is any
                        val at = newList.indexOfFirst { it.first == writingId }
                        if (at > -1)
                            removeAt(at)
                        // add message id
                        add(Pair(pendingMessage.sender, currentTime))
                    }
                    messageMap.apply {
                        // remote writing id
                        remove(writingId)
                        // add message id
                        put(pendingMessage.sender, pendingMessage.message)
                    }
                }
                NinchatMessage.Type.META -> {
                    // if there is already a meta message of given sender id,
                    // then don't add again
                    if (newList.any { it.first == pendingMessage.sender }) {
                        continue
                    }
                    newList.add(Pair(pendingMessage.sender, currentTime))
                    messageMap[pendingMessage.sender] = pendingMessage.message!!
                }
                NinchatMessage.Type.END -> {
                    // get end message id from here since it require calling messageIds
                    val endMessageId = getLastMessageId(true) + END_MESSAGE_ID_SUFFIX
                    newList.add(Pair(endMessageId, currentTime))
                    messageMap[endMessageId] = pendingMessage.message!!
                }
                NinchatMessage.Type.DELETED -> {
                    val at = newList.indexOfFirst { it.first == pendingMessage.sender }
                    if (at > -1) {
                        // if it's already existing message,
                        //1.a: update timestamp of current message and its adjacent messages
                        for(i in -1..1) {
                            if(at + i < 0 || at + i >= newList.size) continue
                            newList[at + i] = newList[at + i].copy(second = currentTime)
                        }
                        //2: then mark it as deleted from the message map
                        messageMap[pendingMessage.sender]!!.apply {
                            markDeleted(pendingMessage.message?.rawMessage)
                        }
                    } else {
                        // otherwise, add it in the newList and messageMap
                        newList.add(Pair(pendingMessage.sender, currentTime))
                        messageMap[pendingMessage.sender] = pendingMessage.message!!
                    }
                }
                else -> {
                    Log.d("NinchatMessageList", "Unknown message ${pendingMessage.messageType}")
                }
            }
        }
        diffUtilAsync(newList = newList)
    }

    private fun applyDiffUtil(
        newList: List<Pair<String, Long>> = emptyList(),
        diffResult: DiffUtil.DiffResult
    ) {
        // assign to new list
        messageIds = newList
        // Call diff callback

        mAdapter.callback(diffResult = diffResult, position = size())
    }

    private fun diffUtilAsync(newList: List<Pair<String, Long>> = emptyList()) {
        Single
            .create<Pair<List<Pair<String, Long>>, DiffUtil.DiffResult>> { emitter ->
                val sortedNewList = newList.sortedBy { it.first }
                val diffResult = DiffUtil.calculateDiff(
                    NinchatMessageDiffUtil(
                        oldList = messageIds,
                        newList = sortedNewList
                    )
                )
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
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.WRITING,
                sender = "$WRITING_MESSAGE_ID_PREFIX $sender",
                message = NinchatMessage(
                    NinchatMessage.Type.WRITING,
                    sender,
                    System.currentTimeMillis()
                )
            )
        )
    }

    fun removeWriting(sender: String) {
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.REMOVE_WRITING,
                sender = "$WRITING_MESSAGE_ID_PREFIX $sender",
            )
        )
    }

    fun add(messageId: String, message: NinchatMessage) {
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.MESSAGE,
                sender = messageId,
                message = message
            )
        )
    }

    fun addDeletedMessage(messageId: String, message: NinchatMessage) {
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.DELETED,
                sender = messageId,
                message = message
            )
        )
    }

    fun addMetaMessage(messageId: String, message: String) {
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.META,
                sender = messageId,
                message = NinchatMessage(
                    NinchatMessage.Type.META,
                    message,
                    System.currentTimeMillis()
                )
            )
        )
    }

    fun addEndMessage() {
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.END,
                sender = "",
                message = NinchatMessage(NinchatMessage.Type.END, System.currentTimeMillis())
            )
        )
    }

    fun clear() {
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.CLEAR,
                sender = "",
            )
        )
    }

    fun getLastMessageId(allowMeta: Boolean): String {
        if (messageIds.isEmpty()) {
            return "" // Imaginary message id preceding all actual ids.
        }
        val iterator = messageIds.listIterator(messageIds.size - 1)
        while (iterator.hasPrevious() && !allowMeta) {
            val id = iterator.previous().first
            val message = messageMap[id]
            if (message != null && (message.type == NinchatMessage.Type.MESSAGE || message.type == NinchatMessage.Type.MULTICHOICE)) {
                return id
            }
        }
        return messageIds[messageIds.size - 1].first
    }

    fun removeChatCloseMessage() {
        subject.onNext(
            NinchatPendingMessage(
                messageType = NinchatMessage.Type.REMOVE_END,
                sender = "",
            )
        )
    }

    fun size(): Int {
        return messageIds.size
    }

    fun getItemId(position: Int): Long {
        return 1L * messageIds[position].hashCode()
    }

    fun getItemMuxedPosition(position: Int): Int {
        return messageIds[position].hashCode()
    }

    fun getMessage(position: Int): NinchatMessage? {
        val id = messageIds[position].first
        return messageMap[id]
    }

    fun getMessageByMuxedPosition(position: Int): NinchatMessage? {
        val id = messageIds.firstOrNull { it.hashCode() ==  position} ?: return null
        return messageMap[id.first]
    }

    private data class NinchatPendingMessage(
        val messageType: NinchatMessage.Type,
        val sender: String,
        val message: NinchatMessage? = null
    )

    private inner class NinchatMessageDiffUtil(
        private val oldList: List<Pair<String, Long>> = emptyList(),
        private val newList: List<Pair<String, Long>> = emptyList()
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].first == newList[newItemPosition].first && oldList[oldItemPosition].second == newList[newItemPosition].second
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].first == newList[newItemPosition].first && oldList[oldItemPosition].second == newList[newItemPosition].second
        }

    }
}

interface INinchatMessageList {
    fun callback(diffResult: DiffUtil.DiffResult, position: Int)
}