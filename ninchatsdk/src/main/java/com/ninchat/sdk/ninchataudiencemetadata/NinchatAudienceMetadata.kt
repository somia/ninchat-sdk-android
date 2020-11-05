package com.ninchat.sdk.ninchataudiencemetadata

import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser

class NinchatAudienceMetadata {
    private var audienceMetadata: Props? = null
        set(value: Props?) {
            field = value
            // save audience metadata in persistence storage
            NinchatSessionManager.getInstance()?.context?.let {
                NinchatPropsParser.getAudienceMetadata(props = value)
            }
        }
}