package com.ninchat.sdk.utils.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.ninchat.sdk.activities.NinchatBaseActivity
import com.ninchat.sdk.ninchatchatactivity.presenter.NinchatChatPresenter

class NinchatPermission {
    companion object {

        @JvmStatic
        fun hasFileAccessPermissions(mContext: Context): Boolean {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            val permissionPassed = permissions.all {
                mContext.checkCallingOrSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
            return permissionPassed
        }

        @JvmStatic
        fun hasVideoCallPermissions(mContext: Context): Boolean {
            val permissions = listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
            val permissionPassed = permissions.all {
                mContext.checkCallingOrSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
            return permissionPassed
        }

        @JvmStatic
        fun requestFileAccessPermissions(mActivity: Activity) {
            val permissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
                permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            mActivity.requestPermissions(
                permissions.toTypedArray(),
                NinchatBaseActivity.STORAGE_PERMISSION_REQUEST_CODE
            )
        }

        @JvmStatic
        fun requestAudioVideoPermissions(mActivity: Activity) {
            val permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            mActivity.requestPermissions(
                permissions.toTypedArray(),
                NinchatChatPresenter.CAMERA_AND_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }
}