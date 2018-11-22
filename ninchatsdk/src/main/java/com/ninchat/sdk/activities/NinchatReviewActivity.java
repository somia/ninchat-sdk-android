package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;

public final class NinchatReviewActivity extends NinchatBaseActivity {

    static final int REQUEST_CODE = NinchatReviewActivity.class.hashCode() & 0xffff;

    static Intent getLaunchIntent(final Context context) {
        return new Intent(context, NinchatReviewActivity.class);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_review;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TextView title = findViewById(R.id.ninchat_review_title);
        title.setText(sessionManager.getFeedbackTitle());
        final TextView positive = findViewById(R.id.ninchat_review_positive);
        positive.setText(sessionManager.getFeedbackPositive());
        final TextView neutral = findViewById(R.id.ninchat_review_neutral);
        neutral.setText(sessionManager.getFeedbackNeutral());
        final TextView negative = findViewById(R.id.ninchat_review_negative);
        negative.setText(sessionManager.getFeedbackNegative());
        final TextView skip = findViewById(R.id.ninchat_review_skip);
        skip.setText(sessionManager.getFeedbackSkip());
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

    private void close(final int rating) {
        if (rating != NinchatSession.Analytics.Rating.NO_ANSWER) {
            sessionManager.sendRating(rating);
        }
        setResult(RESULT_OK, getResultIntent(rating));
        finish();
    }

    private Intent getResultIntent(final int rating) {
        return new Intent().putExtra(NinchatSession.Analytics.Keys.RATING, rating);
    }

}
