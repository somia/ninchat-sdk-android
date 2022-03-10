package com.ninchat.sdk.ninchatmedia.view

import android.app.DownloadManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.ninchat.sdk.R
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import com.ninchat.sdk.ninchatmedia.model.NinchatMediaModel
import com.ninchat.sdk.ninchatmedia.presenter.INinchatMediaCallback
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
                ninchat_loading_image_preview.visibility = View.VISIBLE
                ninchat_loading_image_preview_text.text = ninchatMediaPresenter.getLoadingText
                val spinner = animateSpinner()

                GlideWrapper.loadImage(this, ninchatFile.url, ninchat_media_image, object : INinchatMediaCallback {
                    override fun onLoadError() {
                        spinner?.cancel()
                        showError(R.id.ninchat_media_error, R.string.ninchat_chat_error_downloading_preview)
                    }

                    override fun onLoadSuccess() {
                        spinner?.cancel()
                        ninchat_loading_image_preview.visibility = View.GONE
                    }
                })
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


    private fun animateSpinner(): RotateAnimation? {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val spinnerAnimation = RotateAnimation(0f, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                interpolator = LinearInterpolator()
                repeatCount = Animation.INFINITE
                duration = 3000
            }
            val spinner: ImageView = ninchat_loading_image_preview_spinner.apply {
                visibility = View.VISIBLE
                animation = spinnerAnimation
            }
            return spinnerAnimation
        }
        return null
    }

}