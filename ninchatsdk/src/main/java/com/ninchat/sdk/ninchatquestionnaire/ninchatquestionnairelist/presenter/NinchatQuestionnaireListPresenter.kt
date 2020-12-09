package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.ConversationLikeModel
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.FormLikeModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class NinchatQuestionnaireListPresenter(
        questionnaireList: List<JSONObject>,
        isFormLike: Boolean,
        val viewCallback: INinchatQuestionnaireListPresenter,
) {
    init {
        EventBus.getDefault().register(this)
    }

    val model = if (isFormLike) {
        FormLikeModel(
                questionnaireList = questionnaireList,
                answerList = listOf(),
                selectedElement = arrayListOf(),
                isFormLike = isFormLike
        ).apply {
            parse()
        }
    } else {
        ConversationLikeModel(
                questionnaireList = questionnaireList,
                answerList = listOf(),
                selectedElement = arrayListOf(),
                isFormLike = isFormLike
        ).apply { parse() }
    }

    fun get(at: Int): JSONObject = model.get(at)
    fun size() = model.size()

    private fun loadNext(elementName: String?): Int {
        if (!model.hasMatch(elementName = elementName)) return 0
        val index = model.getIndex(elementName = elementName)
        val nextElement = model.nextElement(index = index + 1)
        val itemCount = model.addElement(jsonObject = nextElement)
        return itemCount
    }

    private fun loadThankYou(text: String): Int {
        val nextElement = NinchatQuestionnaireJsonUtil.getThankYouElement(thankYouString = text)
        val itemCount = model.addElement(jsonObject = nextElement)
        return itemCount
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("onNextQuestionnaire")
    fun onNextQuestionnaire(onNextQuestionnaire: OnNextQuestionnaire) {
        val matchedLogic = NinchatQuestionnaireJsonUtil.getMatchingLogic(
                questionnaireList = model.questionnaireList,
                elementName = model.selectedElement.lastOrNull()?.first ?: "~",
                answerList = model.answerList)

        when {
            onNextQuestionnaire.moveType == OnNextQuestionnaire.back -> {
                val positionStart = size()
                val itemCount = model.removeLast()
                if (model.isFormLike) {
                    viewCallback.onDataSetChange()
                } else {
                    viewCallback.onItemRemoved(positionStart = positionStart, itemCount = itemCount)
                }
                return
            }
            onNextQuestionnaire.moveType == OnNextQuestionnaire.thankYou -> {
                // close questionnaire
                return
            }
            model.hasError() -> {
                val positionStart = size()
                val itemCount = model.selectedElement.lastOrNull()?.second ?: 0
                model.updateError()
                if (model.isFormLike) {
                    viewCallback.onDataSetChange()
                } else {
                    viewCallback.onItemUpdate(positionStart = positionStart - itemCount, itemCount = itemCount)
                }
                return
            }
            matchedLogic?.optJSONObject("logic")?.optString("target") == "_complete" -> {
                // reached to complete phase
                // get audience register close text
                model.audienceRegisterCloseText()?.let {
                    val positionStart = size()
                    val itemCount = loadThankYou(it)
                    if (itemCount > 0) {
                        if (model.isFormLike) {
                            viewCallback.onDataSetChange()
                        } else {
                            viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount)
                        }
                    }
                    return
                }
                return
            }
            matchedLogic?.optJSONObject("logic")?.optString("target") == "_register" -> {
                // reached to complete phase
                // get audience register test
                model.audienceRegisterText()?.let {
                    val positionStart = size()
                    val itemCount = loadThankYou(it)
                    if (itemCount > 0) {
                        if (model.isFormLike) {
                            viewCallback.onDataSetChange()
                        } else {
                            viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount)
                        }
                    }
                    return
                }
            }
        }
        val positionStart = size()
        val itemCount = loadNext(elementName = matchedLogic?.optJSONObject("logic")?.optString("target"))
        // there are still some item available
        if (itemCount > 0) {
            if (model.isFormLike) {
                viewCallback.onDataSetChange()
            } else {
                viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount)
            }
        }
    }

}

interface INinchatQuestionnaireListPresenter {
    fun onAddItem(positionStart: Int, itemCount: Int)
    fun onItemRemoved(positionStart: Int, itemCount: Int)
    fun onItemUpdate(positionStart: Int, itemCount: Int)
    fun onDataSetChange()
}