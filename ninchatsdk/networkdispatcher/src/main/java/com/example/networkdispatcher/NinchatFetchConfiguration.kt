package com.example.networkdispatcher

import android.annotation.SuppressLint
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        const val TIMEOUT_MS = 10000

        @SuppressLint("LongLogTag")
        suspend fun execute(serverAddress: String? = null,
                            configurationKey: String? = null): String =
                withContext(Dispatchers.IO) {
                    val configurationUrl = "https://$serverAddress/config/$configurationKey"
                    Log.i(TAG, "Fetching configuration...")
                    val url: URL? = try {
                        URL(configurationUrl)
                    } catch (e: MalformedURLException) {
                        throw e
                    }

                    val connection: HttpsURLConnection? = try {
                        url?.openConnection() as HttpsURLConnection
                    } catch (e: IOException) {
                        throw e
                    }
                    connection?.connectTimeout = TIMEOUT_MS
                    connection?.readTimeout = TIMEOUT_MS
                    var responseCode: Int? = -1
                    var responseMessage: String? = null
                    try {
                        responseCode = connection?.responseCode
                        responseMessage = connection?.responseMessage
                    } catch (e: IOException) {
                        connection?.disconnect()
                        throw e
                    }
                    if (responseCode != HttpsURLConnection.HTTP_OK) {
                        connection?.disconnect()
                    }

                    val inputStream: InputStream? = try {
                        BufferedInputStream(connection?.inputStream)
                    } catch (e: IOException) {
                        connection?.disconnect()
                        throw e
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
                        throw e
                    }
                    connection?.disconnect()
                    jsonDataBuilder.trimToSize()

                    val retval = try {
                        jsonDataBuilder.toString()
                    } catch (e: JSONException) {
                        throw e
                    }
                    retval
                }

        private fun clearBuffer(buffer: ByteArray) {
            for (i in buffer.indices) {
                buffer[i] = '\u0000'.toByte()
            }
        }
    }
}