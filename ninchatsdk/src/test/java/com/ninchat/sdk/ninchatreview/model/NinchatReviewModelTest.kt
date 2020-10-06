package com.ninchat.sdk.ninchatreview.model

import com.ninchat.sdk.NinchatSessionManager
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito

class NinchatReviewModelTest {
    @Test
    fun `rating payload should contain current given rating`(){
        val ninchatReviewModel = NinchatReviewModel()
        val expectedRating = 3
        ninchatReviewModel.currentRating = expectedRating
        val ratingPayload = ninchatReviewModel.getRatingPayload()
        val rating = ratingPayload.optJSONObject("data").optInt("rating", -1)
        Assert.assertEquals(expectedRating, rating)
    }
}