package com.ninchat.sdk.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
abstract class NinchatBaseActivity extends Activity {

    protected NinchatSessionManager sessionManager = NinchatSessionManager.getInstance();

    abstract protected @LayoutRes int getLayoutRes();

    protected boolean allowBackButton() {
        return false;
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
