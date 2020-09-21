package com.ninchat.sdk.session

import android.util.Log
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.events.OnAudienceRegistered
import org.greenrobot.eventbus.EventBus

class NinchatSessionHolder() {
    companion object {
        val TAG = "NinchatSessionHolder"
    }

    fun onNewSession(session: Session) {
        session.setOnClose { Log.v(TAG, "onClose") }
        session.setOnConnState { state -> Log.v(TAG, "onConnState: $state") }
        session.setOnLog { msg -> Log.v(TAG, "onLog: $msg") }

        session.setOnSessionEvent { params: Props ->
            val event = params.getString("event")
            when (event) {
                "session_created" -> {

                }
                "user_deleted" -> {

                }
                else -> {

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