package com.ninchat.sdk.ninchatchatactivity.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.networkdispatchers.NinchatPartChannel
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.launch

class NinchatChatBroadcastManager(
    val ninchatChatActivity: NinchatChatActivity,
    val onChannelClosed: () -> Unit,
    val onTransfer: (intent: Intent) -> Unit,
    val onP2PVideoCallInvitation: (intent: Intent) -> Unit,
    val onJitsiDiscovered: (intent: Intent) -> Unit,
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

    private val webrtcP2PCallInvitation = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Broadcast.WEBRTC_MESSAGE == intent.action) {
                onP2PVideoCallInvitation(intent)
            }
        }
    }

    private val jitsiDiscoveredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Broadcast.JITSI_DISCOVERED_MESSAGE == intent.action) {
                onJitsiDiscovered(intent)
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
        localBroadcastManager.registerReceiver(webrtcP2PCallInvitation,
            IntentFilter(Broadcast.WEBRTC_MESSAGE)
        )
        localBroadcastManager.registerReceiver(jitsiDiscoveredReceiver,
            IntentFilter(Broadcast.JITSI_DISCOVERED_MESSAGE)
        )
    }

    fun unregister(localBroadcastManager: LocalBroadcastManager) {
        listOf(channelClosedReceiver, transferReceiver, webrtcP2PCallInvitation, jitsiDiscoveredReceiver).forEach {
            localBroadcastManager.unregisterReceiver(it)
        }
    }
}