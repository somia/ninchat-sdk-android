package com.ninchat.sdk.ninchatquestionnaire.presenter

import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class NinchatTextViewPresenterTest {
    @Test
    fun `create a default ninchat text view presenter`() {
        val jsonObject = JSONObject("""{
            "label": "sample label",
            "required": true
        }""".trimIndent())
        val mockedPresenter = Mockito.mock(INinchatTextViewPresenter::class.java)
        val ninchatTextViewPresenter = NinchatTextViewPresenter(
                jsonObject = jsonObject,
                iPresenter = mockedPresenter
        )
    }

    @Test
    fun `should render a form like view`() {
        val jsonObject = JSONObject("""{
            "label": "sample label",
            "required": true
        }""".trimIndent())
        val iPresenter = object : INinchatTextViewPresenter {
            override fun onUpdateConversationView(label: String?) {
                Assert.assertEquals("should not call", true, false)
            }

            override fun onUpdateFormView(label: String?) {
                Assert.assertEquals("sample label *", label)
            }
        }
        val ninchatTextViewPresenter = NinchatTextViewPresenter(
                jsonObject = jsonObject,
                isFormLikeQuestionnaire = true,
                iPresenter = iPresenter
        )
        ninchatTextViewPresenter.renderCurrentView()
    }

    @Test
    fun `should render a conversation like view`() {
        val jsonObject = JSONObject("""{
            "label": "sample label",
            "required": true
        }""".trimIndent())
        val iPresenter = object : INinchatTextViewPresenter {
            override fun onUpdateConversationView(label: String?) {
                Assert.assertEquals("sample label *", label)
            }

            override fun onUpdateFormView(label: String?) {
                Assert.assertEquals("should not call", true, false)
            }
        }
        val ninchatTextViewPresenter = NinchatTextViewPresenter(
                jsonObject = jsonObject,
                isFormLikeQuestionnaire = false,
                iPresenter = iPresenter
        )
        ninchatTextViewPresenter.renderCurrentView()
    }
}