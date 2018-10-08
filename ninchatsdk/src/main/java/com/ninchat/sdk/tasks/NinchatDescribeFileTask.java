package com.ninchat.sdk.tasks;

import android.os.AsyncTask;

import com.ninchat.client.Props;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.NinchatFile;

import java.util.Date;

public final class NinchatDescribeFileTask extends NinchatBaseTask {


    public static final void start(final String fileId) {
        new NinchatDescribeFileTask(fileId).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String fileId;

    private NinchatDescribeFileTask(final String fileId) {
        this.fileId = fileId;
    }

    @Override
    protected Exception doInBackground(Void... voids) {
        final NinchatFile file = NinchatSessionManager.getInstance().getFile(fileId);
        if (file.getUrl() == null || file.getUrlExpiry() == null || file.getUrlExpiry().before(new Date())) {
            final Props props = new Props();
            props.setString("action", "describe_file");
            props.setString("file_id", fileId);
            try {
                NinchatSessionManager.getInstance().getSession().send(props, null);
            } catch (final Exception e) {
                return e;
            }
        }
        return null;
    }
}
