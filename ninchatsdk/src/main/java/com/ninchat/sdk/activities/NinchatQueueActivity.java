package com.ninchat.sdk.activities;

import com.ninchat.sdk.R;

/**
 * Created by Jussi Pekonen (jussi.pekonen@qvik.fi) on 22/08/2018.
 */
public class NinchatQueueActivity extends BaseActivity {

    static final int REQUEST_CODE = NinchatQueueActivity.class.hashCode() & 0xffff;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_queue;
    }
}
