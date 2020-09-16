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
            return try {
                props?.accept(parser)
                return parser.properties.values.map {
                    (it as Props).getObject("channel_attrs")
                }.map {
                    it.getString("queue_id")
                }.firstOrNull { it.isNotBlank() }
            } catch (_: Exception) {
                null
            }
        }

        /**
         * Parse queue id from user queue.
         * Return the first queue id that has non-zero ( in queue ) queue position.
         */
        @JvmStatic
        fun getQueueIdFromUserQueue(props: Props?): String? {
            val parser = NinchatPropVisitor()
            return try {
                props?.accept(parser)
                return parser.properties.filterValues { value ->
                    val position = (value as Props).getInt("queue_position")
                    position > 0L
                }.keys.firstOrNull()
            } catch (e: Exception) {
                null
            }
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
                val queuePosition = parser.properties.filterKeys {
                    it == queueId
                }.map { (it as Props).getInt("queue_position") }.firstOrNull()
                return queuePosition ?: -1
            } catch (e: java.lang.Exception) {
                -1
            }

        }

        @JvmStatic
        fun getQueueNameByQueueId(props: Props?, queueId: String): String? {
            val parser = NinchatPropVisitor()
            return try {
                props?.accept(parser)
                parser.properties.filterKeys {
                    it == queueId
                }.filter {
                    val queuePosition = (it as Props).getInt("queue_position")
                    queuePosition > 0
                }.map {
                    (it as Props).getObject("queue_attrs")
                }.map {
                    (it as Props).getString("name")
                }.firstOrNull()
            } catch (e: java.lang.Exception) {
                null
            }
        }

        @JvmStatic
        fun getChannelId(props: Props?): String? {
            val parser = NinchatPropVisitor()
            return try {
                props?.accept(parser)
                parser.properties.keys.firstOrNull()
            } catch (e: java.lang.Exception) {
                null
            }
        }

        @JvmStatic
        fun hasUserChannel(props: Props?): Boolean {
            val parser = NinchatPropVisitor()
            return try {
                props?.accept(parser)
                parser.properties.keys.size > 0
            } catch (e: java.lang.Exception) {
                false
            }
        }

        @JvmStatic
        fun hasUserQueues(currentUserQueues: Props?): Boolean {
            val parser = NinchatPropVisitor()
            return try {
                currentUserQueues?.accept(parser)
                parser.properties.values.any {
                    val queuePosition = (it as Props).getInt("queue_position")
                    queuePosition > 0L
                }
            } catch (e: java.lang.Exception) {
                false
            }
        }


    }
}