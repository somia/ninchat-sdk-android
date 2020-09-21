package com.ninchat.sdk.session

import android.util.Log
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatSessionHolder() {
    companion object {
        val TAG = "NinchatSessionHolder"
    }

    var session: Session? = null
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
            val event = params.getString("event")
            when (event) {
                "realm_queues_found" -> {
                }
                "queue_found", "queue_updated" -> {
                }
                "audience_enqueued" -> {
                }
                "channel_joined" -> {
                }
                "channel_found" -> {
                }
                "channel_updated" -> {
                }
                "message_received" -> {
                }
                "ice_begun" -> {
                }
                "file_found" -> {
                }
                "channel_member_updated", "user_updated" -> {
                }
                "audience_registered" -> {
                }
                "error" -> {
                }
                else -> {

                }
            }
        }
    }
}