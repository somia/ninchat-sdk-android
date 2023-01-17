package com.ninchat.sdk.helper.session

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.sdk.NinchatSDKEventListener
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.events.OnSubmitPreAudienceQuestionnaireAnswers
import com.ninchat.sdk.helper.message.NinchatMessageService
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser
import com.ninchat.sdk.helper.propsparser.getSafe
import com.ninchat.sdk.helper.siteconfigparser.NinchatSiteConfig
import com.ninchat.sdk.models.NinchatSessionCredentials
import com.ninchat.sdk.networkdispatchers.NinchatDescribeRealmQueues
import com.ninchat.sdk.states.NinchatState
import com.ninchat.sdk.utils.misc.Broadcast
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
        ninchatState.userId = params.getSafe<String>("user_id")
        ninchatState.userChannels = params.getSafe<Props>("user_channels")
        ninchatState.userQueues = params.getSafe<Props>("user_queues")
        ninchatState.queueId =
            if (ninchatState.queueId == null && !ninchatSiteConfig.getAudienceAutoQueue()
                    .isNullOrBlank()
            ) {
                ninchatSiteConfig.getAudienceAutoQueue()
            } else ninchatState.queueId

        ninchatState.currentSessionState =
            ninchatState.currentSessionState or (1 shl Misc.NEW_SESSION)
        if (NinchatPropsParser.hasUserChannel(ninchatState.userChannels)) {
            ninchatState.currentSessionState =
                ninchatState.currentSessionState or (1 shl Misc.HAS_CHANNEL)
            ninchatState.queueId =
                NinchatPropsParser.getQueueIdFromUserChannels(ninchatState.userChannels)
                    ?: ninchatState.queueId
        }
        if (NinchatPropsParser.hasUserQueues(ninchatState.userQueues)) {
            ninchatState.currentSessionState =
                ninchatState.currentSessionState or (1 shl Misc.IN_QUEUE)
            ninchatState.queueId =
                NinchatPropsParser.getQueueIdFromUserQueue(ninchatState.userQueues)
                    ?: ninchatState.queueId
        }
        ninchatState.sessionCredentials = NinchatSessionCredentials(
            params.getSafe<String>("user_id", null),
            ninchatState.sessionCredentials?.userAuth ?: params.getSafe<String>("user_auth"),
            params.getSafe<String>("session_id")
        )
    }

    fun onNewSession(
        session: Session,
        ninchatSiteConfig: NinchatSiteConfig,
        listener: NinchatSDKEventListener?
    ) {
        currentSession = session
        session.setOnClose { Log.v(TAG, "onClose") }
        session.setOnConnState { state -> Log.v(TAG, "onConnState: $state") }
        session.setOnLog { msg -> Log.v(TAG, "onLog: $msg") }

        session.setOnSessionEvent { params: Props ->
            val event = params.getString("event")
            val errorType = params.getSafe<String>("error_type")
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
                    if (errorType == "user_not_found") {
                        dispose()
                    }
                    Handler(Looper.getMainLooper()).post {
                        listener?.onSessionInitFailed()
                    }
                }
            }
        }
        session.setOnEvent { params: Props, payload: Payload, lastReply: Boolean ->
            Log.v(TAG, "onEvent: ${params.string()}, ${payload.string()}, $lastReply")
            val event = params.getSafe<String>("event")
            val currentActionId = params.getSafe<Long>("action_id") ?: -1
            when (event) {
                "realm_queues_found" -> NinchatSessionManagerHelper.parseQueues(params)
                "queue_found", "queue_updated" -> NinchatSessionManager.getInstance()
                    .queueUpdated(params)
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
                "jitsi_discovered" -> NinchatSessionManagerHelper.jitsiDiscovered(params)
                "message_received" -> NinchatMessageService.handleIncomingMessage(params, payload)
                "ice_begun" -> NinchatSessionManager.getInstance().iceBegun(params)
                "file_found" -> NinchatSessionManagerHelper.fileFound(params)
                "channel_member_updated", "user_updated" -> NinchatSessionManagerHelper.memberUpdated(
                    params
                )
                "audience_registered" -> EventBus.getDefault()
                    .post(OnSubmitPreAudienceQuestionnaireAnswers(false))
                "error" -> {
                    when (NinchatSessionManager.getInstance()?.ninchatState?.actionId) {
                        currentActionId -> {
                            EventBus.getDefault().post(OnSubmitPreAudienceQuestionnaireAnswers(true))
                        }
                    }
                }
            }
            Handler(Looper.getMainLooper()).post {
                listener?.onEvent(params, payload)
            }
        }
    }

    fun isResumedSession(): Boolean {
        return isInQueue() || hasChannel()
    }

    fun isInQueue(): Boolean {
        return (ninchatState.currentSessionState and (1 shl Misc.IN_QUEUE)) != 0
    }

    fun hasChannel(): Boolean {
        return (ninchatState.currentSessionState and (1 shl Misc.HAS_CHANNEL)) != 0
    }

    fun supportVideos(): Boolean {
        return NinchatSessionManager.getInstance()
            ?.getQueue(ninchatState.queueId)?.supportVideos == true
    }

    fun supportFiles(): Boolean {
        return NinchatSessionManager.getInstance()
            ?.getQueue(ninchatState.queueId)?.supportFiles == true
    }

    fun isGroupVideo(): Boolean {
        return NinchatSessionManager.getInstance()
            ?.getQueue(ninchatState.queueId)?.isGroup == true
    }

    fun dispose() {
        currentSession?.close()
        NinchatSessionManager.getInstance()?.context?.let {
            LocalBroadcastManager.getInstance(it)
                .sendBroadcast(Intent(Broadcast.CLOSE_NINCHAT_ACTIVITY))
        }

    }

}