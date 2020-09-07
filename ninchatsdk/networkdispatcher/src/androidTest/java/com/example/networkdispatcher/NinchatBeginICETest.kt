package com.example.networkdispatcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NinchatBeginICETest {
    @Test
    fun ninchatBeginICE() = runBlocking<Unit> {
        val actionId = NinchatBeginICE.execute(null)
        Assert.assertEquals("should return -1 when session is null", -1, actionId)
    }
}