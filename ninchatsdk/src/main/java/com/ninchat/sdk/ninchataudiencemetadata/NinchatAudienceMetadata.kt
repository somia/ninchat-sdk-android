package com.ninchat.sdk.ninchataudiencemetadata

import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser

data class NinchatAudienceMetadata(private var audienceMetadata: Props? = null) {
    fun update(props: Props?) {
        audienceMetadata = props
        // save audience metadata in persistence storage
        NinchatSessionManager.getInstance()?.context?.let {
            val metadata = NinchatPropsParser.getAudienceMetadata(props = props)
        }
    }

    fun get(): Props? {
        return audienceMetadata
    }

    fun has(): Boolean {
        return audienceMetadata != null
    }
}