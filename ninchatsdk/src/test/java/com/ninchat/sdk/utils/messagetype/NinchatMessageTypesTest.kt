package com.ninchat.sdk.utils.messagetype

import org.junit.Assert
import org.junit.Test

class NinchatMessageTypesTest {
    @Test
    fun `should match all message types`() {
        val expectedText = "ninchat.com/text"
        val expectedFile = "ninchat.com/file"
        val expectedUICompose = "ninchat.com/ui/compose"
        val expectedUIAction = "ninchat.com/ui/action"
        val expectedMetadata = "ninchat.com/metadata"

        val expectedICECandidate = "ninchat.com/rtc/ice-candidate"
        val expectedAnswers = "ninchat.com/rtc/answer"
        val expectedOffer = "ninchat.com/rtc/offer"
        val expectedCall = "ninchat.com/rtc/call"
        val expectedPickUp = "ninchat.com/rtc/pick-up"
        val expectedHangUp = "ninchat.com/rtc/hang-up"
        val expectedWebRTCServersParsed = "ninchat.com/rtc/serversParsed"

        val expectedWebRTCMessageTypes = mutableListOf(
                expectedICECandidate,
                expectedAnswers,
                expectedOffer,
                expectedCall,
                expectedPickUp,
                expectedHangUp
        )
        Assert.assertEquals("shoud match ninchat TEXT message type", expectedText, NinchatMessageTypes.TEXT)
        Assert.assertEquals("shoud match ninchat FILE message type", expectedFile, NinchatMessageTypes.FILE)
        Assert.assertEquals("shoud match ninchat UI_COMPOSE message type", expectedUICompose, NinchatMessageTypes.UI_COMPOSE)
        Assert.assertEquals("shoud match ninchat UI_ACTION message type", expectedUIAction, NinchatMessageTypes.UI_ACTION)
        Assert.assertEquals("shoud match ninchat METADATA message type", expectedMetadata, NinchatMessageTypes.METADATA)
        Assert.assertEquals("shoud match ninchat ICE_CANDIDATE message type", expectedICECandidate, NinchatMessageTypes.ICE_CANDIDATE)
        Assert.assertEquals("shoud match ninchat ANSWER message type", expectedAnswers, NinchatMessageTypes.ANSWER)
        Assert.assertEquals("shoud match ninchat OFFER message type", expectedOffer, NinchatMessageTypes.OFFER)
        Assert.assertEquals("shoud match ninchat CALL message type", expectedCall, NinchatMessageTypes.CALL)
        Assert.assertEquals("shoud match ninchat PICK_UP message type", expectedPickUp, NinchatMessageTypes.PICK_UP)
        Assert.assertEquals("shoud match ninchat HANG_UP message type", expectedHangUp, NinchatMessageTypes.HANG_UP)
        Assert.assertEquals("shoud match ninchat WEBRTC_SERVERS_PARSED message type", expectedWebRTCServersParsed, NinchatMessageTypes.WEBRTC_SERVERS_PARSED)
        Assert.assertEquals("shoud match ninchat WEBRTC_MESSAGE_TYPES message type", expectedWebRTCMessageTypes, NinchatMessageTypes.WEBRTC_MESSAGE_TYPES)
    }

    @Test
    fun `should be webrtc message`() {
        val expectedICECandidate = "ninchat.com/rtc/ice-candidate"
        val expectedAnswers = "ninchat.com/rtc/answer"
        val expectedOffer = "ninchat.com/rtc/offer"
        val expectedCall = "ninchat.com/rtc/call"
        val expectedPickUp = "ninchat.com/rtc/pick-up"
        val expectedHangUp = "ninchat.com/rtc/hang-up"

        Assert.assertEquals("should be an webrtc message type", true, NinchatMessageTypes.webrtcMessage(expectedICECandidate))
        Assert.assertEquals("should be an webrtc message type", true, NinchatMessageTypes.webrtcMessage(expectedAnswers))
        Assert.assertEquals("should be an webrtc message type", true, NinchatMessageTypes.webrtcMessage(expectedOffer))
        Assert.assertEquals("should be an webrtc message type", true, NinchatMessageTypes.webrtcMessage(expectedCall))
        Assert.assertEquals("should be an webrtc message type", true, NinchatMessageTypes.webrtcMessage(expectedPickUp))
        Assert.assertEquals("should be an webrtc message type", true, NinchatMessageTypes.webrtcMessage(expectedHangUp))
    }

    @Test
    fun `empty string should not be webrtc message`() {
        Assert.assertEquals(false, NinchatMessageTypes.webrtcMessage(""))
    }

    @Test
    fun `null string should not be webrtc message`() {
        Assert.assertEquals(false, NinchatMessageTypes.webrtcMessage(null))
    }
}