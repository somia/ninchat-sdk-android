package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatCheckboxViewModelTest {
    @Test
    fun `should_initialize_with_default_value`() {
        val ninchatCheckboxViewModel = NinchatCheckboxViewModel(isFormLikeQuestionnaire = false)
        Assert.assertEquals(false, ninchatCheckboxViewModel.isFormLikeQuestionnaire)
        Assert.assertEquals(false, ninchatCheckboxViewModel.isChecked)
        Assert.assertEquals("", ninchatCheckboxViewModel.label)
        Assert.assertEquals(false, ninchatCheckboxViewModel.hasError)
        Assert.assertEquals(false, ninchatCheckboxViewModel.fireEvent)
    }

    @Test
    fun `should_parse_data_class_from_json_object`() {
        val jsonObject = JSONObject("""{
            "result": true,
            "label": "test-label",
            "hasError": true,
            "fireEvent": true
        }""".trimIndent())

        val ninchatCheckboxViewModel = NinchatCheckboxViewModel(isFormLikeQuestionnaire = false).parse(
                jsonObject = jsonObject
        )
        Assert.assertEquals(false, ninchatCheckboxViewModel.isFormLikeQuestionnaire)
        Assert.assertEquals(true, ninchatCheckboxViewModel.isChecked)
        Assert.assertEquals("test-label", ninchatCheckboxViewModel.label)
        Assert.assertEquals(true, ninchatCheckboxViewModel.hasError)
        Assert.assertEquals(true, ninchatCheckboxViewModel.fireEvent)
    }
}