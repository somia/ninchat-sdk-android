package com.ninchat.sdk.espresso.ninchatquestionnaire.ninchatbutton.presenter

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter.INinchatButtonViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.presenter.NinchatButtonViewPresenter
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatButtonViewPresenterTest {
    @Test
    fun `should_fire_both_next_and_back_button_update_callback_with_proper_parameter`() {
        val json = """{
            "back": true,    
            "next": true
        }""".trimMargin()
        val jsonObject = JSONObject(json)
        for (back in listOf(true, false, "Back", "")) {
            for (next in listOf(true, false, "Next", "")) {
                jsonObject.putOpt("back", back)
                jsonObject.putOpt("next", next)
                val iButtonViewPresenter = object : INinchatButtonViewPresenter {
                    override fun onBackButtonUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean) {
                        when (back) {
                            true -> {
                                Assert.assertEquals("visible", imageButton, visible)
                                Assert.assertEquals("text", "true", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                            false -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "false", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                            "Back" -> {
                                Assert.assertEquals("visible", !imageButton, visible)
                                Assert.assertEquals("text", "Back", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                            "" -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                        }
                    }

                    override fun onNextNextUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean) {
                        when (next) {
                            true -> {
                                Assert.assertEquals("visible", imageButton, visible)
                                Assert.assertEquals("text", "true", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                            false -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "false", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                            "Back" -> {
                                Assert.assertEquals("visible", !imageButton, visible)
                                Assert.assertEquals("text", "Back", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                            "" -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "", text)
                                Assert.assertEquals("clicked", false, clicked)
                            }
                        }
                    }
                }
                val ninchatButtonViewPresenter = NinchatButtonViewPresenter(
                        jsonObject = jsonObject,
                        iPresenter = iButtonViewPresenter
                )
                ninchatButtonViewPresenter.renderCurrentView()
            }
        }
    }

    @Test
    fun `check_button_clicked_and_corrosponding_view_change_after_click`() {
        val json = """{
            "back": true,    
            "next": true
        }""".trimMargin()
        val jsonObject = JSONObject(json)
        for (back in listOf(true, false, "Back", "")) {
            for (next in listOf(true, false, "Next", "")) {
                jsonObject.putOpt("back", back)
                jsonObject.putOpt("next", next)
                var backClicked = false
                var nextClicked = false
                val iButtonViewPresenter = object : INinchatButtonViewPresenter {
                    override fun onBackButtonUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean) {
                        when (back) {
                            true -> {
                                Assert.assertEquals("visible", imageButton, visible)
                                Assert.assertEquals("text", "true", text)
                                Assert.assertEquals("clicked", backClicked, clicked)
                            }
                            false -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "false", text)
                                Assert.assertEquals("clicked", backClicked, clicked)
                            }
                            "Back" -> {
                                Assert.assertEquals("visible", !imageButton, visible)
                                Assert.assertEquals("text", "Back", text)
                                Assert.assertEquals("clicked", backClicked, clicked)
                            }
                            "" -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "", text)
                                Assert.assertEquals("clicked", backClicked, clicked)
                            }
                        }
                    }

                    override fun onNextNextUpdated(visible: Boolean, text: String?, imageButton: Boolean, clicked: Boolean) {
                        when (next) {
                            true -> {
                                Assert.assertEquals("visible", imageButton, visible)
                                Assert.assertEquals("text", "true", text)
                                Assert.assertEquals("clicked", nextClicked, clicked)
                            }
                            false -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "false", text)
                                Assert.assertEquals("clicked", nextClicked, clicked)
                            }
                            "Back" -> {
                                Assert.assertEquals("visible", !imageButton, visible)
                                Assert.assertEquals("text", "Back", text)
                                Assert.assertEquals("clicked", nextClicked, clicked)
                            }
                            "" -> {
                                Assert.assertEquals("visible", false, visible)
                                Assert.assertEquals("text", "", text)
                                Assert.assertEquals("clicked", nextClicked, clicked)
                            }
                        }
                    }
                }
                val ninchatButtonViewPresenter = NinchatButtonViewPresenter(
                        jsonObject = jsonObject,
                        iPresenter = iButtonViewPresenter
                )
                ninchatButtonViewPresenter.renderCurrentView()
                backClicked = true
                ninchatButtonViewPresenter.onBackButtonClicked()
                nextClicked = true
                ninchatButtonViewPresenter.onNextButtonClicked()
            }
        }
    }
}