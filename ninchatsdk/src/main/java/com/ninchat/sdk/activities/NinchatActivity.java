package com.ninchat.sdk.activities;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.ninchat.sdk.R;


public class NinchatActivity extends BaseActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat;
    }

    public void onBlogLinkClick(final View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.ninchat.com")));
    }

    public void onStartButtonClick(final View view) {
        startActivityForResult(new Intent(this, NinchatQueueActivity.class), NinchatQueueActivity.REQUEST_CODE);
    }

}
