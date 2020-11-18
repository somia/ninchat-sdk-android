package com.ninchat.sdk.ninchatdb.light

import android.content.Context

class NinchatPersistenceStore {
    companion object {
        private const val storeName = "NINCHAT_LIGHT_STORAGE"
        fun save(key: String, value: String, appContext: Context?) {
            appContext?.let { context ->
                val sharedPref = context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString(key, value)
                    apply()
                }
            }
        }

        fun remove(key: String, appContext: Context?) {
            appContext?.let { context ->
                val sharedPref = context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    remove(key)
                    apply()
                }
            }
        }

        fun get(key: String, appContext: Context?): String? {
            return appContext?.let { context ->
                val sharedPref = context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
                sharedPref.getString(key, null)
            }
        }

        fun has(key: String, appContext: Context?): Boolean {
            return appContext?.let { context ->
                val sharedPref = context.getSharedPreferences(storeName, Context.MODE_PRIVATE)
                !sharedPref.getString(key, null).isNullOrEmpty()
            } ?: false
        }
    }
}