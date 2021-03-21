package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.presenter

import com.ninchat.sdk.events.OnNextQuestionnaire
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.view.QuestionnaireActivityCallback
import com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnairelist.view.NinchatQuestionnaireListAdapter
import com.ninchat.sdk.utils.ninchatdiffutil.QuestionnaireListDiffUtil
import org.json.JSONObject

class NinchatConversationListPresenter(
        questionnaireList: List<JSONObject>,
        preAnswers: List<Pair<String, Any>>,
        var rootActivityCallback: QuestionnaireActivityCallback,
        val mAdapter: NinchatQuestionnaireListAdapter,
) : NinchatQuestionnaireListPresenter(questionnaireList = questionnaireList, preAnswers = preAnswers) {
    private val questionnaireDiffUtil = QuestionnaireListDiffUtil()
    override fun init() {
        // try to get the first element
        val nextElement = getNextElement(currentIndex = 0, 100)
        // load bot view or if nothing found just finish
        nextElement?.let {
            addBotWritingView(nextTarget = it, thankYouText = null)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    private fun updateElementPosition() {
        model.answerList.forEachIndexed { index, jsonObject ->
            jsonObject.putOpt("isLast", isLast(index))
        }
    }

    private fun loadNextByTarget(targetName: String?) {
        loadNextByElementName(elementName = targetName)
        updateElementPosition()
        questionnaireDiffUtil.updateList(currentItemList = model.answerList, mAdapter = mAdapter, mActivityCallback = rootActivityCallback)
    }

    private fun loadNextByElement(elementName: JSONObject? = null) {
        model.addElement(jsonObject = elementName)
        updateElementPosition()
        questionnaireDiffUtil.updateList(currentItemList = model.answerList, mAdapter = mAdapter, mActivityCallback = rootActivityCallback)
    }

    private fun addBotWritingView(nextTarget: String?, thankYouText: String?) {
        val currentElement = NinchatQuestionnaireJsonUtil.getBotElement(botName = model.getBotName(), botImgUrl = model.getBotAvatar(), targetElement = nextTarget, thankYouText = thankYouText)
        loadNextByElement(elementName = currentElement)
    }

    override fun addThankYouView(isComplete: Boolean) {
        val thankYouText = if (isComplete) model.audienceRegisterCloseText() else model.audienceRegisterText()
        thankYouText?.let {
            addBotWritingView(nextTarget = "thankYouText", thankYouText = thankYouText)
        } ?: rootActivityCallback.onFinishQuestionnaire(openQueue = false)
    }

    override fun showNext(onNextQuestionnaire: OnNextQuestionnaire?) {
        // if a thank you text
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.thankYou) {
            rootActivityCallback.onFinishQuestionnaire(openQueue = false)
            return
        }
        if (onNextQuestionnaire?.moveType == OnNextQuestionnaire.back) {
            model.removeLast() + model.removeLast()
            updateElementPosition()
            questionnaireDiffUtil.updateList(currentItemList = model.answerList, mAdapter = mAdapter, mActivityCallback = null)
            return
        }
        // if the last answer has some error
        if (model.hasError()) {
            model.updateError()
            updateElementPosition()
            questionnaireDiffUtil.updateList(currentItemList = model.answerList, mAdapter = mAdapter, mActivityCallback = null)
            return
        }

        val index = model.getIndex(elementName = model.selectedElement.lastOrNull()?.first)
        val nextTargetName = getNextElement(currentIndex = index + 1, 1000)
        if (nextTargetName == "_complete") {
            rootActivityCallback.onComplete(answerList = getAnswerList())
            return
        }
        if (nextTargetName == "_register") {
            rootActivityCallback.onRegistered(answerList = getAnswerList())
            return
        }
        // otherwise simply load next element by target name or if there is no target then treat as _complete
        nextTargetName?.let {
            addBotWritingView(nextTarget = nextTargetName, thankYouText = null)
        } ?: rootActivityCallback.onComplete(answerList = getAnswerList())
    }

    // override bot loading view
    override fun onCompleteLoading(target: String?, thankYouText: String?, loaded: Boolean, position: Int) {
        val isLoaded = loaded || model.answerList.getOrNull(position)?.optBoolean("loaded", false) == true
        when (target) {
            "thankYouText" -> {
                val nextElement = NinchatQuestionnaireJsonUtil.getThankYouElement(thankYouString = thankYouText
                        ?: "")
                loadNextByElement(elementName = nextElement)
            }
            else -> {
                if (isLoaded) return
                loadNextByTarget(targetName = target)
            }
        }
        super.onCompleteLoading(target = target, thankYouText, loaded = loaded, position = position)
    }

    override fun isLast(at: Int): Boolean {
        val lastElementCount = model.selectedElement.lastOrNull()?.second ?: 0
        return at + lastElementCount >= model.answerList.size
    }

    override fun size() = questionnaireDiffUtil.size()

    override fun getByMuskedPosition(index: Int): JSONObject {
        return model.answerList.getOrNull(index) ?: JSONObject()
    }
}