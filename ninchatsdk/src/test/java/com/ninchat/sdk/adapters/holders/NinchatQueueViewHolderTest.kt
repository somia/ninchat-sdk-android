package com.ninchat.sdk.adapters.holders

import android.content.Context
import android.view.View
import android.widget.Button
import com.ninchat.sdk.models.NinchatQueue
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

class NinchatQueueViewHolderTest {
    @Test
    fun `should create a NinchatQueueViewHolder class`() {
        val viewItem = mock(View::class.java)
        NinchatQueueViewHolder(viewItem)
    }

    lateinit var mItemView: View
    @Before
    fun setup() {
        mItemView = mock(View::class.java)
    }

    @Test
    fun `getText should return null when queue is empty`() {
        val mNinchatQueueViewHolder = NinchatQueueViewHolder(mItemView)
        Assert.assertNull(mNinchatQueueViewHolder.getText(null))
    }

    @Test
    fun `set button click listener, and setAlpha when queue is open`() {
        val mQueue = mock(NinchatQueue::class.java)
        `when`(mQueue.isClosed).thenReturn(false)

        val mButton = mock(Button::class.java)
        val viewHolder = NinchatQueueViewHolder(mItemView)
        val mockedViewHolder = spy(viewHolder)
        `when`(mockedViewHolder.getText(ArgumentMatchers.any())).thenReturn("")
        `when`(mockedViewHolder.buttonItem).thenReturn(mButton)
        mockedViewHolder.bind(mQueue, mock(NinchatQueueViewHolder.Callback::class.java))

        verify(mButton).setOnClickListener(ArgumentMatchers.any())
        verify(mButton).alpha = 1f
    }


    @Test
    fun `should not set button click listener, and set button disabled with set alpha when queue is close`() {
        val mQueue = mock(NinchatQueue::class.java)
        `when`(mQueue.isClosed).thenReturn(true)

        val mButton = mock(Button::class.java)
        val viewHolder = NinchatQueueViewHolder(mItemView)
        val mockedViewHolder = spy(viewHolder)
        `when`(mockedViewHolder.getText(ArgumentMatchers.any())).thenReturn("")
        `when`(mockedViewHolder.buttonItem).thenReturn(mButton)
        mockedViewHolder.bind(mQueue, mock(NinchatQueueViewHolder.Callback::class.java))

        verify(mButton).alpha = 0.5f
        verify(mButton).isEnabled = false
    }
}