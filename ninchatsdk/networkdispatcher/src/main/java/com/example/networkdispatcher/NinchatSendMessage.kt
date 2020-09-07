
import com.ninchat.client.Payload
import com.ninchat.client.Props
import com.ninchat.client.Session

class NinchatSendMessage {
    fun execute(currentSession: Session? = null,
                channelId: String? = null,
                messageType: String? = null,
                message: String? = null
    ): Long {

        val params = Props()
        params.setString("action", "send_message")
        
        channelId?.let {
            params.setString("channel_id", channelId)    
        }
        messageType?.let {
            params.setString("message_type", messageType)
            messageType.startsWith("ninchat.com/rtc/").let {
                params.setInt("message_ttl", 10)
            }
        }

        val payload = Payload()
        message?.let {
            payload.append(message.toByteArray())
        }

        val actionId: Long = try {
            currentSession?.send(params, payload) ?: -1
        } catch (e: Exception) {
            return -1
        }
        return actionId
    }
}