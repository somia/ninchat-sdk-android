package com.ninchat.sdk.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.tasks.NinchatDownloadFileTask;

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
        NinchatDownloadFileTask.start(fileId);
    }

    private String fileId;

    private BroadcastReceiver fileDownloadedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.FILE_DOWNLOADED.equals(action)) {
                findViewById(R.id.ninchat_media_download).setVisibility(sessionManager.getFile(fileId).isDownloaded() ? View.GONE : View.VISIBLE);
            }
        }
    };

    private BroadcastReceiver fileDownloadingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (NinchatSessionManager.Broadcast.DOWNLOADING_FILE.equals(action)) {
                findViewById(R.id.ninchat_media_download).setVisibility(View.GONE);
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
            Glide.with(this)
                    .load(file.getUrl())
                    .into(image);
        }
        final TextView name = findViewById(R.id.ninchat_media_name);
        name.setText(file.getName());
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(fileDownloadedReceiver, new IntentFilter(NinchatSessionManager.Broadcast.FILE_DOWNLOADED));
        localBroadcastManager.registerReceiver(fileDownloadingReceiver, new IntentFilter(NinchatSessionManager.Broadcast.DOWNLOADING_FILE));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(fileDownloadedReceiver);
        localBroadcastManager.unregisterReceiver(fileDownloadingReceiver);
    }
}
