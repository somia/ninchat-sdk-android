package com.ninchat.sdk.ninchatquestionnaire.ninchatquestionnaireactivity.model

import android.content.Intent
import com.ninchat.client.Props
import com.ninchat.client.Strings
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser
import com.ninchat.sdk.ninchataudiencemetadata.NinchatAudienceMetadata
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireConstants
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireJsonUtil
import com.ninchat.sdk.ninchatquestionnaire.helper.NinchatQuestionnaireNormalizer
import org.json.JSONArray
import org.json.JSONObject

data class NinchatQuestionnaireModel(
        var questionnaireType: Int = NinchatQuestionnaireConstants.preAudienceQuestionnaire,
        var queueId: String? = null,
        var isFormLike: Boolean = true,
        var questionnaireList: List<JSONObject> = listOf(),
        var answers: NinchatQuestionnaireAnswers? = null,
        var fromComplete: Boolean = false,
) {

    fun update(intent: Intent?) {
        intent?.getIntExtra(QUESTIONNAIRE_TYPE, -1)?.let {
            questionnaireType = it
        }
        intent?.getStringExtra(QUEUE_ID)?.let {
            queueId = it
        }
        val questionnaireArr = if (questionnaireType == NinchatQuestionnaireConstants.preAudienceQuestionnaire)
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPreAudienceQuestionnaire()
        else
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPostAudienceQuestionnaire()

        questionnaireList = NinchatQuestionnaireNormalizer.unifyQuestionnaireList(questionnaireArr = questionnaireArr)
        isFormLike = if (questionnaireType == NinchatQuestionnaireConstants.preAudienceQuestionnaire) {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPreAudienceQuestionnaireStyle() != "conversation"
        } else {
            NinchatSessionManager.getInstance()?.ninchatState?.siteConfig?.getPostAudienceQuestionnaireStyle() != "conversation"
        }
    }

    fun audienceMetadata(): NinchatAudienceMetadata {
        return NinchatSessionManager.getInstance()?.ninchatState?.audienceMetadata
                ?: NinchatAudienceMetadata()
    }

    fun preAnswers(): List<Pair<String, Any>> {
        return NinchatPropsParser.getPreAnswersFromProps(audienceMetadata().get())
    }

    fun isQueueClosed(): Boolean {
        // todo ( pallab ) currently if queue not found then we consider it as open queue
        val queue = NinchatSessionManager.getInstance()?.ninchatState?.getQueueList()?.find { it.id == queueId }
        return queue?.let {
            return it.isClosed
        } ?: false
    }

    fun getAnswersAsProps(): Props {
        val answerProps = Props()
        if (answers?.answerList.isNullOrEmpty().not()) {
            answers?.answerList?.forEach {
                answerProps.setString(it.first, it.second)
            }
        }
        if (answers?.tagList.isNullOrEmpty().not()) {
            val tags = Strings()
            answers?.tagList?.forEach { tags.append(it) }
            answerProps.setStringArray("tags", tags)
        }
        return answerProps
    }

    fun getAnswersAsJson(): JSONObject {
        val answerProps = JSONObject()
        if (answers?.answerList.isNullOrEmpty().not()) {
            answers?.answerList?.forEach {
                answerProps.putOpt(it.first, it.second)
            }
        }
        if (answers?.tagList.isNullOrEmpty().not()) {
            answerProps.putOpt("tags", JSONArray(answers?.tagList))
        }
        return answerProps
    }

    companion object {
        const val OPEN_QUEUE = "openQueue"
        const val QUEUE_ID = "queueId"
        const val QUESTIONNAIRE_TYPE = "questionType"
    }
}

data class NinchatQuestionnaireAnswers(
        var answerList: List<Pair<String, String>> = listOf(),
        var tagList: List<String> = listOf(),
        var queueId: String? = null,
) {

    fun parse(answerList: List<JSONObject> = listOf()) {
        this.answerList = NinchatQuestionnaireJsonUtil.getQuestionnaireAnswers(answerList = answerList)
        tagList = NinchatQuestionnaireJsonUtil.getQuestionnaireTags(answerList = answerList)
        queueId = NinchatQuestionnaireJsonUtil.getQuestionnaireQueue(answerList = answerList)
    }
}