package com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model.NinchatDropDownSelectViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.view.INinchatDropDownSelectViewHolder
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

class NinchatDropDownSelectViewPresenter(
        isFormLikeQuestionnaire: Boolean,
        val jsonObject: JSONObject?,
        val viewCallback: INinchatDropDownSelectViewPresenter,
) : INinchatDropDownSelectViewHolder {
    private val ninchatDropDownSelectViewModel = NinchatDropDownSelectViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
    ).parse(jsonObject = jsonObject)

    fun renderCurrentView() {
        if (ninchatDropDownSelectViewModel.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(label = ninchatDropDownSelectViewModel.label
                    ?: "", options = ninchatDropDownSelectViewModel.optionList)
        } else {
            viewCallback.onUpdateConversationView(label = ninchatDropDownSelectViewModel.label
                    ?: "", options = ninchatDropDownSelectViewModel.optionList)
        }
        // cal on item selection change
        onItemSelectionChange(ninchatDropDownSelectViewModel.selectedIndex)
    }

    override fun onItemSelectionChange(position: Int) {
        val value = ninchatDropDownSelectViewModel.optionList.getOrNull(position)
        ninchatDropDownSelectViewModel.hasError = false
        // first position is "Selected" and should be consider as not selected
        if (position == 0) {
            viewCallback.onUnSelected(
                    position = ninchatDropDownSelectViewModel.selectedIndex,
                    hasError = ninchatDropDownSelectViewModel.hasError)
            viewCallback.onUnSelected(
                    position = position,
                    hasError = ninchatDropDownSelectViewModel.hasError)
        } else {
            viewCallback.onUnSelected(
                    position = ninchatDropDownSelectViewModel.selectedIndex,
                    hasError = ninchatDropDownSelectViewModel.hasError)
            viewCallback.onSelected(
                    position = position,
                    hasError = ninchatDropDownSelectViewModel.hasError)
        }
        ninchatDropDownSelectViewModel.value = value
        ninchatDropDownSelectViewModel.selectedIndex = position
        // update json model
        ninchatDropDownSelectViewModel.updateJson(jsonObject = jsonObject)
        if (position != 0) {
            mayBeFireEvent()
        }
    }

    private fun mayBeFireEvent() {
        if (!ninchatDropDownSelectViewModel.fireEvent) return
        EventBus.getDefault().post(OnNextQuestionnaire(OnNextQuestionnaire.other))
    }
}

interface INinchatDropDownSelectViewPresenter {
    fun onUpdateFromView(label: String, options: List<String>)
    fun onUpdateConversationView(label: String, options: List<String>)
    fun onSelected(position: Int, hasError: Boolean)
    fun onUnSelected(position: Int, hasError: Boolean)
}