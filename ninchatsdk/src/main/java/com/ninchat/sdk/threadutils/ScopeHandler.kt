package com.ninchat.sdk.threadutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object ScopeHandler {
    val scope = CoroutineScope(Job() + Dispatchers.IO)
    @JvmStatic
    fun getIOScope(): CoroutineScope {
        return scope
    }
}