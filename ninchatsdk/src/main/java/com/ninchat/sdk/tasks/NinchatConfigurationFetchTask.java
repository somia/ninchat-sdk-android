package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

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

    public static void start(final NinchatSessionManager.ConfigurationFetchListener listener, final String configurationUrl) {
        new NinchatConfigurationFetchTask(listener, configurationUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected static final int TIMEOUT = 15000;

    protected WeakReference<NinchatSessionManager.ConfigurationFetchListener> listenerWeakReference;
    protected String configurationUrl;

    protected NinchatConfigurationFetchTask(final NinchatSessionManager.ConfigurationFetchListener listener, final String configurationUrl) {
        this.listenerWeakReference = new WeakReference<>(listener);
        this.configurationUrl = configurationUrl;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        URL url;
        try {
            url = new URL(configurationUrl);
        } catch (final MalformedURLException e) {
            return e;
        }
        HttpsURLConnection connection;
        try {
            connection = (HttpsURLConnection) url.openConnection();
        } catch (final IOException e) {
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
            return e;
        }
        connection.disconnect();
        jsonDataBuilder.trimToSize();
        try {
            NinchatSessionManager.setConfiguration(jsonDataBuilder.toString());
        } catch (final JSONException e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Exception error) {
        final NinchatSessionManager.ConfigurationFetchListener listener = listenerWeakReference.get();
        if (listener != null) {
            if (error != null) {
                listener.failure(error);
            } else {
                listener.success();
            }
        }
    }

    private void clearBuffer(final byte[] buffer) {
        for (int i = 0; i < buffer.length; ++i) {
            buffer[i] = '\0';
        }
    }
}
