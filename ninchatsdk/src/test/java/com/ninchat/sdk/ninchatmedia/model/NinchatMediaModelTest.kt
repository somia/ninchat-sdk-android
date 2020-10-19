package com.ninchat.sdk.ninchatmedia.model

import org.junit.Assert
import org.junit.Test

class NinchatMediaModelTest {
    @Test
    fun `should_match_file_id_string`() {
        Assert.assertEquals("fileId", NinchatMediaModel.FILE_ID)
    }
    @Test
    fun `should_return_file_as_null`() {
        Assert.assertNull(NinchatMediaModel().getFile())
    }
}