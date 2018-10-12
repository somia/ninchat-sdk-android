package com.ninchat.sdk.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.NinchatFile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public final class NinchatDownloadFileTask extends NinchatBaseTask {

    public static void start(final String fileId) {
        new NinchatDownloadFileTask(fileId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String fileId;

    private NinchatDownloadFileTask(final String fileId) {
        this.fileId = fileId;
    }

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 120000;

    @Override
    protected Exception doInBackground(Void... voids) {
        try {
            final Context context = NinchatSessionManager.getInstance().getContext();
            if (context == null) {
                return null;
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSessionManager.Broadcast.DOWNLOADING_FILE));
            final NinchatFile ninchatFile = NinchatSessionManager.getInstance().getFile(fileId);
            final URL url = new URL(ninchatFile.getUrl());
            final File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            final File file = new File(directory, ninchatFile.getName());
            final byte[] buffer = new byte[1024];
            final DataInputStream inputStream = new DataInputStream(url.openStream());
            final DataOutputStream fileOutputStream = new DataOutputStream(new FileOutputStream(file));
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, len);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            ninchatFile.setDownloaded();
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSessionManager.Broadcast.FILE_DOWNLOADED));
        } catch (final Exception e) {
            final Context context = NinchatSessionManager.getInstance().getContext();
            if (context != null) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(NinchatSessionManager.Broadcast.FILE_DOWNLOADED));
            }
        }
        return null;
    }
}
