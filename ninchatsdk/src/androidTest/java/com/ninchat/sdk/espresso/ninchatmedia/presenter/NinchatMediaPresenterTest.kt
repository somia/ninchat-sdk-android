package com.ninchat.sdk.espresso.ninchatmedia.presenter

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.ninchatmedia.model.NinchatMediaModel
import com.ninchat.sdk.ninchatmedia.presenter.NinchatMediaPresenter
import com.ninchat.sdk.ninchatmedia.view.NinchatMediaActivity
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatMediaPresenterTest {

    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)
    lateinit var activityScenario: ActivityScenario<NinchatMediaActivity>

    @After
    fun dispose() {
        try {
            activityScenario.close()
        } catch (err: Exception) {
            println(err)
        }
    }

    @Test
    fun should_update_file_id_from_intent() {
        val intent = NinchatMediaPresenter.getLaunchIntent(appContext, "12345").run {
            putExtra("isDebug", true)
        }
        val ninchatMediaPresenter = NinchatMediaPresenter(
                ninchatMediaModel = NinchatMediaModel(),
                callback = null,
                mContext = appContext
        ).apply {
            updateFileId(intent = intent)
        }

        Assert.assertEquals("12345", ninchatMediaPresenter.ninchatMediaModel.fileId)
    }

    @Test
    fun fetch_launch_intent_with_file_id() {
        val intent = NinchatMediaPresenter.getLaunchIntent(appContext, "123456")
        Assert.assertEquals("123456", intent.getStringExtra(NinchatMediaModel.FILE_ID))
    }

}