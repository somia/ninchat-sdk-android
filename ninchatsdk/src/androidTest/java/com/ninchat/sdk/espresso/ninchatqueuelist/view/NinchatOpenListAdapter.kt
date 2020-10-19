package com.ninchat.sdk.espresso.ninchatqueuelist.view

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.R
import com.ninchat.sdk.ninchatactivity.presenter.NinchatActivityPresenter
import com.ninchat.sdk.ninchatactivity.view.NinchatActivity
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class NinchatOpenListAdapter {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    val configurationKey = appContext.getString(R.string.ninchat_configuration_key)

    lateinit var activityScenario: ActivityScenario<NinchatActivity>

    @After
    fun dispose() {
        try {
            activityScenario.close()
        } catch (err: Exception) {
            println(err)
        }
    }

    fun atPosition(position: Int, itemMatcher: Matcher<View?>): Matcher<View?>? {
        checkNotNull(itemMatcher)
        return object : BoundedMatcher<View?, RecyclerView>(RecyclerView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has item at position $position: ")
                itemMatcher.describeTo(description)
            }

            override fun matchesSafely(view: RecyclerView): Boolean {
                val viewHolder = view.findViewHolderForAdapterPosition(position)
                        ?: // has no item on such position
                        return false
                return itemMatcher.matches(viewHolder.itemView)
            }
        }
    }

    @Test
    fun queue_list_adapter_with_empty_queue_list() {
        NinchatSession.Builder(appContext, configurationKey).create()
        val intent = NinchatActivityPresenter.getLaunchIntent(appContext, null)?.run {
            putExtra("isDebug", true)
        }
        val q1 = NinchatQueue("1234", "test-queue-1").also {
            it.position = 1
            it.isClosed = false
        }
        val q2 = NinchatQueue("1235", "test-queue-2").also {
            it.position = 1
            it.isClosed = true
        }

        val q3 = NinchatQueue("1236", "test-queue-3").also {
            it.position = 1
            it.isClosed = false
        }

        val q4 = NinchatQueue("1237", "test-queue-4").also {
            it.position = 1
            it.isClosed = false
        }
        NinchatSessionManager.getInstance().ninchatState.queues = arrayListOf()
        activityScenario = ActivityScenario.launch(intent)
        Thread.sleep(60000)
    }
}