package com.ninchat.sdk.helper.propsparser

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ninchat.client.Props
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert

@RunWith(AndroidJUnit4::class)
class PropsParserTest {

    @Test
    fun `get_queue_id_from_user_channel_with_null_props`() {
        val queueId = PropsParser.getQueueIdFromUserChannels(null)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_channel_with_no_channel`() {
        val userProps = Props()
        val queueId = PropsParser.getQueueIdFromUserChannels(userProps)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_channel_with_no_channel_attributes`() {
        val currentChannel = Props()
        val userProps = Props()
        userProps.setObject("7ohmt2sn00hqg", currentChannel)
        val queueId = PropsParser.getQueueIdFromUserChannels(userProps)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_channel_with_no_queue_id`() {
        val channelAttrs = Props()
        val currentChannel = Props()
        currentChannel.setObject("channel_attrs", channelAttrs)
        val userProps = Props()
        userProps.setObject("7ohmt2sn00hqg", currentChannel)
        val queueId = PropsParser.getQueueIdFromUserChannels(userProps)
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

        val queueId = PropsParser.getQueueIdFromUserChannels(userProps)
        Assert.assertEquals("123456", queueId)
    }

    @Test
    fun `get_queue_id_from_user_queue_with_no_queue`() {
        val userQueue = Props()
        val queueId = PropsParser.getQueueIdFromUserQueue(userQueue)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_no_queue_position`() {
        val queueDetails = Props()

        val userQueue = Props()
        userQueue.setObject("123456", queueDetails)
        val queueId = PropsParser.getQueueIdFromUserQueue(userQueue)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_queue_position_zero`() {
        val queueDetails = Props()
        queueDetails.setInt("queue_position", 0L)
        val userQueue = Props()
        userQueue.setObject("123456", queueDetails)
        val queueId = PropsParser.getQueueIdFromUserQueue(userQueue)
        Assert.assertNull(queueId)
    }

    @Test
    fun `get_queue_id_from_user_queue_with_non_zero_queue_position`() {
        // TODO: 16.9.2020 (pallab) throw exception test and fix it
        val queuePosition: Long = 1
        val queueDetails = Props()
        queueDetails.setInt("queue_position", queuePosition)
        val userQueue = Props()
        userQueue.setObject("123456", queueDetails)
        val queueId = PropsParser.getQueueIdFromUserQueue(userQueue)
        Assert.assertEquals(1L, queueId)
    }
}