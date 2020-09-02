package com.ninchat.sdk.activities;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
abstract class NinchatBaseActivity extends Activity {

    protected static final int STORAGE_PERMISSION_REQUEST_CODE = "ExternalStorage".hashCode() & 0xffff;

    protected NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();

    abstract protected @LayoutRes int getLayoutRes();

    protected boolean allowBackButton() {
        return false;
    }

    protected boolean hasFileAccessPermissions() {
        return checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    protected void requestFileAccessPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
    }

    protected void showError(final @IdRes int layoutId, final @StringRes int message) {
        final TextView errorMessage = findViewById(R.id.error_message);
        errorMessage.setText(message);
        findViewById(R.id.error_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(layoutId).setVisibility(View.GONE);
            }
        });
        findViewById(layoutId).setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (allowBackButton()) {
            super.onBackPressed();
        }
    }
}
