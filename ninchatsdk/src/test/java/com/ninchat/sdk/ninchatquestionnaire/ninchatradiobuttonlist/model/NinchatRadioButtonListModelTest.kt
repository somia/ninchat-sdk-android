package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.model

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatRadioButtonListModelTest {
    @Test
    fun `should initialize NinchatRadioButtonListModel data class with default value`(){
        val ninchatRadioButtonListModel = NinchatRadioButtonListModel()
        Assert.assertEquals(true, ninchatRadioButtonListModel.isFormLikeQuestionnaire)
        Assert.assertEquals("", ninchatRadioButtonListModel.label)
        Assert.assertEquals(false, ninchatRadioButtonListModel.hasError)
        Assert.assertEquals("", ninchatRadioButtonListModel.value)
        Assert.assertEquals(null, ninchatRadioButtonListModel.optionList)
        Assert.assertEquals(-1, ninchatRadioButtonListModel.position)
        Assert.assertEquals(false, ninchatRadioButtonListModel.fireEvent)
    }

    @Test
    fun `should initialize NinchatRadioButtonListModel by provided json`(){
        val jsonObject = JSONObject("""{
            "label": "test-label",
            "hasError": true,
            "result": "test-result",
            "options": [
                {"value": "a", "label": "la"},
                {"value": "b", "label": "lb"}
            ],
            "position": 1,
            "fireEvent": true,
        }""".trimIndent())

        val jsonArr = JSONArray("""[
            {"value": "a", "label": "la"},
            {"value": "b", "label": "lb"}
        ]""".trimIndent())

        val ninchatRadioButtonListModel = NinchatRadioButtonListModel().apply {
            parse(jsonObject = jsonObject)
        }
        Assert.assertEquals(true, ninchatRadioButtonListModel.isFormLikeQuestionnaire)
        Assert.assertEquals("test-label", ninchatRadioButtonListModel.label)
        Assert.assertEquals(true, ninchatRadioButtonListModel.hasError)
        Assert.assertEquals("test-result", ninchatRadioButtonListModel.value)
        Assert.assertEquals(jsonArr.toString(2), ninchatRadioButtonListModel.optionList?.toString(2))
        Assert.assertEquals(1, ninchatRadioButtonListModel.position)
        Assert.assertEquals(true, ninchatRadioButtonListModel.fireEvent)
    }

    @Test
    fun `should update model with params`(){
        val jsonObject = JSONObject("""{
            "label": "test-label",
            "hasError": true,
            "result": "result-1",
            "options": [
                {"value": "a", "label": "la"},
                {"value": "b", "label": "lb"}
            ],
            "position": 1
        }""".trimIndent())

        val updatedJsonObject = JSONObject("""{
            "hasError": false,
            "result": "result-2",
            "position": 2
        }""".trimIndent())


        val ninchatRadioButtonListModel = NinchatRadioButtonListModel().apply {
            parse(jsonObject = jsonObject)
        }
        ninchatRadioButtonListModel.update(jsonObject = updatedJsonObject)
        Assert.assertEquals(false, ninchatRadioButtonListModel.hasError)
        Assert.assertEquals("result-2", ninchatRadioButtonListModel.value)
        Assert.assertEquals(2, ninchatRadioButtonListModel.position)
    }

    @Test
    fun `should return value at given position`(){
        val jsonObject = JSONObject("""{
            "label": "test-label",
            "hasError": true,
            "result": "result-1",
            "options": [
                {"value": "a", "label": "la"},
                {"value": "b", "label": "lb"}
            ],
            "position": 1
        }""".trimIndent())

        val ninchatRadioButtonListModel = NinchatRadioButtonListModel().apply {
            parse(jsonObject = jsonObject)
        }
        Assert.assertEquals("b", ninchatRadioButtonListModel.getValue(1))
    }

    @Test
    fun `should return null value for wrong index`(){
        val jsonObject = JSONObject("""{
            "label": "test-label",
            "hasError": true,
            "result": "result-1",
            "options": [
                {"value": "a", "label": "la"},
                {"value": "b", "label": "lb"}
            ],
            "position": 1
        }""".trimIndent())

        val ninchatRadioButtonListModel = NinchatRadioButtonListModel().apply {
            parse(jsonObject = jsonObject)
        }
        Assert.assertEquals(null, ninchatRadioButtonListModel.getValue(10000))
    }

    @Test
    fun `should update json attributes`(){
        val jsonObject = JSONObject("""{
            "label": "test-label",
            "hasError": true,
            "result": "result-1",
            "options": [
                {"value": "a", "label": "la"},
                {"value": "b", "label": "lb"}
            ],
            "position": 1
        }""".trimIndent())

        val ninchatRadioButtonListModel = NinchatRadioButtonListModel().apply {
            parse(jsonObject = jsonObject)
        }
        ninchatRadioButtonListModel.hasError = false
        ninchatRadioButtonListModel.value = "result-2"
        ninchatRadioButtonListModel.position = 2
        ninchatRadioButtonListModel.updateJson(jsonObject = jsonObject)
        Assert.assertEquals(false, jsonObject.optBoolean("hasError"))
        Assert.assertEquals("result-2", jsonObject.optString("result"))
        Assert.assertEquals(2, jsonObject.optInt("position"))
    }




}