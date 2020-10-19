package com.ninchat.sdk.ninchatmedia.view

import android.app.DownloadManager
import android.os.Bundle
import android.view.View
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.ninchatmedia.model.NinchatMediaModel
import com.ninchat.sdk.ninchatmedia.presenter.INinchatMediaPresenter
import com.ninchat.sdk.ninchatmedia.presenter.NinchatMediaPresenter
import kotlinx.android.synthetic.main.activity_ninchat_media.*

class NinchatMediaActivity : NinchatBaseActivity(), INinchatMediaPresenter {
    override val layoutRes: Int
        get() = R.layout.activity_ninchat_media

    override fun allowBackButton(): Boolean {
        return true
    }

    // ninchat media presenter
    private val ninchatMediaPresenter = NinchatMediaPresenter(
            ninchatMediaModel = NinchatMediaModel(),
            callback = this,
            mContext = this
    )

    fun onToggleTopBar(view: View?) {
        ninchatMediaPresenter.toggleMediaTopBar(ninchat_media_top)
    }

    fun onClose(view: View?) {
        finish()
    }

    fun onDownloadFile(view: View?) {
        if (hasFileAccessPermissions()) {
            ninchatMediaPresenter
                    .downloadFile(ninchat_media_download, getSystemService(DOWNLOAD_SERVICE) as DownloadManager)
        } else {
            requestFileAccessPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (hasFileAccessPermissions()) {
                ninchatMediaPresenter
                        .downloadFile(ninchat_media_download, getSystemService(DOWNLOAD_SERVICE) as DownloadManager)
            } else {
                showError(R.id.ninchat_media_error, R.string.ninchat_chat_error_no_file_permissions)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ninchatMediaPresenter.updateFileId(intent = intent)
        ninchatMediaPresenter.ninchatMediaModel.getFile()?.let { ninchatFile ->
            if (ninchatFile.isVideo) {
                ninchatMediaPresenter.playVideo(ninchat_media_video, ninchatFile.url)
            } else {
                GlideWrapper.loadImage(this, ninchatFile.url, ninchat_media_image)
            }
            ninchat_media_name.text = ninchatFile.name
            ninchat_media_download.visibility = if (ninchatFile.isDownloaded || ninchatFile.isVideo) View.GONE else View.VISIBLE
        }
        ninchatMediaPresenter.subscribeBroadcaster()
    }

    override fun onDestroy() {
        ninchatMediaPresenter.unSubscribeBroadcaster()
        super.onDestroy()
    }

    override fun onDownloadComplete() {
        ninchat_media_download.visibility = if (ninchatMediaPresenter.ninchatMediaModel.isDownloaded()) View.GONE else View.VISIBLE
    }

    // helper class for test
    internal fun getMediaPresenter() = ninchatMediaPresenter
}