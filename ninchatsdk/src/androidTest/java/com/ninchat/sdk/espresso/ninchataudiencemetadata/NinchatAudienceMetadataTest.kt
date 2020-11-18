package com.ninchat.sdk.espresso.ninchataudiencemetadata

import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.ninchataudiencemetadata.NinchatAudienceMetadata
import com.ninchat.sdk.ninchatdb.light.NinchatPersistenceStore
import com.ninchat.sdk.ninchatmedia.view.NinchatMediaActivity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NinchatAudienceMetadataTest {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(com.ninchat.sdk.R.string.ninchat_configuration_key)
    lateinit var activityScenario: ActivityScenario<NinchatMediaActivity>


    @Before
    fun initialize() {
        NinchatPersistenceStore.remove("audienceMetadata", appContext)
    }

    @After
    fun dispose() {
        try {
            NinchatPersistenceStore.remove("audienceMetadata", appContext)
            activityScenario.close()
        } catch (err: Exception) {
            println(err)
        }
    }

    @Test
    fun should_return_false_for_null_audience_metadata(){
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        Assert.assertEquals(false, audienceMetadata.has())
    }

    @Test
    fun should_return_true_for_empty_audience_metadata(){
        val audienceMetadata = NinchatAudienceMetadata(Props(), appContext)
        Assert.assertEquals(true, audienceMetadata.has())
    }

    @Test
    fun should_return_true_from_cached_audience_metadata(){
        NinchatPersistenceStore.save("audienceMetadata", "{}", appContext)
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        Assert.assertEquals(true, audienceMetadata.has())
    }

    @Test
    fun should_remove_audience_metadata_from_local_and_cache_store(){
        NinchatPersistenceStore.save("audienceMetadata", "{}", appContext)
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        Assert.assertEquals(true, audienceMetadata.has())
        audienceMetadata.remove()
        Assert.assertEquals(false, audienceMetadata.has())
    }

    @Test
    fun should_not_set_null_audience_metadata(){
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        audienceMetadata.set(null)
        Assert.assertEquals(false, audienceMetadata.has())
    }

    @Test
    fun should_set_empty_audience_metadata(){
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        audienceMetadata.set(Props())
        Assert.assertEquals(true, audienceMetadata.has())
    }

    @Test
    fun should_get_audience_metadata_from_memory(){
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        audienceMetadata.set(Props())
        Assert.assertEquals(Props(), audienceMetadata.get())
    }

    @Test
    fun should_get_audience_metadata_from_cache(){
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        audienceMetadata.set(Props())
        audienceMetadata.audienceMetadata = null
        Assert.assertEquals(Props(), audienceMetadata.get())
    }

    @Test
    fun set_null_audience_metadata_after_not_null_audience_medatadata(){
        val audienceMetadata = NinchatAudienceMetadata(null, appContext)
        audienceMetadata.set(Props())
        Assert.assertEquals(Props(), audienceMetadata.get())
        audienceMetadata.set(null)
        Assert.assertEquals(null, audienceMetadata.get())
    }
}