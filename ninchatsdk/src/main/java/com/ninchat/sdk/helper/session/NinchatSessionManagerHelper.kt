package com.ninchat.sdk.helper.session

import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.client.Objects
import com.ninchat.client.Props
import com.ninchat.client.Strings
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.adapters.NinchatMessageAdapter
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser.Companion.getChannelIdFromUserChannel
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser.Companion.getOpenQueueList
import com.ninchat.sdk.helper.propsparser.getSafe
import com.ninchat.sdk.models.NinchatMessage
import com.ninchat.sdk.models.NinchatWebRTCServerInfo
import com.ninchat.sdk.networkdispatchers.NinchatDescribeChannel
import com.ninchat.sdk.networkdispatchers.NinchatDescribeQueue
import com.ninchat.sdk.networkdispatchers.NinchatRequestAudience
import com.ninchat.sdk.networkdispatchers.NinchatSendMessage
import com.ninchat.sdk.ninchatactivity.presenter.NinchatActivityPresenter
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.NinchatAdapterCallback
import com.ninchat.sdk.utils.misc.Parameter
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler.getIOScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*

class NinchatSessionManagerHelper {
    companion object {
        internal val getServers =
            fun(serverList: Objects?, isTurn: Boolean): ArrayList<NinchatWebRTCServerInfo>? {
                return serverList?.let {
                    val retval = arrayListOf<NinchatWebRTCServerInfo>()
                    for (i in 0 until it.length()) {
                        val stunServerProps = it[i]
                        stunServerProps.getSafe<Strings>("urls")?.let { urls ->
                            for (j in 0 until urls.length()) {
                                if (isTurn) {
                                    val username: String =
                                        stunServerProps.getSafe<String>("username") ?: ""
                                    val credential: String =
                                        stunServerProps.getSafe<String>("credential") ?: ""
                                    retval.add(
                                        NinchatWebRTCServerInfo(
                                            urls.get(j),
                                            username,
                                            credential
                                        )
                                    );
                                } else {
                                    retval.add(NinchatWebRTCServerInfo(urls[j]))
                                }

                            }
                        }
                    }
                    return retval
                }
            }

        /**
         * Try to join queue
         */
        @JvmStatic
        fun mayBeJoinQueue(queueId: String) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { currentSession ->
                currentSession.ninchatState?.queueId = queueId
                if (currentSession.ninchatSessionHolder?.isResumedSession() == true) {
                    currentSession.audienceEnqueued(queueId)
                    if (currentSession.ninchatSessionHolder?.isInQueue() == true) {
                        // already in queue
                        return
                    }
                    if (currentSession.ninchatSessionHolder?.hasChannel() == true) {
                        val currentChannelId =
                            getChannelIdFromUserChannel(currentSession.ninchatState?.userChannels)
                        getIOScope().launch {
                            val actionId = NinchatDescribeChannel.execute(
                                currentSession = currentSession.session,
                                channelId = currentChannelId
                            )
                            currentSession.ninchatState.actionId = actionId
                        }
                        return
                    }
                }
                // if none of above. try to join queue by calling request audience
                getIOScope().launch {
                    NinchatRequestAudience.execute(
                        currentSession = currentSession.session,
                        queueId = queueId,
                        audienceMetadata = currentSession.ninchatState?.audienceMetadata?.get()
                    )
                }

            }
        }

        @JvmStatic
        fun getQueueStatus(queueId: String?): String? {
            val sessionManager = NinchatSessionManager.getInstance()
            return sessionManager?.let { currentSession ->
                val selectedQueue: NinchatQueue? = currentSession.getQueue(queueId)
                val position = selectedQueue?.position
                    ?: NinchatPropsParser.getQueuePositionByQueueId(
                        props = sessionManager.ninchatState.userQueues, queueId = queueId
                            ?: ""
                    )
                val name = selectedQueue?.name
                    ?: NinchatPropsParser.getQueueNameByQueueId(
                        props = sessionManager.ninchatState.userQueues, queueId = queueId
                            ?: ""
                    )
                // if there is no queue position
                if (position == 0L || position == -1L || position == Long.MAX_VALUE) {
                    return null
                }
                val queueStatus: String? =
                    currentSession.ninchatState?.siteConfig?.getQueueStatus(name, position)
                queueStatus
            }
        }

