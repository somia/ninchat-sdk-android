package com.ninchat.sdk.helper.propsparser

import com.ninchat.client.Objects
import com.ninchat.client.Props
import com.ninchat.client.Strings
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
                    (it as Props).getSafe<Props>("channel_attrs")
                }.map {
                    it?.getSafe<String>("queue_id") ?: ""
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
                    val position = (value as Props).getSafe<Long>("queue_position") ?: 0L
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
                    (it.value as Props).getSafe<Long>("queue_position")
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
                    val queuePosition =
                        (it.value as Props).getSafe<Long>("queue_position") ?: 0L
                    queuePosition > 0
                }.map {
                    (it.value as Props).getSafe<Props>("queue_attrs")
                }.map {
                    (it as Props).getSafe<String>("name")
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
                parser.properties.keys.maxOrNull()
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
                    val queuePosition = (it as Props).getSafe<Long>("queue_position") ?: 0L
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
        fun getOpenQueueList(
            props: Props?,
            audienceQueue: Collection<String>? = null
        ): List<NinchatQueue> {
            val parser = NinchatPropVisitor()
            val realmQueues = props?.getSafe<Props>("realm_queues")
            realmQueues?.accept(parser)
            // only get list of queues that are open
            return parser.properties.filterKeys {
                audienceQueue?.contains(it) ?: false
            }.mapValues {
                val currentQueue = (it.value as Props)
                val queuePosition = currentQueue.getSafe<Long>("queue_position") ?: 0L
                val queueName =
                    currentQueue.getSafe<Props>("queue_attrs")?.getSafe<String>("name") ?: ""
                val queueClosed = currentQueue.getSafe<Props>("queue_attrs")
                    ?.getSafe<Boolean>("closed") ?: false
                val supportVideos = currentQueue.getSafe<Props>("queue_attrs")
                    ?.getSafe<String>("video") == "member"
                val supportFiles = currentQueue.getSafe<Props>("queue_attrs")
                    ?.getSafe<String>("upload") == "member"
                val ninchatQueue = NinchatQueue(
                    it.key,
                    name = queueName,
                    supportVideos = supportVideos,
                    supportFiles = supportFiles,
                )
                ninchatQueue.position = queuePosition
                ninchatQueue.isClosed = queueClosed
                ninchatQueue
            }.map { it.value }
        }

        @JvmStatic
        fun getUsersFromChannel(props: Props?): List<Pair<String, NinchatUser>> {
            val parser = NinchatPropVisitor()
            val channelMembers = props?.getSafe<Props>("channel_members")
            val channelId = props?.getSafe<String>("channel_id")
            channelMembers?.accept(parser)
            // only get list of queues that are open
            return parser.properties.map {
                val userAttr = (it.value as Props).getSafe<Props>("user_attrs")
                val displayName = userAttr?.getSafe<String>("name")
                val realName = userAttr?.getSafe<String>("realname")
                val avatar = userAttr?.getSafe<String>("iconurl")
                val guest = userAttr?.getSafe<Boolean>("guest") ?: false
                val jobTitle = userAttr?.getSafe<Props>("info")?.getSafe<String>("job_title")
                Pair(it.key, NinchatUser(displayName, realName, avatar, guest, jobTitle,channelId))
            }
        }

        @JvmStatic
        fun getPreAnswersFromProps(props: Props?): List<Pair<String, Any>> {
            val parser = NinchatPropVisitor()
            val preAnswers = props?.getSafe<Props>("pre_answers")
            preAnswers?.accept(parser)
            return parser.properties.mapNotNull {
                if (it.key != "tags") {
                    Pair(it.key, it.value)
                } else null
            }
        }

        fun getAudienceMetadata(props: Props?): String? {
            return props?.marshalJSON()
        }

        fun toAudienceMetadata(audienceMetadata: String?): Props? {
            if (audienceMetadata.isNullOrEmpty()) return null
            val props = Props()
            props.unmarshalJSON(audienceMetadata)
            return props
        }

    }
}

inline fun <reified T> Props.getSafe(key: String, default: T? = null): T? {
    return try {
        return when (T::class) {
            Boolean::class -> this.getBool(key)
            Double::class -> this.getFloat(key)
            Long::class -> this.getInt(key)
            Props::class -> this.getObject(key)
            Objects::class -> this.getObjectArray(key)
            String::class -> this.getString(key)
            Strings::class -> this.getStringArray(key)
            else ->
                default
        } as T
    } catch (_: Exception) {
        default
    }

}