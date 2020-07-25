package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSession;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaires;

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
        final boolean conversationLikeQuestionnaire = isConversationLikeQuestionnaire();
        if (conversationLikeQuestionnaire) {
            handleConversationView();
        } else {
            handleFormView();
        }
    }

    private void handleConversationView() {
        findViewById(R.id.review_rating_normal_view).setVisibility(View.GONE);

        final View botViewItem = findViewById(R.id.review_rating_bot_view);
        botViewItem.setVisibility(View.VISIBLE);

        botViewItem.findViewById(R.id.ninchat_bot_rating_text_root_view).setVisibility(View.GONE);
        botViewItem.findViewById(R.id.ninchat_bot_ratings_icon_items_root_view).setVisibility(View.GONE);

        findViewById(R.id.ninchat_review_activity).setBackground(
                ContextCompat.getDrawable(getApplicationContext(),
                        R.drawable.ninchat_chat_background_tiled));

        ((TextView) botViewItem.findViewById(R.id.ninchat_chat_message_bot_text)).setText("LightbotAgent");
        ImageView mImageView = botViewItem.findViewById(R.id.ninchat_chat_message_bot_writing);
        mImageView.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator);
        AnimationDrawable animationDrawable = (AnimationDrawable) mImageView.getBackground();
        animationDrawable.start();
        new Handler().postDelayed(() -> {
            animationDrawable.stop();
            botViewItem.findViewById(R.id.ninchat_chat_message_bot_writing_review_root).setVisibility(View.GONE);
            botViewItem.findViewById(R.id.ninchat_bot_rating_text_root_view).setVisibility(View.VISIBLE);
            botViewItem.findViewById(R.id.ninchat_bot_ratings_icon_items_root_view).setVisibility(View.VISIBLE);

            // change background to chat like
            botViewItem.findViewById(R.id.ninchat_bot_rating_text_root_view).setBackground(ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ninchat_chat_bubble_left_repeated));

            final TextView title = botViewItem.findViewById(R.id.ninchat_review_title);
            title.setGravity(Gravity.START);
            title.setText(sessionManager.getThankYouText());


            final TextView description = botViewItem.findViewById(R.id.ninchat_review_description);
            description.setVisibility(View.VISIBLE);
            description.setGravity(Gravity.START);
            description.setText(sessionManager.getFeedbackTitle());


            final TextView positive = botViewItem.findViewById(R.id.ninchat_review_positive);
            positive.setText(sessionManager.getFeedbackPositive());
            final TextView neutral = botViewItem.findViewById(R.id.ninchat_review_neutral);
            neutral.setText(sessionManager.getFeedbackNeutral());
            final TextView negative = botViewItem.findViewById(R.id.ninchat_review_negative);
            negative.setText(sessionManager.getFeedbackNegative());
            final TextView skip = findViewById(R.id.ninchat_review_skip);
            skip.setText(sessionManager.getFeedbackSkip());
        }, 1500);
    }

    private void handleFormView() {
        findViewById(R.id.review_rating_bot_view).setVisibility(View.GONE);
        final View formViewItem = findViewById(R.id.review_rating_normal_view);
        formViewItem.setVisibility(View.VISIBLE);
        final TextView title = formViewItem.findViewById(R.id.ninchat_review_title);
        title.setText(sessionManager.getFeedbackTitle());
        final TextView positive = formViewItem.findViewById(R.id.ninchat_review_positive);
        positive.setText(sessionManager.getFeedbackPositive());
        final TextView neutral = formViewItem.findViewById(R.id.ninchat_review_neutral);
        neutral.setText(sessionManager.getFeedbackNeutral());
        final TextView negative = formViewItem.findViewById(R.id.ninchat_review_negative);
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

    // check if post audience questionnaire is conversation like questionnaire
    private boolean isConversationLikeQuestionnaire() {
        NinchatQuestionnaires questionnaires = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires();
        return questionnaires.conversationLikePostAudienceQuestionnaire();
    }


    private Intent getResultIntent(final int rating) {
        return new Intent().putExtra(NinchatSession.Analytics.Keys.RATING, rating);
    }

}
