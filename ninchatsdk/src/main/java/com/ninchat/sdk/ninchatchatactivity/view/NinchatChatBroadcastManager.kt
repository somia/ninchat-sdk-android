package com.ninchat.sdk.ninchatchatactivity.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.activities.NinchatChatActivity
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.networkdispatchers.NinchatPartChannel
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.launch

class NinchatChatBroadcastManager(
    val ninchatChatActivity: NinchatChatActivity,
    val onChannelClosed: () -> Unit,
    val onTransfer: (intent: Intent) -> Unit
) {
    private val channelClosedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Broadcast.CHANNEL_CLOSED == intent.action) {
                NinchatSessionManager.getInstance()
                    ?.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                        override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                            adapter.close(ninchatChatActivity)
                        }
                    })
                onChannelClosed()
            }
        }
    }

    private val transferReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Broadcast.AUDIENCE_ENQUEUED == intent.action) {
                NinchatSessionManager.getInstance()?.let {
                    NinchatScopeHandler.getIOScope().launch {
                        NinchatPartChannel.execute(
                            currentSession = it.session,
                            channelId = it.ninchatState?.channelId,
                        )
                    }
                }
                onTransfer(intent)
            }
        }
    }

    fun register(localBroadcastManager: LocalBroadcastManager) {
        localBroadcastManager.registerReceiver(channelClosedReceiver,
            IntentFilter(Broadcast.CHANNEL_CLOSED)
        )
        localBroadcastManager.registerReceiver(transferReceiver,
            IntentFilter(Broadcast.AUDIENCE_ENQUEUED)
        )
    }

    fun unregister(localBroadcastManager: LocalBroadcastManager) {
        listOf(channelClosedReceiver, transferReceiver).forEach {
            localBroadcastManager.unregisterReceiver(it)
        }
    }
}