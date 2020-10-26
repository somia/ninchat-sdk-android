package com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatDropDownSelectViewModelTest {
    @Test
    fun `should_initialize_with_default_value`() {
        val ninchatDropDownViewModel = NinchatDropDownSelectViewModel(
                isFormLikeQuestionnaire = true)
        Assert.assertEquals(true, ninchatDropDownViewModel.isFormLikeQuestionnaire)
        Assert.assertEquals("", ninchatDropDownViewModel.label)
        Assert.assertEquals(false, ninchatDropDownViewModel.hasError)
        Assert.assertEquals(false, ninchatDropDownViewModel.fireEvent)
        Assert.assertEquals(0, ninchatDropDownViewModel.selectedIndex)
        Assert.assertEquals("", ninchatDropDownViewModel.value)
        Assert.assertEquals(listOf<String>(), ninchatDropDownViewModel.optionList)
    }
}