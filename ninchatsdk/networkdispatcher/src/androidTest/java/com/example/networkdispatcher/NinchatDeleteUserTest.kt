package com.example.networkdispatcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

// all test currently now should only delete guest user
// todo complete test
@RunWith(AndroidJUnit4::class)
class NinchatDeleteUserTest {
    @Test
    fun deleteUser_with_nullSession() = runBlocking {
        val actionId = NinchatBeginICE.execute(null)
        Assert.assertEquals("should return -1 when providing an empty session", -1, actionId)
    }
    fun deleteUser_with_session() {
        assertEquals(4, 2 + 2)
    }
}