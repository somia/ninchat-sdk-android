package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobutton.model

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatRadioButtonModelTest {
    @Test
    fun `create ninchat radio button view model with default value`() {
        val ninchatRadioButtonModel = NinchatRadioButtonModel()
        Assert.assertEquals("", ninchatRadioButtonModel.label)
        Assert.assertEquals(false, ninchatRadioButtonModel.hasError)
        Assert.assertEquals(false, ninchatRadioButtonModel.isSelected)
    }

    @Test
    fun `should parse label text from json object`() {
        val jsonObject = JSONObject("""{
            "label": "test-label"
        }""".trimIndent())
        val ninchatRadioButtonModel = NinchatRadioButtonModel().apply {
            parse(jsonObject = jsonObject)
        }
        Assert.assertEquals("test-label", ninchatRadioButtonModel.label)
    }
}