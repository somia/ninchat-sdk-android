package com.ninchat.sdk.espresso.ninchatquestionnaire.ninchatdropdownselect.model

import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.model.NinchatDropDownSelectViewModel
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatDropDownSelectViewModelTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)

    val siteConfig = """{
      "default": {
        "translations": {
          "a": "a-t",
          "c": "c-t"
        }
      }
    }""".trimIndent()

    @Test
    fun `should_parse_data_class_from_json_object`() {
        val jsonObject = JSONObject("""{
            "result": "a",
            "label": "test-label",
            "hasError": true,
            "fireEvent": true,
            "options": [
                {"label": "a"},
                {"label": "b"},
                {"label": "c"}
            ]
        }""".trimIndent())

        val ninchatDropDownViewModel = NinchatDropDownSelectViewModel(
                isFormLikeQuestionnaire = false).parse(jsonObject = jsonObject)
        Assert.assertEquals(false, ninchatDropDownViewModel.isFormLikeQuestionnaire)
        Assert.assertEquals("test-label", ninchatDropDownViewModel.label)
        Assert.assertEquals(true, ninchatDropDownViewModel.hasError)
        Assert.assertEquals(true, ninchatDropDownViewModel.fireEvent)
        Assert.assertEquals(1, ninchatDropDownViewModel.selectedIndex)
        Assert.assertEquals("a", ninchatDropDownViewModel.value)
        Assert.assertEquals(listOf<String>("Select", "a", "b", "c"), ninchatDropDownViewModel.optionList)
    }

    @Test
    fun `should_translate_options_list`() {
        val jsonObject = JSONObject("""{
            "result": "a",
            "label": "test-label",
            "hasError": true,
            "fireEvent": true,
            "options": [
                {"label": "a"},
                {"label": "b"},
                {"label": "c"}
            ]
        }""".trimIndent())
        NinchatSession.Builder(appContext, configurationKey).create()
        NinchatSessionManager.getInstance().ninchatState.siteConfig.setConfigString(siteConfig)
        val ninchatDropDownViewModel = NinchatDropDownSelectViewModel(
                isFormLikeQuestionnaire = false).parse(jsonObject = jsonObject)

        Assert.assertEquals(listOf("Select", "a-t", "b", "c-t"), ninchatDropDownViewModel.optionList)
    }
}