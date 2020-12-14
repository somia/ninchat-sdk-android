package com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model.NinchatDropDownSelectViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view.INinchatDropDownSelectViewHolder
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatDropDownSelectViewPresenter(
        isFormLikeQuestionnaire: Boolean,
        jsonObject: JSONObject? = null,
        val viewCallback: INinchatDropDownSelectViewPresenter,
        val updateCallback: DropDownSelectUpdateListener,
        position: Int
) : INinchatDropDownSelectViewHolder {
    private val model = NinchatDropDownSelectViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            position = position
    ).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(jsonObject: JSONObject? = null) {
        jsonObject?.let {
            model.update(jsonObject = jsonObject)
        }
        if (model.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(label = model.label
                    ?: "", options = model.optionList)
        } else {
            viewCallback.onUpdateConversationView(label = model.label
                    ?: "", options = model.optionList)
        }
        // cal on item selection change
        onItemSelectionChange(model.selectedIndex)
    }

    override fun onItemSelectionChange(position: Int) {
        val value = model.optionList.getOrNull(position)
        // first position is "Selected" and should be consider as not selected
        if (position == 0) {
            viewCallback.onUnSelected(
                    position = position,
                    hasError = model.hasError)
        } else {
            model.hasError = false
            viewCallback.onSelected(
                    position = position,
                    hasError = model.hasError)
        }
        model.value = if (value == "Select") "" else value
        model.selectedIndex = position
        updateCallback.onUpdate(value = model.value, position = model.position)
        if (position != 0) {
            mayBeFireEvent()
        }
    }

    private fun mayBeFireEvent() {
        if (!model.fireEvent) return
        EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other))
    }
}

interface INinchatDropDownSelectViewPresenter {
    fun onUpdateFromView(label: String, options: List<String>)
    fun onUpdateConversationView(label: String, options: List<String>)
    fun onSelected(position: Int, hasError: Boolean)
    fun onUnSelected(position: Int, hasError: Boolean)
}


interface DropDownSelectUpdateListener{
    fun onUpdate(value: String?, position: Int)
}