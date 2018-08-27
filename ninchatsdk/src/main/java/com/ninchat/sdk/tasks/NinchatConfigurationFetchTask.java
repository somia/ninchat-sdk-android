package com.ninchat.sdk.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ninchat.sdk.NinchatSessionManager;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 23/08/2018.
 */
public final class NinchatConfigurationFetchTask extends AsyncTask<Void, Void, Exception> {

    public static void start(final Context context, final String configurationUrl) {
        new NinchatConfigurationFetchTask(context, configurationUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected static final String TAG = NinchatConfigurationFetchTask.class.getSimpleName();
    protected static final int TIMEOUT = 10000;

    protected WeakReference<Context> contextWeakReference;
    protected String configurationUrl;

    protected NinchatConfigurationFetchTask(final Context context, final String configurationUrl) {
        this.contextWeakReference = new WeakReference<>(context);
        this.configurationUrl = configurationUrl;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        URL url;
        try {
            url = new URL(configurationUrl);
        } catch (final MalformedURLException e) {
            Log.e(TAG, "URL error", e);
            return e;
        }
        HttpsURLConnection connection;
        try {
            connection = (HttpsURLConnection) url.openConnection();
        } catch (final IOException e) {
            Log.e(TAG, "Connection error", e);
            return e;
        }
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        int responseCode;
        String responseMessage;
        try {
            responseCode = connection.getResponseCode();
            responseMessage = connection.getResponseMessage();
        } catch (final IOException e) {
            connection.disconnect();
            Log.e(TAG, "Connection error", e);
            return e;
        }
        if (responseCode != HttpsURLConnection.HTTP_OK) {
            connection.disconnect();
            return new Exception(configurationUrl + " " + responseMessage);
        }
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (final IOException e) {
            connection.disconnect();
            Log.e(TAG, "Connection error", e);
            return e;
        }
        final StringBuilder jsonDataBuilder = new StringBuilder();
        final byte[] buffer = new byte[1024];
        clearBuffer(buffer);
        try {
            int numberOfBytes = inputStream.read(buffer);
            while (numberOfBytes > 0) {
                jsonDataBuilder.append(new String(buffer, 0, numberOfBytes));
                clearBuffer(buffer);
                numberOfBytes = inputStream.read(buffer);
            }
        } catch (final IOException e) {
            connection.disconnect();
            Log.e(TAG, "Connection error", e);
            return e;
        }
        connection.disconnect();
        jsonDataBuilder.trimToSize();
        try {
            NinchatSessionManager.getInstance().setConfiguration(jsonDataBuilder.toString());
        } catch (final JSONException e) {
            Log.e(TAG, "Configuration parsing error", e);
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Exception error) {
        final Context context = contextWeakReference.get();
        if (context != null && error != null) {
            LocalBroadcastManager.getInstance(context)
                    .sendBroadcast(new Intent(NinchatSessionManager.CONFIGURATION_FETCH_ERROR)
                            .putExtra(NinchatSessionManager.CONFIGURATION_FETCH_ERROR_REASON, error));
        }
    }

    private void clearBuffer(final byte[] buffer) {
        for (int i = 0; i < buffer.length; ++i) {
            buffer[i] = '\0';
        }
    }
}
