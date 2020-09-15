package com.ninchat.sdk.utils.threadutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object NinchatScopeHandler {
    val scope = CoroutineScope(Job() + Dispatchers.IO)
    @JvmStatic
    fun getIOScope(): CoroutineScope {
        return scope
    }
}