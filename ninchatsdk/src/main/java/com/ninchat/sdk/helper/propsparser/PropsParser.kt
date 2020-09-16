package com.ninchat.sdk.helper.propsparser

import com.ninchat.client.Props
import com.ninchat.sdk.utils.propsvisitor.NinchatPropVisitor
import java.lang.Exception

class PropsParser {
    companion object {

        /**
         * Parse queue id from user channel.
         * Return the first queue id that is not null ( queue id is present in the channel attribute )
         */
        @JvmStatic
        fun getQueueIdFromUserChannels(props: Props?): String? {
            val parser = NinchatPropVisitor()
            try {
                props?.accept(parser)
                for (channelId in parser.properties.keys) {
                    val channelInfo = parser.properties[channelId] as Props?
                    val channelAttrs = channelInfo?.getObject("channel_attrs")
                    val queueId = channelAttrs?.getString("queue_id")
                    queueId?.let {
                        if (it.isNotBlank()) return queueId
                    }
                }
            } catch (_: Exception) {
            }
            return null
        }

        /**
         * Parse queue id from user queue.
         * Return the first queue id that has non-zero ( in queue ) queue position.
         */
        @JvmStatic
        fun getQueueIdFromUserQueue(props: Props?): String? {
            val parser = NinchatPropVisitor()
            try {
                props?.accept(parser)
                for (currentQueueId in parser.properties.keys) {
                    val queueInfo = parser.properties[currentQueueId] as Props?
                    val queuePosition = queueInfo?.getInt("queue_position")
                    if (queuePosition != 0L) return currentQueueId
                }
            } catch (_: Exception) {
            }

            return null
        }

        /**
         * Parse queue position from user queue for a given queueId
         */
        @JvmStatic
        fun getQueuePositionByQueueId(props: Props?, queueId: String): Long {
            val parser = NinchatPropVisitor()
            return try {
                props?.accept(parser)
                // audience has one or multiple channel
                val queueInfo = parser.properties.filterKeys { it == queueId } as Props?
                return queueInfo?.getInt("queue_position") ?: -1
            } catch (e: java.lang.Exception) {
                -1
            }
        }

        @JvmStatic
        fun parseQueueNameFromUserQueues(currentUserQueues: Props?, queueId: String): String? {
            if (currentUserQueues == null) return null
            val parser = NinchatPropVisitor()
            try {
                currentUserQueues.accept(parser)
                // audience has one or multiple channel
                for (currentQueueId in parser.properties.keys) {
                    val queueInfo = parser.properties[currentQueueId] as Props?
                    val queuePosition = queueInfo!!.getInt("queue_position")
                    if (queuePosition != 0L && currentQueueId == queueId) {
                        val queueAttrs = queueInfo.getObject("queue_attrs")
                        if (queueAttrs != null) {
                            return queueAttrs.getString("name")
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                return null
            }
            return null
        }

        @JvmStatic
        fun parseChannelId(currentUserChannel: Props): String? {
            val parser = NinchatPropVisitor()
            try {
                currentUserChannel.accept(parser)
                // audience has one or multiple channel
                for (channelId in parser.properties.keys) {
                    return channelId
                }
            } catch (e: java.lang.Exception) {
                return null
            }
            return null
        }

        @JvmStatic
        fun hasUserChannel(currentUserChannel: Props): Boolean {
            val parser = NinchatPropVisitor()
            return try {
                currentUserChannel.accept(parser)
                parser.properties.keys.size > 0
            } catch (e: java.lang.Exception) {
                false
            }
        }

        @JvmStatic
        fun hasUserQueues(currentUserQueues: Props): Boolean {
            val parser = NinchatPropVisitor()
            try {
                currentUserQueues.accept(parser)
                for (currentQueueId in parser.properties.keys) {
                    val info = parser.properties[currentQueueId] as Props?
                    try {
                        val queuePosition = info!!.getInt("queue_position")
                        if (queuePosition != 0L) {
                            return true
                        }
                    } catch (e: java.lang.Exception) {
                        // passed
                    }
                }
            } catch (e: java.lang.Exception) {
                return false
            }
            return false
        }


    }
}