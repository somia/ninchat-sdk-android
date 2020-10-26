package com.ninchat.sdk.ninchatquestionnaire.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model.NinchatDropDownSelectViewModel
import com.ninchat.sdk.ninchatquestionnaire.view.INinchatDropDownSelectViewHolder
import org.json.JSONObject

class NinchatDropDownSelectViewPresenter(
        isFormLikeQuestionnaire: Boolean,
        jsonObject: JSONObject?,
        val viewCallback: INinchatDropDownSelectViewPresenter,
) : INinchatDropDownSelectViewHolder {
    private val ninchatDropDownSelectViewModel = NinchatDropDownSelectViewModel(
            isFormLikeQuestionnaire = isFormLikeQuestionnaire,
    ).parse(jsonObject = jsonObject)
    
    fun renderCurrentView() {
        if (ninchatDropDownSelectViewModel.isFormLikeQuestionnaire) {
            viewCallback.onUpdateFromView(label = ninchatDropDownSelectViewModel.label
                    ?: "", options = ninchatDropDownSelectViewModel.optionList)
            return
        }
        viewCallback.onUpdateConversationView(label = ninchatDropDownSelectViewModel.label
                ?: "", options = ninchatDropDownSelectViewModel.optionList)

        onItemSelectionChange(ninchatDropDownSelectViewModel.selectedIndex)
    }

    override fun onItemSelectionChange(position: Int) {
        val value = ninchatDropDownSelectViewModel.optionList[position]
        // first position is "Selected" and should be consider as not selected
        if (position == 0) {
            viewCallback.onUnSelected(
                    position = ninchatDropDownSelectViewModel.selectedIndex,
                    hasError = ninchatDropDownSelectViewModel.hasError)
            viewCallback.onUnSelected(
                    position = position,
                    hasError = ninchatDropDownSelectViewModel.hasError)
        } else {
            ninchatDropDownSelectViewModel.hasError = false
            viewCallback.onUnSelected(
                    position = ninchatDropDownSelectViewModel.selectedIndex,
                    hasError = ninchatDropDownSelectViewModel.hasError)
            viewCallback.onSelected(
                    position = position,
                    hasError = ninchatDropDownSelectViewModel.hasError)
        }
        ninchatDropDownSelectViewModel.value = value
        ninchatDropDownSelectViewModel.selectedIndex = position
    }
}

interface INinchatDropDownSelectViewPresenter {
    fun onUpdateFromView(label: String, options: List<String>)
    fun onUpdateConversationView(label: String, options: List<String>)
    fun onSelected(position: Int, hasError: Boolean)
    fun onUnSelected(position: Int, hasError: Boolean)
}