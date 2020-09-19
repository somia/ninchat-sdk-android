package com.ninchat.sdk.helper.propsparser

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ninchat.client.Props
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


}