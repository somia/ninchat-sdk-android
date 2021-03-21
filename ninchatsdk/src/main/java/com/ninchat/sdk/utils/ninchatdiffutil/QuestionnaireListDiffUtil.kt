package com.ninchat.sdk.utils.ninchatdiffutil

import androidx.recyclerview.widget.DiffUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import org.json.JSONObject

class QuestionnaireListDiffUtil {
    private var itemList: List<JSONObject> = emptyList()
    fun updateList(currentItemList: List<JSONObject> = emptyList(), mAdapter: NinchatQuestionnaireListAdapter, mActivityCallback: QuestionnaireActivityCallback?) {
        diffUtlAsync(currentItemList = currentItemList, mAdapter = mAdapter, mActivityCallback = mActivityCallback)
    }

    private fun diffUtlAsync(currentItemList: List<JSONObject> = emptyList(), mAdapter: NinchatQuestionnaireListAdapter, mActivityCallback: QuestionnaireActivityCallback?) {
        val size = itemList.size
        val diffResult = DiffUtil.calculateDiff(NinchatDiffUtil(oldList = itemList, newList = currentItemList))
        diffResult.dispatchUpdatesTo(mAdapter)
        mActivityCallback?.scrollTo(size)
        // in the end update the list
        itemList = currentItemList.map {
            NinchatQuestionnaireJsonUtil.slowCopy(it)
        }
    }

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