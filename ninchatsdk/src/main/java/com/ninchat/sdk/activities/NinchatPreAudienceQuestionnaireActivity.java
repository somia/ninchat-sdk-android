package com.ninchat.sdk.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatComplexFormLikeQuestionnaireAdapter;
import com.ninchat.sdk.adapters.NinchatSimpleFormLikeQuestionnaireAdapter;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;

import static com.ninchat.sdk.activities.NinchatComplexQuestionnaireActivity.PAGE_INDEX;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.addEof;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.isSimpleForm;


public final class NinchatPreAudienceQuestionnaireActivity extends NinchatBaseActivity {
    private final String TAG = NinchatPreAudienceQuestionnaireActivity.class.getSimpleName();
    public static final int REQUEST_CODE = NinchatPreAudienceQuestionnaireActivity.class.hashCode() & 0xffff;
    protected static final String QUEUE_ID = "queueId";
    private String queueId;
    private int lastElement = 0;

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
        if (isSimpleForm(NinchatSessionManager.getInstance().getNinchatQuestionnaires().getNinchatPreAudienceQuestionnaire().getQuestionnaireList())) {
            // add manual eof
            addEof(NinchatSessionManager.getInstance().getNinchatQuestionnaires().getNinchatPreAudienceQuestionnaire().getQuestionnaireList());
            final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.questionnaire_form_rview);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            final NinchatSimpleFormLikeQuestionnaireAdapter mPreAudienceQuestionnaireAdapter = new NinchatSimpleFormLikeQuestionnaireAdapter(
                    NinchatSessionManager.getInstance().getNinchatQuestionnaires().getNinchatPreAudienceQuestionnaire(), new NinchatSimpleFormLikeQuestionnaireAdapter.Callback() {
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
        } else {
            // get single item
            // get a list of items
            // feed them to recycler view and listen to on close or on error
            startActivityForResult(
                    NinchatComplexQuestionnaireActivity.getLaunchIntent(getApplicationContext(), lastElement),
                    NinchatComplexQuestionnaireActivity.REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onClose(final View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NinchatComplexQuestionnaireActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                final int nextIndex = data.getIntExtra(PAGE_INDEX, 0);
                startActivityForResult(
                        NinchatComplexQuestionnaireActivity.getLaunchIntent(getApplicationContext(),
                                nextIndex),
                        NinchatComplexQuestionnaireActivity.REQUEST_CODE);
            } else if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void close() {
        setResult(RESULT_OK, new Intent().putExtra(NinchatPreAudienceQuestionnaireActivity.QUEUE_ID, queueId));
        finish();
    }
}
