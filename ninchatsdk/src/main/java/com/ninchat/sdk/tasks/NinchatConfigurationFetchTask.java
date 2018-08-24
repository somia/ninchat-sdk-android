package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

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
public final class NinchatConfigurationFetchTask extends AsyncTask<Void, Void, String> {

    public interface Listener {
        void success(final String config);
        void failure(final Exception error);
    }

    public static void start(final Listener listener, final String configurationUrl) {
        new NinchatConfigurationFetchTask(listener, configurationUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected String configurationUrl;
    protected WeakReference<Listener> listenerWeakReference;
    protected Exception error;

    protected NinchatConfigurationFetchTask(final Listener listener, final String configurationUrl) {
        this.listenerWeakReference = new WeakReference<>(listener);
        this.configurationUrl = configurationUrl;
    }

    @Override
    protected String doInBackground(Void... voids) {
        URL url;
        try {
            url = new URL(configurationUrl);
        } catch (final MalformedURLException e) {
            this.error = e;
            return null;
        }
        HttpsURLConnection connection;
        try {
            connection = (HttpsURLConnection) url.openConnection();
        } catch (final IOException e) {
            this.error = e;
            return null;
        }
        InputStream inputStream;
        try {
            inputStream = new BufferedInputStream(connection.getInputStream());
        } catch (final IOException e) {
            connection.disconnect();
            this.error = e;
            return null;
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
            this.error = e;
            return null;
        }
        connection.disconnect();
        jsonDataBuilder.trimToSize();
        return jsonDataBuilder.toString();
    }

    @Override
    protected void onPostExecute(final String config) {
        final Listener listener = listenerWeakReference.get();
        if (listener != null) {
            if (config != null) {
                listener.success(config);
            } else {
                listener.failure(error);
            }
        }
    }

    private void clearBuffer(final byte[] buffer) {
        for (int i = 0; i < buffer.length; ++i) {
            buffer[i] = '\0';
        }
    }
}
