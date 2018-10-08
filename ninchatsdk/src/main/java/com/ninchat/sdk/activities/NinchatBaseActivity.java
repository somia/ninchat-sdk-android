package com.ninchat.sdk.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
abstract class NinchatBaseActivity extends Activity {

    abstract protected @LayoutRes int getLayoutRes();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutRes());
    }

    @Override
    public void onBackPressed() {
        // Ignore back press, let's not navigate back
    }
}
