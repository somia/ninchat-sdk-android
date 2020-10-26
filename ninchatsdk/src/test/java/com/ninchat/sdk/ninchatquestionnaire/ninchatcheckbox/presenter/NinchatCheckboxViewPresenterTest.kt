package com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model.NinchatCheckboxViewModel
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatCheckboxViewPresenterTest {
    @Test
    fun `should render form view with given params`() {
        val jsonObject = JSONObject("""{
            "result": true,
            "label": "test-label",
            "hasError": true,
            "fireEvent": true
        }""".trimIndent())

        val iPresent = object : INinchatCheckboxViewPresenter {
            override fun onCheckBoxToggled(isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateConversationView(label: String?, isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateFromView(label: String?, isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("test-label", label)
                Assert.assertEquals(true, isChecked)
                Assert.assertEquals(true, hasError)

            }
        }
        val ninchatCheckboxViewPresenter = NinchatCheckboxViewPresenter(
                isFormLikeQuestionnaire = true,
                jsonObject = jsonObject,
                iPresent = iPresent
        );

        ninchatCheckboxViewPresenter.renderCurrentView()
    }

    @Test
    fun `should render conversation view with given params`() {
        val jsonObject = JSONObject("""{
            "result": true,
            "label": "test-label",
            "hasError": true,
            "fireEvent": true
        }""".trimIndent())

        val iPresent = object : INinchatCheckboxViewPresenter {
            override fun onCheckBoxToggled(isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateConversationView(label: String?, isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("test-label", label)
                Assert.assertEquals(true, isChecked)
                Assert.assertEquals(true, hasError)

            }

            override fun onUpdateFromView(label: String?, isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val ninchatCheckboxViewPresenter = NinchatCheckboxViewPresenter(
                isFormLikeQuestionnaire = false,
                jsonObject = jsonObject,
                iPresent = iPresent
        );

        ninchatCheckboxViewPresenter.renderCurrentView()
    }

    @Test
    fun `should toggle checkbox with expected params`() {
        val jsonObject = JSONObject("""{
            "result": false,
            "hasError": true,
            "fireEvent": false
        }""".trimIndent())

        var iterationCount = 0
        val iPresent = object : INinchatCheckboxViewPresenter {
            override fun onCheckBoxToggled(isChecked: Boolean, hasError: Boolean) {
                when (iterationCount) {
                    0 -> {
                        Assert.assertEquals(true, isChecked)
                        Assert.assertEquals(false, hasError)
                        iterationCount += 1
                    }
                    1 -> {
                        Assert.assertEquals(false, isChecked)
                        Assert.assertEquals(false, hasError)
                        iterationCount += 1
                    }
                }

            }

            override fun onUpdateConversationView(label: String?, isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateFromView(label: String?, isChecked: Boolean, hasError: Boolean) {
                Assert.assertEquals("Should not be called", true)
            }
        }
        val ninchatCheckboxViewPresenter = NinchatCheckboxViewPresenter(
                isFormLikeQuestionnaire = false,
                jsonObject = jsonObject,
                iPresent = iPresent
        );

        ninchatCheckboxViewPresenter.handleCheckBoxToggled(true)
        ninchatCheckboxViewPresenter.handleCheckBoxToggled(false)
    }
}