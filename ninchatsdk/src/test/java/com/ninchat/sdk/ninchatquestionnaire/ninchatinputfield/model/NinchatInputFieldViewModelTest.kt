package com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.model

import android.text.InputType
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatInputFieldViewModelTest {
    @Test
    fun `should_initialize_with_default_value`() {
        val ninchatInputFieldViewModel = NinchatInputFieldViewModel(isFormLikeQuestionnaire = false, isMultiline = false)
        Assert.assertEquals(false, ninchatInputFieldViewModel.isMultiline)
        Assert.assertEquals("", ninchatInputFieldViewModel.pattern)
        Assert.assertEquals(false, ninchatInputFieldViewModel.isFormLikeQuestionnaire)
        Assert.assertEquals("", ninchatInputFieldViewModel.label)
        Assert.assertEquals("", ninchatInputFieldViewModel.value)
        Assert.assertEquals(0, ninchatInputFieldViewModel.inputType)
        Assert.assertEquals(false, ninchatInputFieldViewModel.hasError)
        Assert.assertEquals(false, ninchatInputFieldViewModel.hasFocus)
    }

    @Test
    fun `should_parse_data_class_from_json_object`() {
        val jsonObject = JSONObject("""{
            "result": "test-result",
            "pattern": "test-pattern",
            "label": "test-label",
            "inputmode": "text",
            "hasError": true
        }""".trimIndent())


        val ninchatInputFieldViewModel = NinchatInputFieldViewModel(isFormLikeQuestionnaire = false, isMultiline = false).parse(jsonObject = jsonObject)
        Assert.assertEquals(false, ninchatInputFieldViewModel.isMultiline)
        Assert.assertEquals("test-pattern", ninchatInputFieldViewModel.pattern)
        Assert.assertEquals(false, ninchatInputFieldViewModel.isFormLikeQuestionnaire)
        Assert.assertEquals("test-label", ninchatInputFieldViewModel.label)
        Assert.assertEquals("test-result", ninchatInputFieldViewModel.value)
        Assert.assertEquals(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES, ninchatInputFieldViewModel.inputType)
        Assert.assertEquals(true, ninchatInputFieldViewModel.hasError)
        Assert.assertEquals(false, ninchatInputFieldViewModel.hasFocus)
    }
}