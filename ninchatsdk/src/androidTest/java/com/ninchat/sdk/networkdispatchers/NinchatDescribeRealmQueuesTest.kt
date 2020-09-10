package com.ninchat.sdk.networkdispatchers

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.junit.Assert
import org.junit.Test

@RunWith(AndroidJUnit4::class)
class NinchatDescribeRealmQueuesTest {
    @Test
    fun describe_realm_queue_with_empty_session() = runBlocking {
        Assert.assertEquals("should return -1 for null session", -1)
    }

    @Test
    fun describe_realm_queue_with_no_realmId() = runBlocking {
    }

    @Test
    fun describe_realm_queue_with_wrong_realmId() = runBlocking {
    }

    @Test
    fun describe_realm_queue_with_no_audience_queues() = runBlocking {
    }

    @Test
    fun describe_realm_queue_with_audience_queues() = runBlocking {
    }
}