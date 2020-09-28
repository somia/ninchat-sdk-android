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
import com.ninchat.sdk.helper.message.NinchatMessageService
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser
import com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireTypeUtil
import com.ninchat.sdk.helper.sessionmanager.NinchatSessionManagerHelper
import com.ninchat.sdk.helper.siteconfigparser.NinchatSiteConfig
import com.ninchat.sdk.models.NinchatSessionCredentials
import com.ninchat.sdk.networkdispatchers.NinchatDescribeRealmQueues
import com.ninchat.sdk.states.NinchatState
import com.ninchat.sdk.utils.misc.Misc
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class NinchatSessionHolder(ninchatState: NinchatState) {
    private val ninchatState: NinchatState = ninchatState
    var currentSession: Session? = null

    companion object {
        val TAG = "NinchatSessionHolder"
    }

    private fun handleSessionCreate(params: Props, ninchatSiteConfig: NinchatSiteConfig) {
        ninchatState.userId = params.getString("user_id")
        ninchatState.userChannels = params.getObject("user_channels")
        ninchatState.userQueues = params.getObject("user_queues")
        ninchatState.queueId = if (ninchatState.queueId == null && !ninchatSiteConfig.getAudienceAutoQueue().isNullOrBlank()) {
            ninchatSiteConfig.getAudienceAutoQueue()
        } else ninchatState.queueId
        ninchatState.currentSessionState = ninchatState.currentSessionState or (1 shl Misc.NEW_SESSION)
        if (NinchatPropsParser.hasUserChannel(ninchatState.userChannels)) {
            ninchatState.currentSessionState = ninchatState.currentSessionState or (1 shl Misc.HAS_CHANNEL)
            ninchatState.queueId = NinchatPropsParser.getQueueIdFromUserChannels(ninchatState.userChannels)
                    ?: ninchatState.queueId
        }
        if (NinchatPropsParser.hasUserQueues(ninchatState.userQueues)) {
            ninchatState.currentSessionState = ninchatState.currentSessionState or (1 shl Misc.IN_QUEUE)
            ninchatState.queueId = NinchatPropsParser.getQueueIdFromUserQueue(ninchatState.userQueues)
                    ?: ninchatState.queueId
        }
        ninchatState.sessionCredentials = NinchatSessionCredentials(
                params.getString("user_id"),
                ninchatState.sessionCredentials?.userAuth ?: params.getString("user_auth"),
                params.getString("session_id")
        )
    }

    fun onNewSession(session: Session, ninchatSiteConfig: NinchatSiteConfig, listener: NinchatSDKEventListener?) {
        currentSession = session
        session.setOnClose { Log.v(TAG, "onClose") }
        session.setOnConnState { state -> Log.v(TAG, "onConnState: $state") }
        session.setOnLog { msg -> Log.v(TAG, "onLog: $msg") }

        session.setOnSessionEvent { params: Props ->
            val event = params.getString("event")
            when (event) {
                "session_created" -> {
                    handleSessionCreate(params, ninchatSiteConfig)
                    listener?.onSessionInitiated(ninchatState.sessionCredentials)
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
                "realm_queues_found" -> NinchatSessionManagerHelper.parseQueues(params)
                "queue_found", "queue_updated" -> NinchatSessionManager.getInstance().queueUpdated(params)
                "audience_enqueued" -> NinchatSessionManager.getInstance().audienceEnqueued(params)
                "channel_joined" -> NinchatSessionManagerHelper.channelJoined(params)
                "channel_found" -> {
                    if (ninchatState.actionId == currentActionId) {
                        NinchatSessionManagerHelper.channelJoined(params)
                    } else {
                        NinchatSessionManagerHelper.channelUpdated(params)
                    }
                }
                "channel_updated" -> NinchatSessionManagerHelper.channelUpdated(params)
                "message_received" -> NinchatMessageService.handleIncomingMessage(params, payload)
                "ice_begun" -> NinchatSessionManager.getInstance().iceBegun(params)
                "file_found" -> NinchatSessionManagerHelper.fileFound(params)
                "channel_member_updated", "user_updated" -> NinchatSessionManagerHelper.memberUpdated(params)
                "audience_registered" -> EventBus.getDefault().post(OnAudienceRegistered(false))
                "error" -> EventBus.getDefault().post(OnAudienceRegistered(true))
            }
        }
    }

    fun isResumedSession(): Boolean {
        return isInQueue() || hasChannel()
    }

    fun isInQueue(): Boolean {
        return (ninchatState.currentSessionState and (1 shl NinchatQuestionnaireTypeUtil.IN_QUEUE)) != 0
    }

    fun hasChannel(): Boolean {
        return (ninchatState.currentSessionState and (1 shl NinchatQuestionnaireTypeUtil.HAS_CHANNEL)) != 0
    }

    fun dispose() {
        // todo ( pallab )
    }

}