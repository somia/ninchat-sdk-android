package com.ninchat.sdk.utils.threadutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object NinchatScopeHandler {
    @JvmStatic
    fun getIOScope(): CoroutineScope {
        return CoroutineScope(Job() + Dispatchers.IO)
    }
}