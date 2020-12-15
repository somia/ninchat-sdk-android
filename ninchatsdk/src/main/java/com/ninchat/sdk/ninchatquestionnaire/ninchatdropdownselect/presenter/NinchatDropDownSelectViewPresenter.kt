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
        position: Int,
        enabled: Boolean,
) : INinchatDropDownSelectViewHolder {
    private val model = NinchatDropDownSelectViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
            position = position,
            enabled = enabled
    ).apply {
        parse(jsonObject = jsonObject)
    }

    fun renderCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        jsonObject?.let {
            model.update(jsonObject = jsonObject, enabled = enabled)
        }
        if (model.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(label = model.label
                    ?: "", options = model.optionList, enabled = enabled)
        } else {
            viewCallback.onUpdateConversationView(label = model.label
                    ?: "", options = model.optionList, enabled = enabled)
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
                    hasError = model.hasError, enabled = model.enabled)
        } else {
            model.hasError = false
            viewCallback.onSelected(
                    position = position,
                    hasError = model.hasError, model.enabled)
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
    fun onUpdateFromView(label: String, options: List<String>, enabled: Boolean)
    fun onUpdateConversationView(label: String, options: List<String>, enabled: Boolean)
    fun onSelected(position: Int, hasError: Boolean, enabled: Boolean)
    fun onUnSelected(position: Int, hasError: Boolean, enabled: Boolean)
}


interface DropDownSelectUpdateListener {
    fun onUpdate(value: String?, position: Int)
}