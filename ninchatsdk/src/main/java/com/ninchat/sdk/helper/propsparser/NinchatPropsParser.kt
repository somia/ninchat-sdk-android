package com.ninchat.sdk.helper.propsparser

import com.ninchat.client.Props
import com.ninchat.sdk.ninchatqueuelist.model.NinchatQueue
import com.ninchat.sdk.models.NinchatUser
import com.ninchat.sdk.utils.propsvisitor.NinchatPropVisitor

class NinchatPropsParser {
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
                }.map {
                    (it.value as Props).getInt("queue_position")
                }.firstOrNull()
                return queuePosition ?: -1
            } catch (e: Exception) {
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
                    val queuePosition = (it.value as Props).getInt("queue_position")
                    queuePosition > 0
                }.map {
                    (it.value as Props).getObject("queue_attrs")
                }.map {
                    (it as Props).getString("name")
                }.firstOrNull()
            } catch (e: Exception) {
                null
            }
        }

        @JvmStatic
        fun getChannelIdFromUserChannel(props: Props?): String? {
            val parser = NinchatPropVisitor()
            return try {
                props?.accept(parser)
                parser.properties.keys.firstOrNull()
            } catch (e: Exception) {
                null
            }
        }

        @JvmStatic
        fun hasUserChannel(props: Props?): Boolean {
            val parser = NinchatPropVisitor()
            return try {
                props?.accept(parser)
                parser.properties.keys.size > 0
            } catch (e: Exception) {
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
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Get list of open queue from real queues found callback props
         */
        @JvmStatic
        fun getOpenQueueList(props: Props?, audienceQueue: Collection<String>? = null): List<NinchatQueue> {
            val parser = NinchatPropVisitor()
            val realmQueues = try {
                props?.getObject("realm_queues")
            } catch (_: Exception) {
                null
            }
            realmQueues?.accept(parser)
            // only get list of queues that are open
            return parser.properties.filterKeys {
                audienceQueue?.contains(it) ?: false
            }.mapValues {
                val currentQueue = (it.value as Props)
                val queuePosition = currentQueue.getInt("queue_position")
                val queueName = currentQueue.getObject("queue_attrs")?.getString("name")
                val queueClosed = currentQueue.getObject("queue_attrs")?.getBool("closed") ?: false
                val ninchatQueue = NinchatQueue(it.key, queueName)
                ninchatQueue.position = queuePosition
                ninchatQueue.isClosed = queueClosed
                ninchatQueue
            }.map { it.value }
        }

        @JvmStatic
        fun getUsersFromChannel(props: Props?): List<Pair<String, NinchatUser>> {
            val parser = NinchatPropVisitor()
            val channelMembers = try {
                props?.getObject("channel_members")
            } catch (_: Exception) {
                null
            }
            channelMembers?.accept(parser)
            // only get list of queues that are open
            return parser.properties.map {
                val userAttr = (it.value as Props).getObject("user_attrs")
                val displayName = userAttr?.getString("name")
                val realName = userAttr?.getString("realname")
                val avatar = userAttr?.getString("iconurl")
                val guest = userAttr?.getBool("guest") ?: false
                Pair(it.key, NinchatUser(displayName, realName, avatar, guest))
            }
        }

        fun getAudienceMetadata(props: Props?): String? {
            return props?.marshalJSON()
        }

        fun toAudienceMetadata(audienceMetadata: String): Props? {
            val props = Props()
            props.unmarshalJSON(audienceMetadata)
            return props
        }

    }
}