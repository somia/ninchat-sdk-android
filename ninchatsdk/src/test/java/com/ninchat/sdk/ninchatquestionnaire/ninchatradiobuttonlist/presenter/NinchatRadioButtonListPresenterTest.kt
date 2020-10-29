package com.ninchat.sdk.ninchatquestionnaire.ninchatradiobuttonlist.presenter

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatRadioButtonListPresenterTest {
    @Test
    fun `should render form view`() {
        val viewCallback = object : INinchatRadioButtonListPresenter {
            override fun onUpdateFormView(label: String, hasError: Boolean) {
                Assert.assertEquals("", label)
                Assert.assertEquals(false, hasError)
            }

            override fun onUpdateConversationView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val ninchatRadioButtonListPresenter = NinchatRadioButtonListPresenter(jsonObject = null, isFormLikeQuestionnaire = true, viewCallback = viewCallback)
        ninchatRadioButtonListPresenter.renderCurrentView(jsonObject = null)

    }

    @Test
    fun `should render conversation view`() {
        val viewCallback = object : INinchatRadioButtonListPresenter {
            override fun onUpdateFormView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)

            }

            override fun onUpdateConversationView(label: String, hasError: Boolean) {
                Assert.assertEquals("", label)
                Assert.assertEquals(false, hasError)
            }
        }
        val ninchatRadioButtonListPresenter = NinchatRadioButtonListPresenter(jsonObject = null, isFormLikeQuestionnaire = false, viewCallback = viewCallback)
        ninchatRadioButtonListPresenter.renderCurrentView(jsonObject = null)
    }

    @Test
    fun `should handle toggle option and toggle data model`() {
        val viewCallback = object : INinchatRadioButtonListPresenter {
            override fun onUpdateFormView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)

            }

            override fun onUpdateConversationView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val ninchatRadioButtonListPresenter = NinchatRadioButtonListPresenter(jsonObject = null, isFormLikeQuestionnaire = false, viewCallback = viewCallback)
        ninchatRadioButtonListPresenter.handleOptionToggled(isSelected = true, position = 1)

        Assert.assertEquals(null, ninchatRadioButtonListPresenter.getModel().value)
        Assert.assertEquals(1, ninchatRadioButtonListPresenter.getModel().position)
        Assert.assertEquals(false, ninchatRadioButtonListPresenter.getModel().hasError)
    }

    @Test
    fun `should return isSelected true`() {
        val viewCallback = object : INinchatRadioButtonListPresenter {
            override fun onUpdateFormView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)

            }

            override fun onUpdateConversationView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val jsonObject = JSONObject("""{
            "position": 1
        }""".trimIndent())
        val ninchatRadioButtonListPresenter = NinchatRadioButtonListPresenter(jsonObject = null, isFormLikeQuestionnaire = false, viewCallback = viewCallback)
        ninchatRadioButtonListPresenter.getModel().position = 1
        Assert.assertEquals(true, ninchatRadioButtonListPresenter.isSelected(jsonObject = jsonObject))
    }

    @Test
    fun `should return isSelected false with previous no position`() {
        val viewCallback = object : INinchatRadioButtonListPresenter {
            override fun onUpdateFormView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)

            }

            override fun onUpdateConversationView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val jsonObject = JSONObject("""{
            "position": 1
        }""".trimIndent())
        val ninchatRadioButtonListPresenter = NinchatRadioButtonListPresenter(jsonObject = null, isFormLikeQuestionnaire = false, viewCallback = viewCallback)
        ninchatRadioButtonListPresenter.getModel().position = -1
        Assert.assertEquals(false, ninchatRadioButtonListPresenter.isSelected(jsonObject = jsonObject))
    }

    @Test
    fun `should return isSelected false with different position`() {
        val viewCallback = object : INinchatRadioButtonListPresenter {
            override fun onUpdateFormView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)

            }

            override fun onUpdateConversationView(label: String, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val jsonObject = JSONObject("""{
            "position": 1
        }""".trimIndent())
        val ninchatRadioButtonListPresenter = NinchatRadioButtonListPresenter(jsonObject = null, isFormLikeQuestionnaire = false, viewCallback = viewCallback)
        ninchatRadioButtonListPresenter.getModel().position = 2
        Assert.assertEquals(false, ninchatRadioButtonListPresenter.isSelected(jsonObject = jsonObject))
    }

}