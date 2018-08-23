package com.ninchat.sdk.activities;

import com.ninchat.sdk.R;

public class NinchatReviewActivity extends BaseActivity {

    static final int REQUEST_CODE = NinchatReviewActivity.class.hashCode() & 0xffff;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_review;
    }
}
