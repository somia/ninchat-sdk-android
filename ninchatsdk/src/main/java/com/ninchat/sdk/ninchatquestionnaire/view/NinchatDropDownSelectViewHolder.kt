package com.ninchat.sdk.ninchatquestionnaire.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject

class NinchatDropDownSelectViewHolder(
        itemView: View,
        jsonObject: JSONObject?,
        isFormLikeQuestionnaire: Boolean = true,
) : RecyclerView.ViewHolder(itemView) {

    fun update(jsonObject: JSONObject?, isFormLikeQuestionnaire: Boolean = true) {
    }

}

interface INinchatDropDownSelectViewHolder {
    fun onItemSelectionChange(position: Int)
}