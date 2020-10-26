package com.ninchat.sdk.espresso.ninchatquestionnaire.ninchatinputfield.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.model.NinchatInputFieldViewModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.presenter.INinchatInputFieldViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatinputfield.presenter.NinchatInputFieldViewPresenter
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatInputFieldViewPresenterTest {

    @Test
    fun `should_render_form_view_with_given_params`() {
        val jsonObject = JSONObject("""{
            "result": "test-result",
            "pattern": "test-pattern",
            "label": "test-label",
            "hasError": false
        }""".trimIndent())

        val viewCallback = object : INinchatInputFieldViewPresenter {
            override fun onUpdateFromView(label: String) {
                Assert.assertEquals("test-label", label)
            }

            override fun onUpdateConversationView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateText(value: String, hasError: Boolean) {
                Assert.assertEquals("test-result", value)
                Assert.assertEquals(false, hasError)
            }

            override fun onUpdateFocus(hasFocus: Boolean) {
                Assert.assertEquals(false, hasFocus)
            }
        }
        val ninchatInputFieldViewPresenter = NinchatInputFieldViewPresenter(
                jsonObject = jsonObject,
                isMultiline = false,
                isFormLikeQuestionnaire = true,
                viewCallback = viewCallback)

        ninchatInputFieldViewPresenter.renderCurrentView()
    }

    @Test
    fun `should_render_conversation_view_with_given_params`() {
        val jsonObject = JSONObject("""{
            "result": "test-result",
            "pattern": "test-pattern",
            "label": "test-label",
            "hasError": false
        }""".trimIndent())

        val viewCallback = object : INinchatInputFieldViewPresenter {
            override fun onUpdateFromView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateConversationView(label: String) {
                Assert.assertEquals("test-label", label)
            }

            override fun onUpdateText(value: String, hasError: Boolean) {
                Assert.assertEquals("test-result", value)
                Assert.assertEquals(false, hasError)
            }

            override fun onUpdateFocus(hasFocus: Boolean) {
                Assert.assertEquals(false, hasFocus)
            }
        }
        val ninchatInputFieldViewPresenter = NinchatInputFieldViewPresenter(
                jsonObject = jsonObject,
                isMultiline = false,
                isFormLikeQuestionnaire = false,
                viewCallback = viewCallback)

        ninchatInputFieldViewPresenter.renderCurrentView()
    }

    @Test
    fun `should_call_onTextChange_callback_with_proper_params`() {
        val jsonObject = JSONObject("""{
            "result": "test-result",
            "pattern": "test-pattern",
            "label": "test-label",
            "hasError": false
        }""".trimIndent())

        val viewCallback = object : INinchatInputFieldViewPresenter {
            override fun onUpdateFromView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateConversationView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateText(value: String, hasError: Boolean) {
                Assert.assertEquals("new-text", value)
                Assert.assertEquals(false, hasError)
            }

            override fun onUpdateFocus(hasFocus: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val ninchatInputFieldViewPresenter = NinchatInputFieldViewPresenter(
                jsonObject = jsonObject,
                isMultiline = false,
                isFormLikeQuestionnaire = false,
                viewCallback = viewCallback)

        ninchatInputFieldViewPresenter.onTextChange("new-text")
    }

    @Test
    fun `should_call_onFocusChange_callback_when_there_is_no_error`() {
        val jsonObject = JSONObject("""{
            "result": "test-result",
            "pattern": "test-pattern",
            "label": "test-label",
            "hasError": false
        }""".trimIndent())

        val viewCallback = object : INinchatInputFieldViewPresenter {
            override fun onUpdateFromView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateConversationView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateText(value: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateFocus(hasFocus: Boolean) {
                Assert.assertEquals(true, true)
            }
        }
        val ninchatInputFieldViewPresenter = NinchatInputFieldViewPresenter(
                jsonObject = jsonObject,
                isMultiline = false,
                isFormLikeQuestionnaire = false,
                viewCallback = viewCallback)

        ninchatInputFieldViewPresenter.onFocusChange(true)
    }

    @Test
    fun `should_not_call_onFocusChange_callback_when_there_is_error`() {
        val jsonObject = JSONObject("""{
            "result": "test-result",
            "pattern": "test-pattern",
            "label": "test-label",
            "hasError": true
        }""".trimIndent())
        val viewCallback = object : INinchatInputFieldViewPresenter {
            override fun onUpdateFromView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateConversationView(label: String) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateText(value: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateFocus(hasFocus: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val ninchatInputFieldViewPresenter = NinchatInputFieldViewPresenter(
                jsonObject = jsonObject,
                isMultiline = false,
                isFormLikeQuestionnaire = false,
                viewCallback = viewCallback)
        ninchatInputFieldViewPresenter.onFocusChange(true)
    }

    @Test
    fun `should_update_a_json_model`() {
        val jsonObject = JSONObject("""{
            "result": "test-result",
            "pattern": "test-pattern",
            "label": "test-label",
            "inputmode": "text",
            "hasError": true
        }""".trimIndent())
        val ninchatInputFieldViewModel = NinchatInputFieldViewModel(
                isFormLikeQuestionnaire = false, isMultiline = false).parse(jsonObject = jsonObject)
        ninchatInputFieldViewModel.hasError = true
        ninchatInputFieldViewModel.value = "test"
        ninchatInputFieldViewModel.updateJson(jsonObject = jsonObject)
        Assert.assertEquals("test", jsonObject.optString("result"))
        Assert.assertEquals(true, jsonObject.optBoolean("hasError"))
    }
}