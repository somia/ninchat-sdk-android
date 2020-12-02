package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNavigator
import com.ninchat.sdk.ninchatquestionnaire.helper.fromJSONArray
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.model.NinchatQuestionnaireListModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class NinchatQuestionnaireListPresenter(
        questionnaireList: List<JSONObject>,
        val viewCallback: INinchatQuestionnaireListPresenter,
) {
    init {
        EventBus.getDefault().register(this)
    }

    val model = NinchatQuestionnaireListModel(questionnaireList = questionnaireList).apply {
        parse()
    }

    fun get(at: Int): JSONObject = model.answerList.getOrNull(at) ?: JSONObject()
    fun size() = model.answerList.size

    fun loadNext(elementName: String?): Int {
        if (!model.hasMatch(elementName = elementName)) return 0
        val index = model.getIndex(elementName = elementName)
        val nextElement = model.nextElement(index = index + 1)
        val itemCount = model.addElement(jsonObject = nextElement)
        return itemCount
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("onNextQuestionnaire")
    fun onNextQuestionnaire(onNextQuestionnaire: OnNextQuestionnaire) {
        when {
            onNextQuestionnaire.moveType == OnNextQuestionnaire.back -> {
                val positionStart = size()
                val itemCount = model.removeLast()
                viewCallback.onItemRemoved(positionStart = positionStart, itemCount = itemCount)
                return
            }
            onNextQuestionnaire.moveType == OnNextQuestionnaire.thankYou -> {
                // close questionnaire
            }
            model.hasError() -> {
                val positionStart = size()

            }
        }
        val positionStart = size()
        val matchedLogic = NinchatQuestionnaireJsonUtil.getMatchingLogic(
                questionnaireList = model.questionnaireList,
                elementName = model.selectedElement.lastOrNull()?.first ?: "~",
                answerList = model.answerList)
        val itemCount = loadNext(elementName = matchedLogic?.optJSONObject("logic")?.optString("target"))
        if (itemCount > 0)
            viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount)
    }
}

interface INinchatQuestionnaireListPresenter {
    fun onAddItem(positionStart: Int, itemCount: Int)
    fun onItemRemoved(positionStart: Int, itemCount: Int)
    fun onItemUpdate(positionStart: Int, itemCount: Int)
}