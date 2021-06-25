package com.ninchat.sdk.ninchatreview.view

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.ninchatreview.model.NinchatReviewModel
import com.ninchat.sdk.ninchatreview.presenter.NinchatReviewPresenter
import kotlinx.android.synthetic.main.activity_ninchat_review.*
import kotlinx.android.synthetic.main.activity_ninchat_review.view.*

// todo (pallab) convert to mvvm from current mvp in future iteration
class NinchatReviewActivity : NinchatBaseActivity() {
    override val layoutRes: Int
        get() = R.layout.activity_ninchat_review

    // ninchat review presenter
    private val ninchatReviewPresenter = NinchatReviewPresenter(NinchatReviewModel())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ninchatReviewPresenter.isConversationView()) {
            ninchatReviewPresenter.renderBotView(
                formView = review_rating_normal_view,
                botView = review_rating_bot_view,
                rootActivity = ninchat_review_activity
            )
        } else {
            ninchatReviewPresenter.renderFormView(
                formView = review_rating_normal_view,
                botView = review_rating_bot_view,
                rootActivity = ninchat_review_activity
            )
        }

        ninchatReviewPresenter.mayBeAttachTitlebar(
            view = ninchat_review_activity.ninchat_titlebar,
            callback = {
                onSkipClick(ninchat_review_activity)
            })
    }

    fun onGoodClick(view: View) {
        ninchatReviewPresenter.maybeSendRating(NinchatSession.Analytics.Rating.GOOD)
        setResult(Activity.RESULT_OK, ninchatReviewPresenter.getResultIntent())
        finish()
    }

    fun onFairClick(view: View) {
        ninchatReviewPresenter.maybeSendRating(NinchatSession.Analytics.Rating.FAIR)
        setResult(Activity.RESULT_OK, ninchatReviewPresenter.getResultIntent())
        finish()
    }

    fun onPoorClick(view: View) {
        ninchatReviewPresenter.maybeSendRating(NinchatSession.Analytics.Rating.POOR)
        setResult(Activity.RESULT_OK, ninchatReviewPresenter.getResultIntent())
        finish()
    }

    fun onSkipClick(view: View) {
        ninchatReviewPresenter.maybeSendRating(NinchatSession.Analytics.Rating.NO_ANSWER)
        setResult(Activity.RESULT_OK, ninchatReviewPresenter.getResultIntent())
        finish()
    }
}