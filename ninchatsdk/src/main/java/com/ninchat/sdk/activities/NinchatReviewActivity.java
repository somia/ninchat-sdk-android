package com.ninchat.sdk.activities;

import android.content.Intent;
import android.view.View;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.R;

public final class NinchatReviewActivity extends BaseActivity {

    static final int REQUEST_CODE = NinchatReviewActivity.class.hashCode() & 0xffff;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_review;
    }

    public final void onGoodClick(final View view) {
        close(NinchatSession.Analytics.Rating.GOOD);
    }

    public final void onFairClick(final View view) {
        close(NinchatSession.Analytics.Rating.FAIR);
    }

    public final void onPoorClick(final View view) {
        close(NinchatSession.Analytics.Rating.POOR);
    }

    public final void onSkipClick(final View view) {
        close(NinchatSession.Analytics.Rating.NO_ANSWER);
    }

    @Override
    protected Intent getOnCloseData() {
        return getResultIntent(NinchatSession.Analytics.Rating.NO_ANSWER);
    }

    private void close(final int rating) {
        setResult(RESULT_OK, getResultIntent(rating));
        finish();
    }

    private Intent getResultIntent(final int rating) {
        return new Intent().putExtra(NinchatSession.Analytics.Keys.RATING, rating);
    }

}
