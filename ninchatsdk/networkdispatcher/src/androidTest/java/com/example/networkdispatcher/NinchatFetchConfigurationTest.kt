package com.example.networkdispatcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Test

import org.junit.runner.RunWith
import kotlin.Exception
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
class NinchatFetchConfigurationTest {
    @Test
    fun withCorrectServerAddressAndConfiguration() = runBlocking<Unit> {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val configurationKey = appContext.getString(R.string.ninchat_configuration_key)
        try {
            val configuration = NinchatFetchConfiguration.execute(serverAddress, configurationKey)
            val configurationJson = JSONObject(configuration)
            Assert.assertNotNull("should fetch configuration with right server and configuration key", configurationJson)
        } catch (e: Exception) {
            Assert.assertNull(e)
        }
    }

    @Test
    fun withEmptyServerAddress() = runBlocking<Unit> {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val serverAddress = null
        val configurationKey = appContext.getString(R.string.ninchat_configuration_key)
        try {
            val configuration = NinchatFetchConfiguration.execute(serverAddress, configurationKey)
            var configurationJson = JSONObject(configuration)
            Assert.assertNull(configurationJson)
        } catch (e: Exception) {
            Assert.assertNotNull("should through exception with empty server address", e)
        }
    }

    @Test
    fun withWrongNinchatConfiguration() = runBlocking<Unit> {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val serverAddress = appContext.getString(R.string.ninchat_server_address)
        val configurationKey = appContext.getString(R.string.ninchat_configuration_key_wrong)
        try {
            val configuration = NinchatFetchConfiguration.execute(serverAddress, configurationKey)
            var configurationJson = JSONObject(configuration)
            Assert.assertNull(configurationJson)
        } catch (e: Exception) {
            Assert.assertNotNull("should through exception with wrong configuration key", e)
        }
    }
}