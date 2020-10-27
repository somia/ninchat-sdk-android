package com.ninchat.sdk.espresso.ninchatquestionnaire.ninchatdropdownselect.presenter

import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.INinchatDropDownSelectViewPresenter
import com.ninchat.sdk.ninchatquestionnaire.ninchatdropdownselect.presenter.NinchatDropDownSelectViewPresenter
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class NinchatDropDownSelectViewPresenterTest {
    @Test
    fun `should_render_form_view_with_given_params`() {
        val viewCallback = object : INinchatDropDownSelectViewPresenter {
            override fun onUpdateFromView(label: String, options: List<String>) {
                Assert.assertEquals("test-label", label)
                Assert.assertEquals(listOf("Select", "a", "b", "c"), options)
            }

            override fun onUpdateConversationView(label: String, options: List<String>) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onSelected(position: Int, hasError: Boolean) {
                Assert.assertEquals(1, position)
                Assert.assertEquals(false, hasError)
            }

            override fun onUnSelected(position: Int, hasError: Boolean) {
                Assert.assertEquals(1, position)
                Assert.assertEquals(false, hasError)
            }
        }
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

        val ninchatDownloadSelectViewPresenter = NinchatDropDownSelectViewPresenter(
                isFormLikeQuestionnaire = true,
                jsonObject = jsonObject,
                viewCallback = viewCallback)
        ninchatDownloadSelectViewPresenter.renderCurrentView()
    }

    @Test
    fun `should_call_unselect_and_selct_when_item_selection_change`() {
        val viewCallback = object : INinchatDropDownSelectViewPresenter {
            override fun onUpdateFromView(label: String, options: List<String>) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onUpdateConversationView(label: String, options: List<String>) {
                Assert.assertEquals("Should not be called", true)
            }

            override fun onSelected(position: Int, hasError: Boolean) {
                Assert.assertEquals(3, position)
                Assert.assertEquals(false, hasError)
            }

            override fun onUnSelected(position: Int, hasError: Boolean) {
                Assert.assertEquals(1, position)
                Assert.assertEquals(false, hasError)
            }
        }
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

        val ninchatDownloadSelectViewPresenter = NinchatDropDownSelectViewPresenter(
                isFormLikeQuestionnaire = true,
                jsonObject = jsonObject,
                viewCallback = viewCallback)

        ninchatDownloadSelectViewPresenter.onItemSelectionChange(3)
    }
}