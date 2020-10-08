package com.ninchat.sdk.activities

import org.junit.Assert
import org.junit.Test

class NinchatBaseActivityTest {

    @Test
    fun `back_button_should_be_disabled_by_default`() {
        val defaultBaseActivity = ActivityClass1()
        Assert.assertEquals(false, defaultBaseActivity.backButton())
    }

    @Test
    fun `back_button_should_be_enabled`() {
        val defaultBaseActivity = ActivityClass2()
        Assert.assertEquals(true, defaultBaseActivity.backButton())
    }
}

class ActivityClass1 : NinchatBaseActivity() {
    override val layoutRes: Int
        get() = TODO("Not yet implemented")

    fun backButton(): Boolean {
        return allowBackButton()
    }
}

class ActivityClass2 : NinchatBaseActivity() {
    override val layoutRes: Int
        get() = TODO("Not yet implemented")

    override fun allowBackButton(): Boolean {
        return true
    }

    fun backButton(): Boolean {
        return allowBackButton()
    }
}