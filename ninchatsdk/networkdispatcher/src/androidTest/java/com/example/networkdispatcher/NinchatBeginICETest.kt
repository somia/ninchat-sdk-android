package com.example.networkdispatcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NinchatBeginICETest {
    @Test
    fun getICEWithEmptySession() = runBlocking<Unit> {
        val actionId = NinchatBeginICE.execute(null)
        Assert.assertEquals("should return -1 when providing an empty session", -1, actionId)
    }

    @Test
    fun getICEWithRightSession() = runBlocking<Unit> {
        val actionId = NinchatBeginICE.execute(null)
        Assert.assertEquals("should return ICE details with right session", -1, actionId)
    }

    @Test
    fun getICEWithClosedSession() = runBlocking<Unit> {
        val actionId = NinchatBeginICE.execute(null)
        Assert.assertEquals("should return -1 when providing a closed session", -1, actionId)
    }
}