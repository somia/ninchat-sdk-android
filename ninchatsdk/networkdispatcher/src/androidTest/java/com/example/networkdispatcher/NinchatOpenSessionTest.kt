package com.example.networkdispatcher

import android.content.res.Resources
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStream


@RunWith(AndroidJUnit4::class)
class NinchatOpenSessionTest {
    @Test
    fun openNewSession() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val configurationKey = appContext.getString(R.string.ninchat_configuration_key)
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
    }
}