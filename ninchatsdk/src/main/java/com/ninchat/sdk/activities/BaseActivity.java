package com.ninchat.sdk.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

import com.ninchat.sdk.R;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 17/08/2018.
 */
abstract class BaseActivity extends Activity {

    abstract protected @LayoutRes int getLayoutRes();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutRes());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    protected Intent getOnCloseData() {
        return null;
    }

    public void onCloseClick(final MenuItem menuItem) {
        setResult(Activity.RESULT_CANCELED, getOnCloseData());
        finish();
    }
}
