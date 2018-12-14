package com.ninchat.sdk.activities;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatFile;

public final class NinchatMediaActivity extends NinchatBaseActivity {

    protected static final String FILE_ID = "fileId";

    public static Intent getLaunchIntent(final Context context, final String fileId) {
        return new Intent(context, NinchatMediaActivity.class).putExtra(FILE_ID, fileId);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_media;
    }

    @Override
    protected boolean allowBackButton() {
        return true;
    }

    public void onToggleTopBar(final View view) {
        final View top = findViewById(R.id.ninchat_media_top);
        top.setVisibility(top.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    public void onClose(final View view) {
        finish();
    }

    public void onDownloadFile(final View view) {
        if (hasFileAccessPermissions()) {
            downloadFile();
        } else {
            requestFileAccessPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (hasFileAccessPermissions()) {
                downloadFile();
            } else {
                showError(R.id.ninchat_media_error, R.string.ninchat_chat_error_no_file_permissions);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void downloadFile() {
        final NinchatFile file = sessionManager.getFile(fileId);
        final Uri uri = Uri.parse(file.getUrl());
        final DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(file.getName());
        request.setDescription(file.getUrl());
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, file.getName());
        final DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
        findViewById(R.id.ninchat_media_download).setVisibility(View.GONE);
    }

    private String fileId;

    private BroadcastReceiver fileDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                findViewById(R.id.ninchat_media_download).setVisibility(sessionManager.getFile(fileId).isDownloaded() ? View.GONE : View.VISIBLE);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileId = getIntent().getStringExtra(FILE_ID);
        final NinchatFile file = sessionManager.getFile(fileId);
        findViewById(R.id.ninchat_media_download).setVisibility(file.isDownloaded() ? View.GONE : View.VISIBLE);
        if (file.isVideo()) {
            findViewById(R.id.ninchat_media_image).setVisibility(View.GONE);
            final VideoView video = findViewById(R.id.ninchat_media_video);
            MediaController mediaController = new MediaController(this);
            mediaController.setAnchorView(video);
            mediaController.setMediaPlayer(video);
            video.setVisibility(View.VISIBLE);
            video.setMediaController(mediaController);
            video.setVideoPath(file.getUrl());
            video.start();
        } else {
            final ImageView image = findViewById(R.id.ninchat_media_image);
            GlideApp.with(this)
                    .load(file.getUrl())
                    .into(image);
        }
        if (file.isDownloaded()) {
            findViewById(R.id.ninchat_media_download).setVisibility(View.GONE);
        }
        final TextView name = findViewById(R.id.ninchat_media_name);
        name.setText(file.getName());
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(fileDownloadedReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(fileDownloadedReceiver);
    }
}
