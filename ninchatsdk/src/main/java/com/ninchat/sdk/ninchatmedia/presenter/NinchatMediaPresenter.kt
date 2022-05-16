package com.ninchat.sdk.ninchatmedia.presenter

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.MediaController
import android.widget.VideoView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ninchat.sdk.ninchatmedia.model.NinchatMediaModel
import com.ninchat.sdk.ninchatmedia.view.NinchatMediaActivity


interface INinchatMediaPresenter {
    fun onDownloadComplete()
}

interface INinchatMediaCallback {
    fun onLoadError()
    fun onLoadSuccess()
}

class NinchatMediaPresenter(
        val ninchatMediaModel: NinchatMediaModel,
        val callback: INinchatMediaPresenter?,
        val mContext: Context,
) {

    fun updateFileId(intent: Intent?) {
        intent?.getStringExtra(NinchatMediaModel.FILE_ID)?.let {
            ninchatMediaModel.fileId = it
        }
    }

    fun toggleMediaTopBar(view: View) {
        view.visibility = if (view.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    fun downloadFile(view: View, downloadManager: DownloadManager?) {
        val downloadRequest = ninchatMediaModel.getFile()?.let { ninchatFile ->
            val uri = Uri.parse(ninchatFile.url)
            val request = DownloadManager.Request(uri).apply {
                setTitle(ninchatFile.name)
                setDescription(ninchatFile.url)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, ninchatFile.name)
                allowScanningByMediaScanner()
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
            request
        }
        downloadManager?.enqueue(downloadRequest)
        view.visibility = View.GONE
    }

    fun playVideo(view: VideoView, url: String?) {
        val mediaController = MediaController(mContext)
        mediaController.setAnchorView(view)
        mediaController.setMediaPlayer(view)
        view.visibility = View.VISIBLE
        view.setMediaController(mediaController)
        view.setVideoPath(url)
        view.start()
    }

    fun subscribeBroadcaster() {
        LocalBroadcastManager.getInstance(mContext).run {
            registerReceiver(fileDownloadedReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    fun unSubscribeBroadcaster() {
        LocalBroadcastManager.getInstance(mContext).run {
            unregisterReceiver(fileDownloadedReceiver)
        }
    }

    private val fileDownloadedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent?.action) {
                callback?.onDownloadComplete()
            }
        }
    }

    val getLoadingText = ninchatMediaModel.translate()

    companion object {
        @JvmStatic
        fun getLaunchIntent(context: Context?, fileId: String): Intent {
            return Intent(context, NinchatMediaActivity::class.java).run {
                putExtra(NinchatMediaModel.FILE_ID, fileId)
            }
        }
    }
}