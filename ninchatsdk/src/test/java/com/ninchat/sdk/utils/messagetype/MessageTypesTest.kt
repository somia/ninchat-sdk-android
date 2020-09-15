package com.ninchat.sdk.utils.messagetype

import org.junit.Assert
import org.junit.Test

class MessageTypesTest {
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
        Assert.assertEquals("shoud match ninchat TEXT message type", expectedText, MessageTypes.TEXT)
        Assert.assertEquals("shoud match ninchat FILE message type", expectedFile, MessageTypes.FILE)
        Assert.assertEquals("shoud match ninchat UI_COMPOSE message type", expectedUICompose, MessageTypes.UI_COMPOSE)
        Assert.assertEquals("shoud match ninchat UI_ACTION message type", expectedUIAction, MessageTypes.UI_ACTION)
        Assert.assertEquals("shoud match ninchat METADATA message type", expectedMetadata, MessageTypes.METADATA)
        Assert.assertEquals("shoud match ninchat ICE_CANDIDATE message type", expectedICECandidate, MessageTypes.ICE_CANDIDATE)
        Assert.assertEquals("shoud match ninchat ANSWER message type", expectedAnswers, MessageTypes.ANSWER)
        Assert.assertEquals("shoud match ninchat OFFER message type", expectedOffer, MessageTypes.OFFER)
        Assert.assertEquals("shoud match ninchat CALL message type", expectedCall, MessageTypes.CALL)
        Assert.assertEquals("shoud match ninchat PICK_UP message type", expectedPickUp, MessageTypes.PICK_UP)
        Assert.assertEquals("shoud match ninchat HANG_UP message type", expectedHangUp, MessageTypes.HANG_UP)
        Assert.assertEquals("shoud match ninchat WEBRTC_SERVERS_PARSED message type", expectedWebRTCServersParsed, MessageTypes.WEBRTC_SERVERS_PARSED)
        Assert.assertEquals("shoud match ninchat WEBRTC_MESSAGE_TYPES message type", expectedWebRTCMessageTypes, MessageTypes.WEBRTC_MESSAGE_TYPES)
    }

    @Test
    fun `should be webrtc message`() {
        val expectedICECandidate = "ninchat.com/rtc/ice-candidate"
        val expectedAnswers = "ninchat.com/rtc/answer"
        val expectedOffer = "ninchat.com/rtc/offer"
        val expectedCall = "ninchat.com/rtc/call"
        val expectedPickUp = "ninchat.com/rtc/pick-up"
        val expectedHangUp = "ninchat.com/rtc/hang-up"

        Assert.assertEquals("should be an webrtc message type", true, MessageTypes.webrtcMessage(expectedICECandidate))
        Assert.assertEquals("should be an webrtc message type", true, MessageTypes.webrtcMessage(expectedAnswers))
        Assert.assertEquals("should be an webrtc message type", true, MessageTypes.webrtcMessage(expectedOffer))
        Assert.assertEquals("should be an webrtc message type", true, MessageTypes.webrtcMessage(expectedCall))
        Assert.assertEquals("should be an webrtc message type", true, MessageTypes.webrtcMessage(expectedPickUp))
        Assert.assertEquals("should be an webrtc message type", true, MessageTypes.webrtcMessage(expectedHangUp))
    }

    @Test
    fun `empty string should not be webrtc message`() {
        Assert.assertEquals(false, MessageTypes.webrtcMessage(""))
    }

    @Test
    fun `null string should not be webrtc message`() {
        Assert.assertEquals(false, MessageTypes.webrtcMessage(null))
    }
}