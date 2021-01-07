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
            viewCallback.onRenderFromView(label = model.label
                    ?: "", options = model.optionList, enabled = enabled, hasError = model.hasError, selectedIndex = model.selectedIndex)
        } else {
            viewCallback.onRenderConversationView(label = model.label
                    ?: "", options = model.optionList, enabled = enabled, hasError = model.hasError, selectedIndex = model.selectedIndex)
        }
    }

    fun updateCurrentView(jsonObject: JSONObject? = null, enabled: Boolean) {
        jsonObject?.let {
            model.update(jsonObject = jsonObject, enabled = enabled)
        }
        if (model.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(label = model.label
                    ?: "", options = model.optionList, enabled = enabled, hasError = model.hasError, selectedIndex = model.selectedIndex)
        } else {
            viewCallback.onUpdateConversationView(label = model.label
                    ?: "", options = model.optionList, enabled = enabled, hasError = model.hasError, selectedIndex = model.selectedIndex)
        }
    }

    override fun onItemSelectionChange(position: Int) {
        val value = model.optionList.getOrNull(position)
        model.value = if (value == "Select") null else value
        model.hasError = if (value == "Select") model.hasError else false
        // first position is "Selected" and should be consider as not selected
        viewCallback.onSelectionChange( selectedIndex = position, isSelected = position != 0, hasError = model.hasError, enabled = model.enabled)
        if (model.selectedIndex != position) {
            model.selectedIndex = position
            updateCallback.onUpdate(value = model.value, position = model.position, hasError = model.hasError)
            if (position != 0) {
                mayBeFireEvent()
            }
        }
    }

    private fun mayBeFireEvent() {
        if (!model.fireEvent) return
        EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other))
    }
}

interface INinchatDropDownSelectViewPresenter {
    fun onRenderFromView(label: String, options: List<String>, enabled: Boolean, hasError: Boolean, selectedIndex: Int)
    fun onRenderConversationView(label: String, options: List<String>, enabled: Boolean, hasError: Boolean, selectedIndex: Int)

    fun onUpdateFromView(label: String, options: List<String>, enabled: Boolean, hasError: Boolean, selectedIndex: Int)
    fun onUpdateConversationView(label: String, options: List<String>, enabled: Boolean, hasError: Boolean, selectedIndex: Int)

    fun onSelectionChange(selectedIndex: Int, isSelected: Boolean, hasError: Boolean, enabled: Boolean)
}


interface DropDownSelectUpdateListener {
    fun onUpdate(value: String?, position: Int, hasError: Boolean)
}