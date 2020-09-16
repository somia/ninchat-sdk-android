package com.ninchat.sdk.helper.siteconfigparser

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NinchatSiteConfigTest {
    @Test
    fun `site_config_should_be_null_initially`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        Assert.assertEquals(null, ninchatSiteConfig.siteConfig)
    }

    @Test
    fun `should_not_set_empty_site_config`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString(null)
        Assert.assertEquals(null, ninchatSiteConfig.siteConfig)
    }

    @Test
    fun `should_digest_exception_for_invalid_site_json`() {
        val ninchatSiteConfig = NinchatSiteConfig()
        ninchatSiteConfig.setConfigString("{key: invalid site config")
        Assert.assertEquals(null, ninchatSiteConfig.siteConfig)
    }
}