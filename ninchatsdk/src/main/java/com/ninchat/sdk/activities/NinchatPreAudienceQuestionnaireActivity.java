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
import com.ninchat.sdk.adapters.holders.formview.NinchatControlFlowViewHolder;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire.NinchatPreAudienceQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import java.util.logging.Logger;

public final class NinchatPreAudienceQuestionnaireActivity extends NinchatBaseActivity {
    private final String TAG = NinchatPreAudienceQuestionnaireActivity.class.getSimpleName();
    public static final int REQUEST_CODE = NinchatPreAudienceQuestionnaireActivity.class.hashCode() & 0xffff;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_form_questionnaire;
    }

    public static Intent getLaunchIntent(final Context context) {
        return new Intent(context, NinchatPreAudienceQuestionnaireActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final NinchatQuestionnaire ninchatQuestionnaire = NinchatSessionManager.getInstance().getNinchatQuestionnaire();
        final RecyclerView mRecyclerView = (RecyclerView)findViewById(R.id.questionnaire_form_rview);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final NinchatSimpleFormLikeQuestionnaireAdapter mPreAudienceQuestionnaireAdapter = new NinchatSimpleFormLikeQuestionnaireAdapter(
                ninchatQuestionnaire.getNinchatPreAudienceQuestionnaire(), new NinchatSimpleFormLikeQuestionnaireAdapter.Callback() {
            @Override
            public void onRequiredScroll(int position) {
                mRecyclerView.smoothScrollToPosition(position);
            }

            @Override
            public void onComplete(NinchatPreAudienceQuestionnaire preAudienceQuestionnaire) {
                Log.d(TAG, "completed. should forward to chat intent");
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
        setResult(RESULT_CANCELED, null);
        finish();
    }
}
