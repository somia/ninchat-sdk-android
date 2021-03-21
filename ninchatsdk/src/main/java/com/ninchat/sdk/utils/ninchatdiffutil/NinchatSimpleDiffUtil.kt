package com.ninchat.sdk.utils.ninchatdiffutil

import android.util.Log
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class NinchatSimpleDiffUtil(
        val previousList: List<JSONObject> = listOf(),
        val newList: List<JSONObject> = listOf()
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return previousList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val previousId = previousList[oldItemPosition].optInt("uuid")
        val currentId = newList[newItemPosition].optInt("uuid")
        return previousId == currentId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listOf("name", "result", "hasError").all {
            previousList[oldItemPosition].optString(it) == newList[newItemPosition].optString(it)
        }
    }

    companion object {
        fun compute() {
            Single.create<String> { it.onSuccess("Hello world") }
                    .observeOn(AndroidSchedulers.mainThread())
                    .delay(2, TimeUnit.SECONDS, Schedulers.io())
                    .subscribe({
                        Log.e(">>", "Yea $it")
                    }, {})

        }
    }
}