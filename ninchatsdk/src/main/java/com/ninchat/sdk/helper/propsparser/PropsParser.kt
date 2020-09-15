package com.ninchat.sdk.helper.propsparser

import com.ninchat.client.Props
import com.ninchat.sdk.utils.propsvisitor.NinchatPropVisitor

class PropsParser {
    companion object {
        private fun parseQueueIdFromUserChannels(currentUserChannels: Props?): String? {
            val parser = NinchatPropVisitor()
            try {
                currentUserChannels?.accept(parser)
                // audience has one or multiple channel
                for (channelId in parser.properties.keys) {
                    val channelInfo = parser.properties.get(channelId) as Props
                    val channelAttrs = channelInfo.getObject("channel_attrs")
                    return channelAttrs.getString("queue_id")
                }
            } catch (e: Exception) {
                return null
            }
            return null
        }
    }
}