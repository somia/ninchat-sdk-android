package com.ninchat.sdk.adapters

import android.app.Activity
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import com.ninchat.sdk.ninchatqueuelist.view.NinchatQueueListAdapter
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

class NinchatQueueListAdapterTest {

    @Test
    fun `should initiate NinchatQueueListAdapter with activity and ninchat queue list`() {
        val activity = mock(Activity::class.java)
        val queue = mock(NinchatQueue::class.java)
        NinchatQueueListAdapter(activity, mutableListOf(queue, queue, queue, queue, queue))
    }

    @Test
    fun `should return 0 when queue is null`() {
        val activity = mock(Activity::class.java)
        val ninchatActivity = NinchatQueueListAdapter(activity, mutableListOf())
        Assert.assertEquals(0, ninchatActivity.itemCount)
    }

    @Test
    fun `should return 5 for queue size 5`() {
        val activity = mock(Activity::class.java)
        val queue = mock(NinchatQueue::class.java)
        val ninchatActivity = NinchatQueueListAdapter(activity, mutableListOf(queue, queue, queue, queue, queue))
        Assert.assertEquals(5, ninchatActivity.itemCount)
    }

    @Test
    fun `should remove all element from queue and call notifyDataSetChanged`() {
        val activity = mock(Activity::class.java)
        val queue = mock(NinchatQueue::class.java)
        val ninchatActivity = NinchatQueueListAdapter(activity, mutableListOf(queue, queue, queue, queue, queue))
        val mockedNinchatActivity = spy(ninchatActivity)
        doNothing().`when`(mockedNinchatActivity).notifyDataSetChanged()
//        mockedNinchatActivity.clearData()
        Assert.assertEquals(0, mockedNinchatActivity.itemCount)
    }

    @Test
    fun `should add a new queue item in queue list and call notifyItemRangeInserted`() {
        val activity = mock(Activity::class.java)
        val queue = mock(NinchatQueue::class.java)
        val ninchatActivity = NinchatQueueListAdapter(activity, mutableListOf(queue, queue, queue, queue, queue))
        val mockedNinchatActivity = spy(ninchatActivity)
        doNothing().`when`(mockedNinchatActivity).notifyItemRangeInserted(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt())

        val previousSize = ninchatActivity.itemCount
//        mockedNinchatActivity.addData(queue)
        Assert.assertEquals(previousSize + 1, mockedNinchatActivity.itemCount)
    }
}