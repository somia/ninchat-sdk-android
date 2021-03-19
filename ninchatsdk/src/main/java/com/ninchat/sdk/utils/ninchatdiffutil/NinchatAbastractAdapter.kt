package com.ninchat.sdk.utils.ninchatdiffutil

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayDeque

abstract class NinchatAbastractAdapter<T, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    protected var items: List<T> = listOf()
    private val pendingUpdates: ArrayDeque<List<T>> = ArrayDeque()

    fun updateItems(diff: DiffUtil.Callback, newItems: List<T>) {
        pendingUpdates.addLast(newItems)
        if (pendingUpdates.size > 1) {
            return
        }
        updateItemsInternal(diff, newItems)
    }

    private fun applyDiffResult(diff: DiffUtil.Callback,
                                newItems: List<T>,
                                diffResult: DiffUtil.DiffResult) {
        pendingUpdates.removeFirst()
        dispatchUpdates(newItems, diffResult)
        if (pendingUpdates.size > 0) {
            updateItemsInternal(diff, pendingUpdates.first())
        }
    }

    // This method does the heavy lifting of
    // pushing the work to the background thread
    private fun updateItemsInternal(diff: DiffUtil.Callback, newItems: List<T>) {
        Single.create<DiffUtil.DiffResult> { it.onSuccess(DiffUtil.calculateDiff(diff)) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ applyDiffResult(diff, newItems, it) }, { TODO() })
    }

    // This method does the work of actually updating
    // the backing data and notifying the adapter
    private fun dispatchUpdates(newItems: List<T>,
                                diffResult: DiffUtil.DiffResult) {
        diffResult.dispatchUpdatesTo(this)
        items = newItems
    }

}
