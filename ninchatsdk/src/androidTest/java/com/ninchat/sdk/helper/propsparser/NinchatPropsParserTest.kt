package com.ninchat.sdk.helper.propsparser

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ninchat.client.JSON
import com.ninchat.client.Props
import com.ninchat.client.Strings
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
class NinchatPropsParserTest {

    @Test
    fun `get_queue_id_from_user_channel_with_null_props`() {
        val queueId = NinchatPropsParser.getQueueIdFromUserChannels(null)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_channel_with_no_channel`() {
        val userProps = Props()
        val queueId = NinchatPropsParser.getQueueIdFromUserChannels(userProps)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_channel_with_no_channel_attributes`() {
        val currentChannel = Props()
        val userProps = Props()
        userProps.setObject("7ohmt2sn00hqg", currentChannel)
        val queueId = NinchatPropsParser.getQueueIdFromUserChannels(userProps)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_channel_with_no_queue_id`() {
        val channelAttrs = Props()
        val currentChannel = Props()
        currentChannel.setObject("channel_attrs", channelAttrs)
        val userProps = Props()
        userProps.setObject("7ohmt2sn00hqg", currentChannel)
        val queueId = NinchatPropsParser.getQueueIdFromUserChannels(userProps)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_channel_with_queue_id`() {
        val channelAttrs = Props()
        channelAttrs.setString("queue_id", "123456")
        val currentChannel = Props()
        currentChannel.setObject("channel_attrs", channelAttrs)
        val userProps = Props()
        userProps.setObject("7ohmt2sn00hqg", currentChannel)

        val queueId = NinchatPropsParser.getQueueIdFromUserChannels(userProps)
        Assert.assertEquals("123456", queueId)
    }

    @Test
    fun `get_queue_id_from_user_queue_with_no_queue`() {
        val userQueue = Props()
        val queueId = NinchatPropsParser.getQueueIdFromUserQueue(userQueue)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_no_queue_position`() {
        val queueDetails = Props()

        val userQueue = Props()
        userQueue.setObject("123456", queueDetails)
        val queueId = NinchatPropsParser.getQueueIdFromUserQueue(userQueue)
        Assert.assertNull(queueId)
    }

    @Test
    fun `queue_position_should_be_-1_when_provided_queue_not_found`() {
        val queueDetails = Props()
        val userQueue = Props()
        userQueue.setObject("123456", queueDetails)
        val queueId = NinchatPropsParser.getQueuePositionByQueueId(userQueue, "wrongQueueId")
        Assert.assertEquals(-1, queueId)
    }

    @Test
    fun `queue_position_should_be_0_when_queue_position_not_provided_but_queue_id_found`() {
        val queueDetails = Props()
        val userQueue = Props()
        userQueue.setObject("123456", queueDetails)
        val queueId = NinchatPropsParser.getQueuePositionByQueueId(userQueue, "123456")
        Assert.assertEquals(0, queueId)
    }

    @Test
    fun `get_audience_metadata_from_props_as_string`() {
        val audienceMetadata = Props()
        val stringArr = Strings()
        stringArr.append("1")
        stringArr.append("2")
        stringArr.append("3")
        val jsonStr = JSON("""{"data":{"base":"test-base","currency":"EU","amount":99.87}}""")
        val simpleProps = Props()
        simpleProps.setString("sub-i", "ii")
        simpleProps.setString("sub-j", "jj")

        audienceMetadata.setFloat("foo", 3.14159)
        audienceMetadata.setString("bar", "asdf")
        audienceMetadata.setStringArray("baz", stringArr)
        audienceMetadata.setInt("kaz", 1)
        audienceMetadata.setBool("taz", true)
        audienceMetadata.setJSON("uzz", jsonStr)
        audienceMetadata.setObject("qux", simpleProps)

        val retval = NinchatPropsParser.getAudienceMetadata(audienceMetadata)
        Assert.assertNotNull(retval)
        Assert.assertEquals("""{"bar":"asdf","baz":["1","2","3"],"foo":3.14159,"kaz":1,"qux":{"sub-i":"ii","sub-j":"jj"},"taz":true,"uzz":{"data":{"base":"test-base","currency":"EU","amount":99.87}}}""", retval)
    }

    @Test
    fun `parse_audience_metadata_from_a_valid_string_representation`() {
        val audienceMetadata = Props()
        val stringArr = Strings()
        stringArr.append("1")
        stringArr.append("2")
        stringArr.append("3")
        val jsonStr = JSON("""{"data":{"base":"test-base","currency":"EU","amount":99.87}}""")
        val simpleProps = Props()
        simpleProps.setString("sub-i", "ii")
        simpleProps.setString("sub-j", "jj")

        audienceMetadata.setFloat("foo", 3.14159)
        audienceMetadata.setString("bar", "asdf")
        audienceMetadata.setStringArray("baz", stringArr)
        audienceMetadata.setInt("kaz", 1)
        audienceMetadata.setBool("taz", true)
        audienceMetadata.setJSON("uzz", jsonStr)
        audienceMetadata.setObject("qux", simpleProps)


        val audienceMetadataStr = """{"bar":"asdf","baz":["1","2","3"],"foo":3.14159,"kaz":1,"qux":{"sub-i":"ii","sub-j":"jj"},"taz":true,"uzz":{"data":{"base":"test-base","currency":"EU","amount":99.87}}}"""
        val retval = NinchatPropsParser.toAudienceMetadata(audienceMetadataStr)
        Assert.assertEquals(3.14159, retval?.getFloat("foo"))
        Assert.assertEquals("asdf", retval?.getString("bar"))
        Assert.assertEquals(stringArr, retval?.getStringArray("baz"))
        Assert.assertEquals(1L, retval?.getInt("kaz"))
        Assert.assertEquals(true, retval?.getBool("taz"))
        Assert.assertNotNull(retval?.getObject("uzz"))
        Assert.assertNotNull(retval?.getObject("qux"))
    }

    @Test
    fun `get_audience_metadata_from_empty_props_as_string`() {
        val audienceMetadata = Props()
        val retval = NinchatPropsParser.getAudienceMetadata(audienceMetadata)
        Assert.assertEquals("""{}""", retval)
    }

    @Test
    fun `get_audience_metadata_from_null_props_as_string`() {
        val retval = NinchatPropsParser.getAudienceMetadata(null)
        Assert.assertEquals(null, retval)
    }

    @Test
    fun `parse_audience_metadata_from_a_valid_empty_json_string_representation`() {
        val retval = NinchatPropsParser.toAudienceMetadata("""{}""")
        Assert.assertNotNull(retval)
    }

    @Test
    fun `parse_audience_metadata_from_an_invalid_json_string_representation`() {
        try {
            val retval = NinchatPropsParser.toAudienceMetadata("""{""")
            Assert.assertNull(retval)
        } catch (e: Exception) {
            Assert.assertNotNull(e)
            Assert.assertEquals("unexpected end of JSON input", e.message)
        }
    }

}