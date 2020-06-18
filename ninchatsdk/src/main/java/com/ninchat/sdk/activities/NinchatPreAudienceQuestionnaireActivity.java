package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatSimpleFormLikeQuestionnaireAdapter;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;


public final class NinchatPreAudienceQuestionnaireActivity extends NinchatBaseActivity {
    private final String TAG = NinchatPreAudienceQuestionnaireActivity.class.getSimpleName();
    public static final int REQUEST_CODE = NinchatPreAudienceQuestionnaireActivity.class.hashCode() & 0xffff;
    protected static final String QUEUE_ID = "queueId";
    private String queueId;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_form_questionnaire;
    }

    public static Intent getLaunchIntent(final Context context, final String queueId) {
        return new Intent(context, NinchatPreAudienceQuestionnaireActivity.class).putExtra(QUEUE_ID, queueId);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        queueId = intent.getStringExtra(QUEUE_ID);

        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.questionnaire_form_rview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final NinchatSimpleFormLikeQuestionnaireAdapter mPreAudienceQuestionnaireAdapter = new NinchatSimpleFormLikeQuestionnaireAdapter(
                NinchatSessionManager.getInstance().getNinchatQuestionnaire().getNinchatPreAudienceQuestionnaire(), new NinchatSimpleFormLikeQuestionnaireAdapter.Callback() {
            @Override
            public void onError(int position) {
                mRecyclerView.smoothScrollToPosition(position);
                close();
            }

            @Override
            public void onComplete() {
                close();
            }
        }
        );
        final int spaceInPixel = getResources().getDimensionPixelSize(R.dimen.items_margin_top);
        mRecyclerView.addItemDecoration(new NinchatQuestionnaireItemDecoration(spaceInPixel));
        mRecyclerView.setAdapter(mPreAudienceQuestionnaireAdapter);
        mRecyclerView.setItemViewCacheSize(mPreAudienceQuestionnaireAdapter.getItemCount());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClose(final View view) {
        // todo add result
        setResult(RESULT_CANCELED, null);
        finish();
    }

    private void close() {
        setResult(RESULT_OK, new Intent().putExtra(NinchatPreAudienceQuestionnaireActivity.QUEUE_ID, queueId));
        finish();
    }
}
