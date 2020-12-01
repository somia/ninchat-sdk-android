package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    @JvmName("onNextQuestionnaire")
    fun onNextQuestionnaire(onNextQuestionnaire: OnNextQuestionnaire) {
        val positionStart = size()
        val itemCount = model.loadNext()
        viewCallback.onAddItem(positionStart = positionStart, itemCount = itemCount)
    }
}

interface INinchatQuestionnaireListPresenter {
    fun onAddItem(positionStart: Int, itemCount: Int)
}