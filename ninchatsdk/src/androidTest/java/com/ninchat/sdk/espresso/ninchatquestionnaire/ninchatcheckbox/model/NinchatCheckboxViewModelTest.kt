package com.ninchat.sdk.espresso.ninchatquestionnaire.ninchatcheckbox.model

import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.ninchatcheckbox.model.NinchatCheckboxViewModel
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatCheckboxViewModelTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)

    val siteConfig = """{
      "default": {
        "translations": {
          "Back": "Takaisin",
          "Next": "Jatka",
          "test-label": "test-label-translated"
        }
      }
    }""".trimIndent()

    @Test
    fun `should_translate_label`() {
        val jsonObject = JSONObject("""{
            "result": true,
            "label": "test-label",
            "hasError": true,
            "fireEvent": true
        }""".trimIndent())

        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)
        val ninchatCheckboxViewModel = NinchatCheckboxViewModel(isFormLikeQuestionnaire = false).apply {
            parse(jsonObject = jsonObject)
        }

        Assert.assertEquals("test-label-translated", ninchatCheckboxViewModel.label)
    }
}