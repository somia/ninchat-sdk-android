package com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.model

import com.ninchat.sdk.ninchatquestionnaire.ninchatbutton.model.NinchatButtonViewModel
import org.junit.Assert
import org.junit.Test

class NinchatButtonViewModelTest {
    @Test
    fun `create ninchat button view model with default value`() {
        val ninchatButtonViewModel = NinchatButtonViewModel()
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showPreviousTextButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showNextImageButton)
        Assert.assertEquals(false, ninchatButtonViewModel.showNextTextButton)
        Assert.assertEquals("", ninchatButtonViewModel.previousButtonLabel)
        Assert.assertEquals("", ninchatButtonViewModel.nextButtonLabel)
        Assert.assertEquals(false, ninchatButtonViewModel.previousButtonClicked)
        Assert.assertEquals(false, ninchatButtonViewModel.nextButtonClicked)
        Assert.assertEquals(false, ninchatButtonViewModel.fireEvent)
        Assert.assertEquals(false, ninchatButtonViewModel.isThankYouText)
    }
}