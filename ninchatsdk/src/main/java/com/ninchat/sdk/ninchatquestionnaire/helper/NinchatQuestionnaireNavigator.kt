package com.ninchat.sdk.ninchatquestionnaire.helper

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import org.json.JSONObject

class NinchatQuestionnaireNavigator {
    companion object {
        private const val DURATION: Long = 500;

        /**
         * Get the next immediate element that is greater than given element index
         */
        fun getNextElement(questionnaireList: List<JSONObject> = listOf(), index: Int = 0): JSONObject? {
            return questionnaireList
                    .filterIndexed { currentIndex, _ -> currentIndex >= index }
                    .find { NinchatQuestionnaireType.isElement(it) }
        }

        /**
         * Get the next immediate element that is greater than given element index
         */
        fun getElementIndex(questionnaireList: List<JSONObject> = listOf(), elementName: String): Int {
            return questionnaireList
                    .indexOfFirst { it.optString("name") == elementName }
        }

        /**
         * Show smooth animation during loading
         */
        fun setAnimation(itemView: View, position: Int, notFirstItem: Boolean) {
            itemView.alpha = 0.0f
            val animatorSet = AnimatorSet()
            val animator: ObjectAnimator = ObjectAnimator.ofFloat(itemView, "alpha", 0f, 0.5f, 1.0f)
            ObjectAnimator.ofFloat(itemView, "alpha", 0f).start()
            animator.startDelay = if (notFirstItem) DURATION / 2 else position * DURATION / 3
            animator.duration = DURATION
            animatorSet.play(animator)
            animator.start()
        }
    }
}