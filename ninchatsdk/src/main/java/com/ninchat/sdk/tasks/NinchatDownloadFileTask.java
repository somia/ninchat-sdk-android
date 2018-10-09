package com.ninchat.sdk.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.NinchatFile;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

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
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            final File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            final File file = new File(directory, ninchatFile.getName());
            final FileOutputStream fos = new FileOutputStream(file);
            if (ninchatFile.isImage()) {
                final Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
                MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), "");
                ninchatFile.setDownloaded();
            }
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
