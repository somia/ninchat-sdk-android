package com.ninchat.sdk.helper.session

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.client.Objects
import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSession
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.activities.NinchatActivity
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser.Companion.getChannelIdFromUserChannel
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser.Companion.getOpenQueueList
import com.ninchat.sdk.models.NinchatMessage
import com.ninchat.sdk.models.NinchatQueue
import com.ninchat.sdk.models.NinchatWebRTCServerInfo
import com.ninchat.sdk.networkdispatchers.NinchatDescribeChannel
import com.ninchat.sdk.networkdispatchers.NinchatRequestAudience
import com.ninchat.sdk.utils.messagetype.NinchatMessageTypes
import com.ninchat.sdk.utils.misc.Broadcast
import com.ninchat.sdk.utils.misc.Parameter
import com.ninchat.sdk.utils.threadutils.NinchatScopeHandler.getIOScope
import kotlinx.coroutines.launch
import java.util.*

class NinchatSessionManagerHelper {
    companion object {
        internal val getServers = fun(serverList: Objects?, isTurn: Boolean): ArrayList<NinchatWebRTCServerInfo>? {
            return serverList?.let {
                val retval = arrayListOf<NinchatWebRTCServerInfo>()
                for (i in 0 until it.length()) {
                    val stunServerProps = it[i]
                    val urls = stunServerProps.getStringArray("urls")
                    for (j in 0 until urls.length()) {
                        if (isTurn) {
                            val username: String = stunServerProps.getString("username")
                            val credential: String = stunServerProps.getString("credential")
                            retval.add(NinchatWebRTCServerInfo(urls.get(j), username, credential));
                        } else {
                            retval.add(NinchatWebRTCServerInfo(urls[j]))
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
                    if (currentSession.ninchatSessionHolder?.hasChannel() == true) {
                        val currentChannelId = getChannelIdFromUserChannel(currentSession.ninchatState?.userChannels)
                        getIOScope().launch {
                            val actionId = NinchatDescribeChannel.execute(
                                    currentSession = currentSession.session,
                                    channelId = currentChannelId
                            )
                            currentSession.ninchatState.actionId = actionId
                        }
                        return
                    }
                    if (currentSession.ninchatSessionHolder?.isInQueue() == true) {
                        // already in queue
                        return
                    }
                }
                // if none of above. try to join queue by calling request audience
                getIOScope().launch {
                    NinchatRequestAudience.execute(
                            currentSession = currentSession.session,
                            queueId = queueId,
                            audienceMetadata = currentSession.ninchatState?.audienceMetadata
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
                        ?: NinchatPropsParser.getQueuePositionByQueueId(props = sessionManager.ninchatState.userQueues, queueId = queueId
                                ?: "")
                val name = selectedQueue?.name
                        ?: NinchatPropsParser.getQueueNameByQueueId(props = sessionManager.ninchatState.userQueues, queueId = queueId
                                ?: "")
                // if there is no queue position
                if (position == -1L) {
                    return null
                }
                val queueStatus: String? = currentSession.ninchatState?.siteConfig?.getQueueStatus(name, position)
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
                    LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(Broadcast.WEBRTC_MESSAGE)
                            .putExtra(Broadcast.WEBRTC_MESSAGE_TYPE, NinchatMessageTypes.WEBRTC_SERVERS_PARSED))
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
                if (currentSession.getQueue(queueId) == null) {
                    val queueName = queueAttributes?.getString("name")
                    currentSession.ninchatState.addQueue(NinchatQueue(queueId, queueName))
                }
                val currentQueue = currentSession.getQueue(queueId)
                currentQueue?.let {
                    currentQueue.position = queuePosition
                    currentQueue.isClosed = closed
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
                file?.urlExpiry = Date(urlExpiry)
                file?.setAspectRatio(aspectRatio)
                file?.setWidth(width)
                file?.setHeight(height)
                file?.isDownloadableFile = width == -1L || height == -1L
                file?.let { currentFile ->
                    currentSession.messageAdapter?.add(currentFile.messageId,
                            NinchatMessage(null, fileId, currentFile.sender, currentFile.timestamp, currentFile.isRemote))
                }
            }
        }

        @JvmStatic
        fun channelJoined(params: Props) {
            val sessionManager = NinchatSessionManager.getInstance()
            sessionManager?.let { currentSession ->
                val isClosed = try {
                    val channelAttrs = params.getObject("channel_attrs")
                    channelAttrs.getBool("closed")
                } catch (e: Exception) {
                    false
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
                Handler(Looper.getMainLooper()).post {
                    currentSession.messageAdapter?.addMetaMessage("", currentSession.getChatStarted())
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
                getOpenQueueList(params, currentSession.ninchatState?.siteConfig?.getAudienceQueues()).map {
                    currentSession.ninchatState?.addQueue(it)
                    currentSession.ninchatQueueListAdapter?.addQueue(it)
                }
                currentSession.contextWeakReference?.get()?.let { mContext ->
                    if (currentSession.ninchatState?.queues?.size ?: 0 > 0) {
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(Intent(NinchatSession.Broadcast.QUEUES_UPDATED))
                    }
                }
                currentSession.activityWeakReference?.get()?.let { mActivity ->
                    mActivity.startActivityForResult(
                            NinchatActivity.getLaunchIntent(mActivity,
                                    currentSession.ninchatState?.queueId), currentSession.ninchatState?.requestCode
                            ?: 0)
                }
                currentSession.eventListenerWeakReference?.get()?.onSessionStarted()
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
                } catch (e: java.lang.Exception) {
                    return
                }
                val channelAttributes = try {
                    params.getObject("channel_attrs")
                } catch (e: java.lang.Exception) {
                    return
                }
                val closed = try {
                    channelAttributes.getBool("closed")
                } catch (e: java.lang.Exception) {
                    return
                }
                val suspended = try {
                    channelAttributes.getBool("suspended")
                } catch (e: java.lang.Exception) {
                    return
                }
                ninchatSessionManager.contextWeakReference?.get()?.let { mContext ->
                    if (closed || suspended) {
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(Intent(Broadcast.CHANNEL_CLOSED))
                    }
                }
            }
        }


    }
}