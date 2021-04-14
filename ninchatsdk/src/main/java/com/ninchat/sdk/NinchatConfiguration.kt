package com.ninchat.sdk

/**
 * Ninchat configuration
 * With this class we will modify configuration related attributes.
 * todo - An initial version. Add more functionality and remove me
 * The current implementation can change the caller user name attribute so that user can sdk user
 * can change user name during set up
 */
class NinchatConfiguration private constructor() {
    var userName: String? = null
        private set

    var ninchatChatBackground: Int = -1
        private set

    class Builder {
        private var userName: String? = null
        private var ninchatChatBackground: Int = -1
        fun setUserName(userName: String?): Builder {
            this.userName = userName
            return this
        }

        fun setNinchatChatBackground(id: Int): Builder {
            this.ninchatChatBackground = id
            return this
        }

        fun create(): NinchatConfiguration {
            val configurationManager = NinchatConfiguration()
            configurationManager.userName = userName
            configurationManager.ninchatChatBackground = ninchatChatBackground

            return configurationManager
        }
    }
}