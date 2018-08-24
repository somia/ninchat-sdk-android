package com.ninchat.sdk.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.ninchat.sdk.R;


public final class NinchatActivity extends BaseActivity {

    protected static final String CONFIGURATION_KEY = "configurationKey";
    protected static final String SITE_SECRET = "siteSecret";

    public static Intent getLaunchIntent(final Context context, final String configurationKey, final String siteSecret) {
        return new Intent(context, NinchatActivity.class)
                .putExtra(CONFIGURATION_KEY, configurationKey)
                .putExtra(SITE_SECRET, siteSecret);
    }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatQueueActivity.REQUEST_CODE && resultCode == RESULT_OK) {
            setResult(RESULT_OK, data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
