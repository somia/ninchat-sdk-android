package com.ninchat.sdk.ninchatquestionnaire.ninchattext.model

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatTextViewModelTest {
    @Test
    fun `should create a default ninchat text view model`(){
        val ninchatTextViewModel = NinchatTextViewModel()
        Assert.assertEquals("", ninchatTextViewModel.label)
        Assert.assertEquals(true, ninchatTextViewModel.isFormLikeQuestionnaire)
    }

    @Test
    fun `should parse optional label from json payload`(){
        val jsonObject = JSONObject("""{
            "label": "sample label"
        }""".trimIndent())
        val ninchatTextViewModel = NinchatTextViewModel(
                isFormLikeQuestionnaire = false
        ).parse(jsonObject)
        Assert.assertEquals("sample label", ninchatTextViewModel.label)
        Assert.assertEquals(false, ninchatTextViewModel.isFormLikeQuestionnaire)
    }

    @Test
    fun `should parse mandatory label from json payload`(){
        val jsonObject = JSONObject("""{
            "label": "sample label",
            "required": true
        }""".trimIndent())
        val ninchatTextViewModel = NinchatTextViewModel(
                isFormLikeQuestionnaire = false
        ).parse(jsonObject)
        Assert.assertEquals("sample label *", ninchatTextViewModel.label)
        Assert.assertEquals(false, ninchatTextViewModel.isFormLikeQuestionnaire)
    }
}