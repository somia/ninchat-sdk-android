package com.ninchat.sdk.utils.ninchatdiffutil

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject

class QuestionnaireListDiffUtil {
    private var itemList: List<JSONObject> = emptyList()
    private val pendingUpdates: ArrayDeque<List<JSONObject>> = ArrayDeque()

    fun updateList(currentItemList: List<JSONObject> = emptyList(), mAdapter: NinchatQuestionnaireListAdapter, mActivityCallback: QuestionnaireActivityCallback?) {
        pendingUpdates.addLast(currentItemList)
        if (pendingUpdates.size > 1) return
        diffUtilAsync(currentItemList = currentItemList, mAdapter = mAdapter, mActivityCallback = mActivityCallback)
    }

    private fun applyDiffResult(newList: List<JSONObject>, mAdapter: NinchatQuestionnaireListAdapter, diffResult: DiffUtil.DiffResult, mActivityCallback: QuestionnaireActivityCallback?) {
        pendingUpdates.removeFirst()
        diffResult.dispatchUpdatesTo(mAdapter)
        mActivityCallback?.scrollTo(itemList.size)
        itemList = newList
        if (pendingUpdates.size > 0) {
            diffUtilAsync(
                    currentItemList = pendingUpdates.first(),
                    mAdapter = mAdapter,
                    mActivityCallback = mActivityCallback,
            )
        }
    }

    private fun diffUtilAsync(currentItemList: List<JSONObject> = emptyList(), mAdapter: NinchatQuestionnaireListAdapter, mActivityCallback: QuestionnaireActivityCallback?) {
        Single
                .create<Pair<DiffUtil.DiffResult, List<JSONObject>>> { emitter ->
                    val diffResult = DiffUtil.calculateDiff(NinchatDiffUtil(oldList = itemList, newList = currentItemList))
                    val newList = currentItemList.map {
                        NinchatQuestionnaireJsonUtil.slowCopy(it)
                    }
                    emitter.onSuccess(Pair(diffResult, newList))
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    applyDiffResult(newList = result.second, mAdapter = mAdapter, diffResult = result.first, mActivityCallback = mActivityCallback)
                }, {
                    Log.e("diffUtlAsync", it.message ?: "Error in diffUtil Async task")
                })
    }

    fun size(): Int = itemList.size

    private inner class NinchatDiffUtil(private val oldList: List<JSONObject> = emptyList(), private val newList: List<JSONObject> = emptyList()) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val previousId = oldList[oldItemPosition].optInt("uuid")
            val currentId = newList[newItemPosition].optInt("uuid")
            return previousId == currentId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return listOf("uuid", "name", "result", "hasError", "isLast").all {
                oldList[oldItemPosition].optString(it) == newList[newItemPosition].optString(it)
            }
        }
    }
}