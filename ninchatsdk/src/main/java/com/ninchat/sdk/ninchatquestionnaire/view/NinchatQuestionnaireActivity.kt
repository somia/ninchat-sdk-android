package com.ninchat.sdk.ninchatquestionnaire.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.ninchatquestionnaire.model.NinchatQuestionnaireModel
import com.ninchat.sdk.ninchatquestionnaire.presenter.INinchatQuestionnairePresenter
import com.ninchat.sdk.ninchatquestionnaire.presenter.NinchatQuestionnairePresenter
import kotlinx.android.synthetic.main.activity_ninchat_questionnaire.*

class NinchatQuestionnaireActivity : NinchatBaseActivity(), INinchatQuestionnairePresenter {
    lateinit var mRecyclerView: RecyclerView
    lateinit var mLayoutManager: LinearLayoutManager

    val ninchaQuestionnairePresenter = NinchatQuestionnairePresenter(
            ninchatQuestionnaireModel = NinchatQuestionnaireModel(),
            iNinchatQuestionnairePresenter = this
    )

    override val layoutRes: Int
        get() = R.layout.activity_ninchat_questionnaire

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ninchaQuestionnairePresenter.updateQueueId(intent = intent)
        ninchaQuestionnairePresenter.updateQuestionnaireType(intent = intent)

        mRecyclerView = questionnaire_form_rview as RecyclerView
        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLayoutManager

        if (ninchaQuestionnairePresenter.isConversationLikeQuestionnaire()) {
            ninchaQuestionnairePresenter.renderConversationLikeQuestionnaire(
                    recyclerView = mRecyclerView,
                    context = this,
                    layoutManager = mLayoutManager
            )
        } else {
            ninchaQuestionnairePresenter.renderFormLikeQuestionnaire(
                    recyclerView = mRecyclerView,
                    context = this
            )
        }
    }

    override fun onDestroy() {
        // dispose
        ninchaQuestionnairePresenter.dispose()
        super.onDestroy()
    }

    fun onClose(view: View?) {
        setResult(RESULT_CANCELED, null)
        finish()
    }

    override fun onCompleteQuestionnaire(currentIntent: Intent) {
        setResult(RESULT_OK, currentIntent)
        finish()
    }
}