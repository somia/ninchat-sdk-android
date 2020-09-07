package com.example.networkdispatcher

import android.annotation.SuppressLint
import android.util.Log
import com.ninchat.client.Session
import org.json.JSONException
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NinchatFetchConfiguration {
    companion object {
        const val TAG = "NinchatFetchConfiguration"
        const val TIMEOUT = 10000

        @SuppressLint("LongLogTag")
        fun execute(currentSession: Session? = null,
                    serverAddress: String? = null,
                    configurationKey: String? = null
        ) {
            val configurationUrl = "https://$serverAddress/config/$configurationKey"
            Log.i(TAG, "Fetching configuration...")
            val url: URL? = try {
                URL(configurationUrl)
            } catch (e: MalformedURLException) {
                Log.e(TAG, "URL error", e)
                null
            }

            val connection: HttpsURLConnection? = try {
                url?.openConnection() as HttpsURLConnection
            } catch (e: IOException) {
                Log.e(TAG, "Connection error", e)
                null
            }
            connection?.connectTimeout = TIMEOUT
            connection?.readTimeout = TIMEOUT
            var responseCode: Int? = -1
            var responseMessage: String? = null
            try {
                responseCode = connection?.responseCode
                responseMessage = connection?.responseMessage
            } catch (e: IOException) {
                connection?.disconnect()
                Log.e(TAG, "Connection error", e)
            }
            if (responseCode != HttpsURLConnection.HTTP_OK) {
                connection?.disconnect()
            }

            val inputStream: InputStream? = try {
                BufferedInputStream(connection?.inputStream)
            } catch (e: IOException) {
                connection?.disconnect()
                Log.e(TAG, "Connection error", e)
                null
            }
            val jsonDataBuilder = StringBuilder()
            val buffer = ByteArray(1024)
            clearBuffer(buffer)
            try {
                var numberOfBytes = inputStream?.read(buffer)
                while (numberOfBytes != null && numberOfBytes > 0) {
                    jsonDataBuilder.append(String(buffer, 0, numberOfBytes))
                    clearBuffer(buffer)
                    numberOfBytes = inputStream?.read(buffer)
                }
            } catch (e: IOException) {
                connection?.disconnect()
                Log.e(TAG, "Connection error", e)
            }
            connection?.disconnect()
            jsonDataBuilder.trimToSize()
            try {
                // NinchatSessionManager.getInstance().setConfiguration(jsonDataBuilder.toString())
                // todo (pallab) send event that configuration needs to be set
            } catch (e: JSONException) {
                Log.e(TAG, "Configuration parsing error", e)
            }
        }

        private fun clearBuffer(buffer: ByteArray) {
            for (i in buffer.indices) {
                buffer[i] = '\u0000'.toByte()
            }
        }
    }
}