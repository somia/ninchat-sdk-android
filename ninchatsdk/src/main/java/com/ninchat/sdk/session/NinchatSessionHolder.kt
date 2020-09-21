package com.ninchat.sdk.session

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.sdk.NinchatSDKEventListener
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.events.OnAudienceRegistered
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser
import com.ninchat.sdk.helper.siteconfigparser.NinchatSiteConfig
import com.ninchat.sdk.models.NinchatSessionCredentials
import com.ninchat.sdk.networkdispatchers.NinchatDescribeRealmQueues
import com.ninchat.sdk.states.NinchatState
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class NinchatSessionHolder() {
    companion object {
        val TAG = "NinchatSessionHolder"
    }

    private fun handleSessionCreate(params: Props, ninchatSiteConfig: NinchatSiteConfig) {
        NinchatState.userId = params.getString("user_id")
        NinchatState.userChannels = params.getObject("user_channels")
        NinchatState.userQueues = params.getObject("user_queues")
        NinchatState.queueId = if (NinchatState.queueId == null && !ninchatSiteConfig.getAudienceAutoQueue().isNullOrBlank()) {
            ninchatSiteConfig.getAudienceAutoQueue()
        } else NinchatState.queueId
        NinchatState.currentSessionState = NinchatState.currentSessionState or (Misc.NEW_SESSION shl 1)
        if (NinchatPropsParser.hasUserChannel(NinchatState.userChannels)) {
            NinchatState.currentSessionState = NinchatState.currentSessionState or (Misc.HAS_CHANNEL shl 1)
            NinchatState.queueId = NinchatPropsParser.getQueueIdFromUserChannels(NinchatState.userChannels)
                    ?: NinchatState.queueId
        }
        if (NinchatPropsParser.hasUserQueues(NinchatState.userQueues)) {
            NinchatState.currentSessionState = NinchatState.currentSessionState or (Misc.IN_QUEUE shl 1)
            NinchatState.queueId = NinchatPropsParser.getQueueIdFromUserQueue(NinchatState.userQueues)
                    ?: NinchatState.queueId
        }
        NinchatState.sessionCredentials = NinchatSessionCredentials(
                params.getString("user_id"),
                NinchatState.sessionCredentials?.userAuth ?: params.getString("user_auth"),
                params.getString("session_id")
        )
    }

    fun onNewSession(session: Session, ninchatSiteConfig: NinchatSiteConfig, listener: NinchatSDKEventListener?) {
        session.setOnClose { Log.v(TAG, "onClose") }
        session.setOnConnState { state -> Log.v(TAG, "onConnState: $state") }
        session.setOnLog { msg -> Log.v(TAG, "onLog: $msg") }

        session.setOnSessionEvent { params: Props ->
            val event = params.getString("event")
            when (event) {
                "session_created" -> {
                    handleSessionCreate(params, ninchatSiteConfig)
                    listener?.onSessionInitiated(NinchatState.sessionCredentials)
                    NinchatScopeHandler.getIOScope().launch {
                        NinchatDescribeRealmQueues.execute(
                                currentSession = session,
                                realmId = ninchatSiteConfig.getRealmId(),
                                audienceQueues = ninchatSiteConfig.getAudienceQueues()
                        )
                    }
                    Handler(Looper.getMainLooper()).post {
                        listener?.onSessionEvent(params)
                    }
                }
                "user_deleted" -> {
                    // ignore
                }
                else -> {
                    listener?.onSessionInitFailed()
                }
            }
        }
        session.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
            Log.v(TAG, "onEvent: ${params.string()}, ${payload.string()}, $lastReply")
            val event = params.getString("event")
            val currentActionId = params.getInt("action_id")
            when (event) {
                "realm_queues_found" -> NinchatSessionManager.getInstance().parseQueues(params)
                "queue_found", "queue_updated" -> NinchatSessionManager.getInstance().queueUpdated(params)
                "audience_enqueued" -> NinchatSessionManager.getInstance().audienceEnqueued(params)
                "channel_joined" -> NinchatSessionManager.getInstance().channelJoined(params)
                "channel_found" -> {
                    if (NinchatSessionManager.getInstance().actionId == currentActionId) {
                        NinchatSessionManager.getInstance().channelJoined(params)
                    } else {
                        NinchatSessionManager.getInstance().channelUpdated(params)
                    }
                }
                "channel_updated" -> NinchatSessionManager.getInstance().channelUpdated(params)
                "message_received" -> NinchatSessionManager.getInstance().messageReceived(params, payload)
                "ice_begun" -> NinchatSessionManager.getInstance().iceBegun(params)
                "file_found" -> NinchatSessionManager.getInstance().fileFound(params)
                "channel_member_updated", "user_updated" -> NinchatSessionManager.getInstance().memberUpdated(params)
                "audience_registered" -> EventBus.getDefault().post(OnAudienceRegistered(false))
                "error" -> EventBus.getDefault().post(OnAudienceRegistered(true))
            }
        }
    }
}