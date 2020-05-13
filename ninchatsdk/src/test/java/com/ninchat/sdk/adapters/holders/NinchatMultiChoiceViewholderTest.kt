package com.ninchat.sdk.adapters.holders

import android.view.View
import android.widget.TextView
import com.ninchat.sdk.models.NinchatMessage
import com.ninchat.sdk.models.NinchatOption
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.*

class NinchatMultiChoiceViewholderTest {
    @Test
    fun `should initiate NinchatMultiChoiceViewholder`() {
        NinchatMultiChoiceViewholder(mock(View::class.java))
    }

    @Test
    fun `should return ninchat option`() {
        val ninchatOption = mutableListOf(mock(NinchatOption::class.java))
        val ninchatMessage = NinchatMessage(
                NinchatMessage.Type.META,
                null,
                null,
                mock(JSONObject::class.java),
                ninchatOption,
                0L
        )

        val ninchatMultiChoiceViewholder = NinchatMultiChoiceViewholder(mock(View::class.java))
        Assert.assertEquals(ninchatOption, ninchatMultiChoiceViewholder.getNinchatOptions(ninchatMessage))
    }

    @Test
    fun `should return correct option for given index`() {
        val ninchatOption = mutableListOf(mock(NinchatOption::class.java),
                mock(NinchatOption::class.java),
                mock(NinchatOption::class.java))

        val ninchatMultiChoiceViewholder = NinchatMultiChoiceViewholder(mock(View::class.java))
        Assert.assertEquals(ninchatOption[0], ninchatMultiChoiceViewholder.getNinchatOption(ninchatOption,0))
        Assert.assertNotEquals(ninchatOption[0], ninchatMultiChoiceViewholder.getNinchatOption(ninchatOption,1))
    }

    @Test
    fun `should call sendUIAction with selected option when sendAction is true`() {
        // todo cover with instrumental test
    }

    @Test
    fun `should call callback  with selected option when sendAction is true`() {
        // todo cover with instrumental test
    }
}