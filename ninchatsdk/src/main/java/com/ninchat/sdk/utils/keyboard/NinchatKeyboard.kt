package com.ninchat.sdk.utils.keyboard

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import timber.log.Timber

fun Activity.hideKeyBoard() {
    try {
        val focusView = currentFocus
        focusView?.let {
            (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                it.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    } catch (e: Exception) {
        Timber.d(e)
    }
}


fun Activity.hideKeyBoardForce() {
    try {
        val focusView = currentFocus
        focusView?.let {
            (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
                it.windowToken,
                0
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.hideKeyBoard() {
    try {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
            windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.hideKeyBoardForce() {
    try {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.hideSoftInputFromWindow(
            windowToken,
            0
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}