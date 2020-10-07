package com.ninchat.sdk.activities

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import com.ninchat.sdk.R
import org.jetbrains.annotations.TestOnly

abstract class NinchatBaseActivity : Activity() {
    @get:LayoutRes
    protected abstract val layoutRes: Int

    protected open fun allowBackButton(): Boolean {
        return false
    }

    protected fun hasFileAccessPermissions(): Boolean {
        return checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    protected fun requestFileAccessPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutRes)
    }

    protected fun showError(@IdRes layoutId: Int, @StringRes message: Int) {
        findViewById<TextView>(R.id.error_message)?.run {
            setText(message)
        }
        findViewById<View>(R.id.error_close)?.run {
            setOnClickListener {
                findViewById<View>(layoutId)?.run { visibility = View.GONE }
            }
        }
        findViewById<View>(layoutId)?.run { visibility = View.VISIBLE }
    }

    override fun onBackPressed() {
        if (allowBackButton()) {
            super.onBackPressed()
        }
    }

    companion object {
        @JvmField
        val STORAGE_PERMISSION_REQUEST_CODE = "ExternalStorage".hashCode() and 0xffff
    }
}