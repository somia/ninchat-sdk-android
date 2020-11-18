package com.ninchat.sdk.espresso.ninchatdb.light

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.ninchatdb.light.NinchatPersistenceStore
import com.ninchat.sdk.ninchatmedia.view.NinchatMediaActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatPersistenceStoreTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)
    lateinit var activityScenario: ActivityScenario<NinchatMediaActivity>


    @Before
    fun initialize() {
        NinchatPersistenceStore.remove("key1", appContext)
        NinchatPersistenceStore.remove("key2", appContext)
    }

    @After
    fun dispose() {
        try {
            NinchatPersistenceStore.remove("key1", appContext)
            NinchatPersistenceStore.remove("key2", appContext)
            activityScenario.close()
        } catch (err: Exception) {
            println(err)
        }
    }

    @Test
    fun should_save_and_retrive_key_value_pair_in_storage() {
        NinchatPersistenceStore.save("key1", "value1", appContext)
        NinchatPersistenceStore.save("key2", "value2", appContext)
        val value1 = NinchatPersistenceStore.get("key1", appContext)
        val value2 = NinchatPersistenceStore.get("key2", appContext)
        Assert.assertEquals("value1", value1)
        Assert.assertEquals("value2", value2)
    }

    @Test
    fun should_remove_key_from_storage() {
        NinchatPersistenceStore.save("key1", "value1", appContext)
        NinchatPersistenceStore.remove("key1", appContext)
        NinchatPersistenceStore.remove("key2", appContext)
        Assert.assertEquals(null, NinchatPersistenceStore.get("key1", appContext))
    }

    @Test
    fun should_correctly_found_element_from_storage() {
        NinchatPersistenceStore.save("key1", "value1", appContext)
        NinchatPersistenceStore.save("key2", "value2", appContext)
        Assert.assertEquals(true, NinchatPersistenceStore.has("key1", appContext))
        Assert.assertEquals(true, NinchatPersistenceStore.has("key2", appContext))
        Assert.assertEquals(false, NinchatPersistenceStore.has("key3", appContext))
    }
}