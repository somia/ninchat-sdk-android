package com.ninchat.sdk.utils.writingindicator

import android.util.Log
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.networkdispatchers.NinchatUpdateMember
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WritingIndicator {
    private val inactiveTimeoutInMs = 5L // 30 seconds
    private var wasWriting = false
    val disposable = Single
        .just(1)
        .subscribe { _ ->
            notifyBackend(isWriting = false)
        }

    @JvmName("updateLastWritingTime")
    fun updateLastWritingTime(messageLength: Int) {
        val isWriting = (messageLength > 0)
        notifyBackend(isWriting = isWriting)
        Observable.just(1)
            .throttleWithTimeout(inactiveTimeoutInMs, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                notifyBackend(isWriting = false)
            }
    }

    private fun notifyBackend(isWriting: Boolean) {
        // if there is no state change and it is not dirty
        if (isWriting == wasWriting) return
        this.wasWriting = isWriting
        NinchatSessionManager.getInstance()?.let { sessionManager ->
            NinchatScopeHandler.getIOScope().launch(CoroutineExceptionHandler(handler = { _, e ->
                Log.d("WritingIndicator", e.message ?: "")
            })) {
                NinchatUpdateMember.execute(
                    currentSession = sessionManager.session,
                    channelId = sessionManager.ninchatState.channelId,
                    userId = sessionManager.ninchatState.userId,
                    isWriting = isWriting,
                );
            }
        }
    }
}