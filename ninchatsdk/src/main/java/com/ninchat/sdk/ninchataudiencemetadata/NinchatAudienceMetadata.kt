package com.ninchat.sdk.ninchataudiencemetadata

import android.content.Context
import com.ninchat.client.Props
import com.ninchat.sdk.NinchatSessionManager
import com.ninchat.sdk.helper.propsparser.NinchatPropsParser
import com.ninchat.sdk.ninchatdb.light.NinchatPersistenceStore

data class NinchatAudienceMetadata(
        internal var audienceMetadata: Props? = null,
        private val debugContext: Context? = null,
) {

    fun set(props: Props?) {
        audienceMetadata = props
        // save audience metadata in persistence storage if it is not empty
        (debugContext ?: NinchatSessionManager.getInstance()?.context)?.let { mContext ->
            try {
                NinchatPropsParser.getAudienceMetadata(props = props)?.let { audienceMetadataString ->
                    NinchatPersistenceStore.save("audienceMetadata", audienceMetadataString, mContext)
                }
            } catch (_: Exception) {
            }
        }
        // call remove in order to remove the cache since cache does not save null value automatically
        if (props == null) remove()
    }

    fun get(): Props? {
        if (audienceMetadata != null) return audienceMetadata
        return (debugContext ?: NinchatSessionManager.getInstance()?.context)?.let { mContext ->
            val metadataString = NinchatPersistenceStore.get("audienceMetadata", mContext)
            if (metadataString.isNullOrEmpty()) return null
            return try {
                NinchatPropsParser.toAudienceMetadata(metadataString)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun remove() {
        audienceMetadata = null
        (debugContext ?: NinchatSessionManager.getInstance()?.context)?.let { mContext ->
            NinchatPersistenceStore.remove("audienceMetadata", mContext)
        }
    }

    fun has(): Boolean {
        if (audienceMetadata != null) return true
        return (debugContext ?: NinchatSessionManager.getInstance()?.context)?.let { mContext ->
            NinchatPersistenceStore.has("audienceMetadata", mContext)
        } ?: false
    }
}