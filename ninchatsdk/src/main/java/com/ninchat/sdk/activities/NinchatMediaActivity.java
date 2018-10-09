package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ninchat.sdk.NinchatSessionManager;
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

    public void onToggleTopBar(final View view) {
        final View top = findViewById(R.id.ninchat_media_top);
        top.setVisibility(top.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
    }

    public void onClose(final View view) {
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String fileId = getIntent().getStringExtra(FILE_ID);
        final NinchatFile file = NinchatSessionManager.getInstance().getFile(fileId);
        final ImageView image = findViewById(R.id.ninchat_media_image);
        Glide.with(this)
                .load(file.getUrl())
                .into(image);
        final TextView name = findViewById(R.id.ninchat_media_name);
        name.setText(file.getName());
    }
}
