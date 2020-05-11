package com.ninchat.sdk.models

import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito.mock
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
        ninchatFile.setWidth(width)
        ninchatFile.setHeight(height)
        Assert.assertEquals(Math.toIntExact(width), ninchatFile.width)
        Assert.assertEquals(Math.toIntExact(height), ninchatFile.height)
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
        ninchatFile.setDownloaded()
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
        ninchatFile.setDownloadableFile(true)
        Assert.assertEquals(true, ninchatFile.isDownloadableFile)
    }
}