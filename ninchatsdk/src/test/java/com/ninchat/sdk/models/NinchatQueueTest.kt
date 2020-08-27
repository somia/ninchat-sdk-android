package com.ninchat.sdk.models

import com.ninchat.sdk.NinchatSessionManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class NinchatQueueTest {
    lateinit var queueId: String
    lateinit var queueName: String

    @Before
    fun setUp() {
        queueId = "test-queue-id"
        queueName = "test-queue-name"
    }

    @Test
    fun `should initial ninchat queue with given id and name`() {
        val ninchatQueue = NinchatQueue(queueId, queueName)
        Assert.assertEquals(queueId, ninchatQueue.id)
        Assert.assertEquals(queueName, ninchatQueue.name)
    }

    @Test
    fun `queue position should initialized with default Long MAX value`() {
        val ninchatQueue = NinchatQueue(queueId, queueName)
        Assert.assertEquals(Long.MAX_VALUE, ninchatQueue.position)
    }

    @Test
    fun `queue should initialized as open`() {
        val ninchatQueue = NinchatQueue(queueId, queueName)
        Assert.assertEquals(false, ninchatQueue.isClosed)
    }

    @Test
    fun `should be able to set queue position to 10`() {
        val ninchatQueue = NinchatQueue(queueId, queueName)
        ninchatQueue.setPosition(10L)
        Assert.assertEquals(10L, ninchatQueue.position)
    }

    @Test
    fun `should be able to set a queue to closed`() {
        val ninchatQueue = NinchatQueue(queueId, queueName)
        ninchatQueue.setClosed(true)
        Assert.assertEquals(true, ninchatQueue.isClosed)
    }

    @Test
    fun `should be able to set a queue to open`() {
        val ninchatQueue = NinchatQueue(queueId, queueName)
        ninchatQueue.setClosed(false)
        Assert.assertEquals(false, ninchatQueue.isClosed)
    }

    @Test
    fun `should be able to check whether a queue is closed or open using isClosed`() {
        val ninchatQueue = NinchatQueue(queueId, queueName)
        ninchatQueue.setClosed(false)
        Assert.assertEquals(false, ninchatQueue.isClosed)
        ninchatQueue.setClosed(true)
        Assert.assertEquals(true, ninchatQueue.isClosed)
    }

}