package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.ninchat.sdk.R;


public final class NinchatActivity extends BaseActivity {

    protected static final String SHOW_LAUNCHER = "showLauncher";

    public static Intent getLaunchIntent(final Context context, final boolean showLauncher) {
        return new Intent(context, NinchatActivity.class)
                .putExtra(SHOW_LAUNCHER, showLauncher);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat;
    }

    protected boolean showLauncher = true;

    @Override
    protected void handleOnCreateIntent(Intent intent) {
        showLauncher = intent.getBooleanExtra(SHOW_LAUNCHER, true);
        if (!showLauncher) {
            openQueueActivity();
        }
    }

    public void onBlogLinkClick(final View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://blog.ninchat.com")));
    }

    public void onStartButtonClick(final View view) {
       openQueueActivity();
    }

    private void openQueueActivity() {
        startActivityForResult(NinchatQueueActivity.getLaunchIntent(this), NinchatQueueActivity.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatQueueActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK || !showLauncher) {
                setResult(resultCode, data);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
