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
    private var messageIds: List<String> = emptyList()
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
        for (pendingMessage in pendingMessageList) {
            when (pendingMessage.messageType) {
                NinchatMessage.Type.CLEAR -> {
                    newList.clear()
                    messageMap.clear()
                }
                NinchatMessage.Type.WRITING -> {
                    if (newList.contains(pendingMessage.sender)) {
                        continue
                    }
                    newList.add(pendingMessage.sender)
                    messageMap[pendingMessage.sender] = pendingMessage.message!!
                }
                NinchatMessage.Type.REMOVE_WRITING -> {
                    val at = newList.indexOf(pendingMessage.sender)
                    if (at == -1) {
                        continue
                    }
                    // remove the message from `messageIds` and  `messageMap`
                    newList.removeAt(at)
                    messageMap.remove(pendingMessage.sender)
                }
                NinchatMessage.Type.MESSAGE -> {
                    if (newList.contains(pendingMessage.sender)) {
                        continue
                    }
                    val writingId =
                        "$WRITING_MESSAGE_ID_PREFIX ${pendingMessage.message!!.senderId}"
                    newList.apply {
                        // remove writing if there is any
                        val at = newList.indexOf(writingId)
                        if (at > -1)
                            removeAt(at)
                        // add message id
                        add(pendingMessage.sender)
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
                    if (newList.contains(pendingMessage.sender)) {
                        continue
                    }
                    newList.add(pendingMessage.sender)
                    messageMap[pendingMessage.sender] = pendingMessage.message!!
                }
                NinchatMessage.Type.END -> {
                    // get end message id from here since it require calling messageIds
                    val endMessageId = getLastMessageId(true) + "zzzzz"
                    newList.add(endMessageId)
                    messageMap[endMessageId] = pendingMessage.message!!
                }
                else -> {
                    Log.d("NinchatMessageList", "Unknown message ${pendingMessage.messageType}")
                }
            }
        }
        diffUtilAsync(newList = newList)
    }

    private fun applyDiffUtil(
        newList: List<String> = emptyList(),
        diffResult: DiffUtil.DiffResult
    ) {
        // assign to new list
        messageIds = newList
        // Call diff callback
        mAdapter.callback(diffResult = diffResult, position = size())
    }

    private fun diffUtilAsync(newList: List<String> = emptyList()) {
        Single
            .create<Pair<List<String>, DiffUtil.DiffResult>> { emitter ->
                val sortedNewList = newList.sorted()
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

    private data class NinchatPendingMessage(
        val messageType: NinchatMessage.Type,
        val sender: String,
        val message: NinchatMessage? = null
    )

    private inner class NinchatMessageDiffUtil(
        private val oldList: List<String> = emptyList(),
        private val newList: List<String> = emptyList()
    ) : DiffUtil.Callback() {
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