package com.ninchat.sdk.ninchatqueuelist.presenter

import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import org.junit.Assert
import org.junit.Test

class NinchatQueueListPresenterTest {
    @Test
    fun `create_queue_list_with_empty_queue`() {
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf())
        Assert.assertEquals(0, ninchatQueueListPresenter.size())
    }

    @Test
    fun `create_queue_list_with_non_empty_queue`() {
        val q1 = NinchatQueue(id = "1234", name = "test-q1")
        val q2 = NinchatQueue(id = "1235", name = "test-q2")
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(q1, q2))
        Assert.assertEquals(2, ninchatQueueListPresenter.size())
    }

    @Test
    fun `add new queue to queue list`() {
        val q1 = NinchatQueue(id = "1234", name = "test-q1")
        val q2 = NinchatQueue(id = "1235", name = "test-q2")
        var size = 0
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(q1))
        ninchatQueueListPresenter.add(q2) { queueSize -> size = queueSize }
        Assert.assertEquals(2, size)
        Assert.assertEquals(2, ninchatQueueListPresenter.size())
    }

    @Test
    fun `by default queue should be closed`() {
        val currentQueue = NinchatQueue(id = "1234", name = "test-q1")
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(currentQueue))
        // index inside bound
        Assert.assertEquals(false, ninchatQueueListPresenter.isClosedQueue(0))
    }

    @Test
    fun `if queue index is more than size or not found then queue should also be closed`() {
        val currentQueue = NinchatQueue(id = "1234", name = "test-q1")
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(currentQueue))
        Assert.assertEquals(false, ninchatQueueListPresenter.isClosedQueue(1023456789))
    }

    @Test
    fun `queue should be closed`() {
        val currentQueue = NinchatQueue(id = "1234", name = "test-q1")
        currentQueue.isClosed = true
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(currentQueue))
        Assert.assertEquals(true, ninchatQueueListPresenter.isClosedQueue(0))
    }

    @Test
    fun `queue id should match`() {
        val currentQueue = NinchatQueue(id = "1234", name = "test-q1")
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(currentQueue))
        Assert.assertEquals("1234", ninchatQueueListPresenter.getQueueId(0))
    }

    @Test
    fun `queue id should be empty string when index is greater than size`() {
        val currentQueue = NinchatQueue(id = "1234", name = "test-q1")
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf(currentQueue))
        Assert.assertEquals("", ninchatQueueListPresenter.getQueueId(1023456789))
    }

    @Test
    fun `should return null as ninchat queue if index is greater than size`() {
        val ninchatQueueListPresenter = NinchatQueueListPresenter(listOf())
        Assert.assertNull(ninchatQueueListPresenter.get(1023456789))
    }
}