        @JvmStatic
        fun iceBegun(params: Props) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { currentSession ->
                val stunServers = try {
                    params.getObjectArray("stun_servers")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }
                val turnServers = try {
                    params.getObjectArray("turn_servers")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }
                try {
                    currentSession.ninchatState?.stunServers = getServers(stunServers, false)
                        ?: arrayListOf()
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }

                try {
                    currentSession.ninchatState?.turnServers = getServers(turnServers, true)
                        ?: arrayListOf()
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }

                currentSession.contextWeakReference?.get()?.let { context ->
                    LocalBroadcastManager.getInstance(context).sendBroadcast(
                        Intent(Broadcast.WEBRTC_MESSAGE)
                            .putExtra(
                                Broadcast.WEBRTC_MESSAGE_TYPE,
                                NinchatMessageTypes.WEBRTC_SERVERS_PARSED
                            )
                    )
                }
            }
        }

        @JvmStatic
        fun parseQueue(params: Props): String? {
            val sessionManager = NinchatSessionManager.getInstance()
            return sessionManager?.let { currentSession ->
                val queueId = try {
                    params.getString("queue_id")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return null
                }
                val queuePosition = try {
                    params.getInt("queue_position")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return null
                }
                val queueAttributes = try {
                    params.getObject("queue_attrs")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    currentSession.sendQueueParsingError()
                    return null
                }
                val closed = try {
                    queueAttributes.getBool("closed")
                } catch (e: Exception) {
                    false
                }
                val supportVideos = queueAttributes?.getSafe<String>("video") == "member"
                val supportFiles = queueAttributes?.getSafe<String>("upload") == "member"
                if (currentSession.getQueue(queueId) == null) {
                    val queueName = queueAttributes?.getString("name")
                    currentSession.ninchatState.addQueue(
                        NinchatQueue(
                            queueId,
                            name = queueName,
                            supportFiles = supportFiles,
                            supportVideos = supportVideos,
                        )
                    )
                }
                val currentQueue = currentSession.getQueue(queueId)
                currentQueue?.apply {
                    // only update the queue position if it is non zero
                    if (queuePosition > 0) position = queuePosition
                    // if not already in queue, only then update queue close state
                    if ((position == Long.MAX_VALUE || position == 0L)) {
                        isClosed = closed
                    }
                    this.supportFiles = supportFiles
                    this.supportVideos = supportVideos
                }
                return queueId
            }
        }

        @JvmStatic
        fun fileFound(params: Props) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { currentSession ->
                val fileId = try {
                    params.getString("file_id")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }
                val url = try {
                    params.getString("file_url")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }
                val thumbnailUrl = try {
                    params.getString("thumbnail_url")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }
                val urlExpiry = try {
                    params.getInt("url_expiry")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }
                var aspectRatio: Float
                var width: Long
                var height: Long
                try {
                    val attrs = params.getObject("file_attrs")
                    val thumbnail = attrs.getObject("thumbnail")
                    width = thumbnail.getInt("width")
                    height = thumbnail.getInt("height")
                    aspectRatio = width.toFloat() / height.toFloat()
                } catch (e: Exception) {
                    aspectRatio = 16.0f / 9.0f
                    width = -1
                    height = -1
                }
                val file = currentSession.ninchatState?.getFile(fileId)
                file?.url = url
                file?.thumbnailUrl = thumbnailUrl
                file?.urlExpiry = Date(urlExpiry)
                file?.aspectRatio = aspectRatio
                file?.fileWidth = width
                file?.fileHeight = height
                file?.isDownloadableFile = width == -1L || height == -1L
                file?.let { currentFile ->
                    currentSession.messageAdapter?.add(
                        currentFile.messageId,
                        NinchatMessage(
                            null,
                            fileId,
                            currentFile.sender,
                            currentFile.senderName,
                            currentFile.timestamp,
                            currentFile.isRemote
                        )
                    )
                }
            }
        }

        @JvmStatic
        fun channelJoined(params: Props) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { currentSession ->
                val isClosed = try {
                    params.getObject("channel_attrs")?.getBool("closed")
                } catch (e: Exception) {
                    false
                }
                currentSession.ninchatState.queueId = try {
                    params.getObject("channel_attrs")?.getString("queue_id")
                } catch (e: Exception) {
                    sessionManager.ninchatState.queueId
                }
                currentSession.ninchatState?.channelId = try {
                    params.getString("channel_id")
                } catch (e: Exception) {
                    currentSession.sessionError(e)
                    return
                }
                val channelMembers = NinchatPropsParser.getUsersFromChannel(params)
                channelMembers.map {
                    currentSession.ninchatState.addMember(
                        it.first,
                        it.second
                    );
                }
                sessionManager.getOnInitializeMessageAdapter(object : NinchatAdapterCallback {
                    override fun onMessageAdapter(adapter: NinchatMessageAdapter) {
                        adapter.addMetaMessage("", currentSession.chatStarted)
                    }
                })
                // may be send "message" from pre_answer
                val isAudienceTransfer = params.getSafe<Props>("channel_attrs")?.getSafe<String>("audience_transferred")
                val isTargetAudience = params.getSafe<Props>("channel_attrs")?.getSafe<String>("requester_id") == sessionManager.ninchatState?.sessionCredentials?.userId
                val message = params.getSafe<Props>("audience_metadata")?.getSafe<Props>("pre_answers")?.getSafe<String>("message")
                val userName = params.getSafe<Props>("audience_metadata")?.getSafe<Props>("pre_answers")?.getSafe<String>("userName")
                val isGroupVideoChannel = params.getSafe<Props>("channel_attrs")?.getSafe<String>("video") == "group"
                // store whether this channel is a group video channel
                sessionManager.ninchatState.isGroupVideoChannel = isGroupVideoChannel

                if(!userName.isNullOrEmpty()) {
                    currentSession.ninchatState.userName = userName
                }
                if(isAudienceTransfer.isNullOrEmpty() && isTargetAudience && !message.isNullOrEmpty() ) {
                    val data = JSONObject().apply {
                        put("text", message)
                    }

                    getIOScope().launch(CoroutineExceptionHandler(handler = { _, _ -> {} })) {
                        NinchatSendMessage.execute(
                                currentSession = sessionManager.session,
                                channelId = sessionManager.ninchatState?.channelId,
                                messageType = NinchatMessageTypes.TEXT,
                                message = data.toString()
                        )
                    }
                }
                currentSession.contextWeakReference?.get()?.let { mContext ->
                    val i = Intent(Broadcast.CHANNEL_JOINED)
                    i.putExtra(Parameter.CHAT_IS_CLOSED, isClosed)
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(i)
                }
            }
        }

        @JvmStatic
        fun parseQueues(params: Props?) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { currentSession ->
                currentSession.ninchatState?.queues?.clear()
                currentSession.messageAdapter?.clear()
                getOpenQueueList(
                    params,
                    currentSession.ninchatState?.siteConfig?.getAudienceQueues()
                ).map {
                    currentSession.ninchatState?.addQueue(it)
                    currentSession.ninchatQueueListAdapter?.addQueue(it)
                }
                currentSession.contextWeakReference?.get()?.let { mContext ->
                    if (currentSession.ninchatState?.getQueueList()?.size ?: 0 > 0) {
                        LocalBroadcastManager.getInstance(mContext)
                            .sendBroadcast(Intent(NinchatSession.Broadcast.QUEUES_UPDATED))
                    }
                }
                currentSession.activityWeakReference?.get()?.let { mActivity ->
                    mActivity.startActivityForResult(
                        NinchatActivityPresenter.getLaunchIntent(
                            mActivity,
                            currentSession.ninchatState?.queueId
                        ), currentSession.ninchatState?.requestCode
                            ?: 0
                    )
                }
                currentSession.eventListenerWeakReference?.get()?.onSessionStarted()
                currentSession.ninchatState?.queueId?.let {
                    NinchatScopeHandler.getIOScope().launch {
                        NinchatDescribeQueue.execute(
                            currentSession = currentSession.session,
                            queueId = currentSession.ninchatState?.queueId
                        )
                    }
                }
            }
        }

        @JvmStatic
        fun memberUpdated(params: Props) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { ninchatSessionManager ->
                val sender = try {
                    params.getString("user_id")
                } catch (e: Exception) {
                    return
                }
                if (sender == ninchatSessionManager.ninchatState?.userId) {
                    // Do not update myself
                    return
                }
                val memberAttrs = try {
                    params.getObject("member_attrs")
                } catch (e: Exception) {
                    null
                }
                val addWritingMessage = try {
                    memberAttrs?.getBool("writing") ?: false
                } catch (e: Exception) {
                    false
                }
                if (addWritingMessage) {
                    ninchatSessionManager.messageAdapter?.addWriting(sender)
                } else {
                    ninchatSessionManager.messageAdapter?.removeWritingMessage(sender)
                }
            }
        }

        @JvmStatic
        fun channelUpdated(params: Props) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { ninchatSessionManager ->
                try {
                    if (params.getString("channel_id") != ninchatSessionManager.ninchatState?.channelId) {
                        return
                    }
                } catch (e: Exception) {
                    return
                }
                val channelAttributes = try {
                    params.getObject("channel_attrs")
                } catch (e: Exception) {
                    return
                }
                val closed = try {
                    channelAttributes.getBool("closed")
                } catch (e: Exception) {
                    return
                }
                val suspended = try {
                    channelAttributes.getBool("suspended")
                } catch (e: Exception) {
                    return
                }
                val isAudienceTransfer = params.getSafe<String>("event_cause") == "audience_transfer"
                val isGroupVideoChannel = channelAttributes?.getSafe<String>("video") == "group"
                // store whether this channel is a group video channel
                ninchatSessionManager.ninchatState.isGroupVideoChannel = isGroupVideoChannel

                ninchatSessionManager.contextWeakReference?.get()?.let { mContext ->
                    if (!isAudienceTransfer && (closed || suspended)) {
                        LocalBroadcastManager.getInstance(mContext)
                            .sendBroadcast(Intent(Broadcast.CHANNEL_CLOSED))
                    }
                }
            }
        }

        @JvmStatic
        fun jitsiDiscovered(params: Props) {
            val jitsiRoom = params.getSafe<String>("jitsi_room")
            val jitsiToken = params.getSafe<String>("jitsi_token")

            val serverPrefix = jitsiRoom?.substringBeforeLast(".")
            NinchatSessionManager.getInstance()?.context?.let { mContext ->
                LocalBroadcastManager.getInstance(mContext)
                    .sendBroadcast(Intent(Broadcast.WEBRTC_MESSAGE).also { mIntent ->
                        mIntent.putExtra(Broadcast.WEBRTC_MESSAGE_TYPE, NinchatMessageTypes.WEBRTC_JITSI_SERVER_CONFIG)
                        mIntent.putExtra(Broadcast.WEBRTC_MESSAGE_JITSI_ROOM, jitsiRoom)
                        mIntent.putExtra(Broadcast.WEBRTC_MESSAGE_JITSI_TOKEN, jitsiToken)
                        mIntent.putExtra(Broadcast.WEBRTC_MESSAGE_JITSI_SERVER_PREFIX, serverPrefix)
                    })
            }

        }
    }
}