package com.ninchat.sdk.ninchatmedia.model

import org.junit.Assert
import org.junit.Test
import java.util.*

class NinchatFileTest {
    val messageId = "test-msg-id"
    val fileId = "test-fileId"
    val name = "test-name"
    val size = 1024
    val fileType = "video/test-video.mp4"
    val timestamp = 123456L
    val sender = "test-sender"
    val isRemote = false

    @Test
    fun `should create a ninchat file instance with given parameters`() {

        val ninchatFile = NinchatFile(
                messageId,
                fileId,
                name,
                size,
                fileType,
                timestamp,
                sender,
                isRemote
        )
        Assert.assertEquals(messageId, ninchatFile.messageId)
        Assert.assertEquals(fileId, ninchatFile.id)
        Assert.assertEquals(name, ninchatFile.name)
        Assert.assertEquals(true, ninchatFile.isVideo)
        Assert.assertEquals(timestamp, ninchatFile.timestamp)
        Assert.assertEquals(sender, ninchatFile.sender)
        Assert.assertEquals(isRemote, ninchatFile.isRemote)
    }

    @Test
    fun `should be able to set url`() {
        val ninchatFile = NinchatFile(
                messageId,
                fileId,
                name,
                size,
                fileType,
                timestamp,
                sender,
                isRemote
        )
        val changedUrl = "changed-url"
        ninchatFile.url = changedUrl
        Assert.assertEquals(changedUrl, ninchatFile.url)
    }

    @Test
    fun `should be able to set url expire data`() {
        val ninchatFile = NinchatFile(
                messageId,
                fileId,
                name,
                size,
                fileType,
                timestamp,
                sender,
                isRemote
        )
        val expiredDate = Date(123456)
        ninchatFile.urlExpiry = expiredDate
        Assert.assertEquals(expiredDate, ninchatFile.urlExpiry)
    }

    @Test
    fun `should be able to set width and height`() {
        val ninchatFile = NinchatFile(
                messageId,
                fileId,
                name,
                size,
                fileType,
                timestamp,
                sender,
                isRemote
        )
        val width = 320L
        val height = 280L
        ninchatFile.fileWidth = width
        ninchatFile.fileHeight = height
        Assert.assertEquals(width, ninchatFile.fileWidth)
        Assert.assertEquals(height, ninchatFile.fileHeight)
    }

    @Test
    fun `should set a file downloaded`() {
        val ninchatFile = NinchatFile(
                messageId,
                fileId,
                name,
                size,
                fileType,
                timestamp,
                sender,
                isRemote
        )
        Assert.assertEquals(false, ninchatFile.isDownloaded)
        ninchatFile.isDownloaded = true
        Assert.assertEquals(true, ninchatFile.isDownloaded)
    }

    @Test
    fun `should set a file downloadable`() {
        val ninchatFile = NinchatFile(
                messageId,
                fileId,
                name,
                size,
                fileType,
                timestamp,
                sender,
                isRemote
        )
        Assert.assertEquals(false, ninchatFile.isDownloadableFile)
        ninchatFile.isDownloadableFile = true
        Assert.assertEquals(true, ninchatFile.isDownloadableFile)
    }
}