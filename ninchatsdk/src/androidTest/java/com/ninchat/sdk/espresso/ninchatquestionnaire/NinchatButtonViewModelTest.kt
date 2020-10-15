package com.ninchat.sdk.espresso.ninchatquestionnaire

import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatButtonViewModel
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatButtonViewModelTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)

    val siteConfig = """{
      "default": {
        "translations": {
          "Back": "Takaisin",
          "Next": "Jatka"
        }
      }
    }""".trimIndent()

    @Test
    fun `should_show_back_and_next_image_button`() {
        val json = """{
            "back": true,
            "next": true
        }""".trimIndent()
        val jsonObject = JSONObject(json)
        val ninchatButtonViewModel = NinchatButtonViewModel().parse(jsonObject = jsonObject)
        Assert.assertEquals(true, ninchatButtonViewModel.showPreviousImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousTextButton)

        Assert.assertEquals(true, ninchatButtonViewModel.showNextImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showNextTextButton)
    }

    @Test
    fun `should_show_back_and_next_text_button`() {
        val json = """{
            "back": "Back",
            "next": "Next"
        }""".trimIndent()
        val jsonObject = JSONObject(json)
        val ninchatButtonViewModel = NinchatButtonViewModel().parse(jsonObject = jsonObject)
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousImageButton)
        Assert.assertEquals(true, ninchatButtonViewModel.showPreviousTextButton)

        Assert.assertEquals(false, ninchatButtonViewModel.showNextImageButton)
        Assert.assertEquals(true, ninchatButtonViewModel.showNextTextButton)

        Assert.assertEquals("Back", ninchatButtonViewModel.previousButtonLabel)
        Assert.assertEquals("Next", ninchatButtonViewModel.nextButtonLabel)
    }

    @Test
    fun `should_hide_both_back_and_next_buttons`() {
        val json1 = """{
            "back": "",
            "next": ""
        }""".trimIndent()
        val json2 = """{
        }""".trimIndent()
        val ninchatButtonViewModel = NinchatButtonViewModel().parse(jsonObject = JSONObject(json1))
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousTextButton)

        Assert.assertEquals(false, ninchatButtonViewModel.showNextImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showNextTextButton)

        ninchatButtonViewModel.parse(jsonObject = JSONObject(json2))
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousTextButton)

        Assert.assertEquals(false, ninchatButtonViewModel.showNextImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showNextTextButton)
    }

    @Test
    fun `parse_all_button_attributes`() {
        val json1 = """{
            "back": "Back",
            "next": "Next",
            "fireEvent": true,
            "thankYouText": true
        }""".trimIndent()

        val ninchatButtonViewModel = NinchatButtonViewModel().parse(jsonObject = JSONObject(json1))
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousImageButton)
        Assert.assertEquals(true, ninchatButtonViewModel.showPreviousTextButton)

        Assert.assertEquals(false, ninchatButtonViewModel.showNextImageButton)
        Assert.assertEquals(true, ninchatButtonViewModel.showNextTextButton)

        Assert.assertEquals(true, ninchatButtonViewModel.fireEvent)
        Assert.assertEquals(true, ninchatButtonViewModel.isThankYouText)
    }

    @Test
    fun `should_translate_back_and_next_button`() {
        val json1 = """{
            "back": "Back",
            "next": "Next"
        }""".trimIndent()
        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)
        val ninchatButtonViewModel = NinchatButtonViewModel().parse(jsonObject = JSONObject(json1))
        Assert.assertEquals("Takaisin", ninchatButtonViewModel.previousButtonLabel)
        Assert.assertEquals("Jatka", ninchatButtonViewModel.nextButtonLabel)
    }
}