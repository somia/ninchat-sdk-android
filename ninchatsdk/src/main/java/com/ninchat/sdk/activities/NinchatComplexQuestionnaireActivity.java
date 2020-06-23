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
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getElements;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getNextElement;

public final class NinchatComplexQuestionnaireActivity extends NinchatBaseActivity {
    private final String TAG = NinchatComplexQuestionnaireActivity.class.getSimpleName();
    public static final int REQUEST_CODE = NinchatComplexQuestionnaireActivity.class.hashCode() & 0xffff;
    public static final String PAGE_INDEX = "pageIndex";

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_ninchat_form_questionnaire;
    }

    public static Intent getLaunchIntent(final Context context, final int inputIndex) {
        return new Intent(context, NinchatComplexQuestionnaireActivity.class).putExtra(PAGE_INDEX, inputIndex);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final int previousPageIndex = intent.getIntExtra(PAGE_INDEX, -1);
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.questionnaire_form_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        final JSONArray questionnaire = NinchatSessionManager.getInstance().getNinchatQuestionnaires().getNinchatPreAudienceQuestionnaire().getQuestionnaireList();
        final JSONObject currentQuestionnaire = getNextElement(questionnaire, previousPageIndex);
        final JSONArray questionnaireList = getElements(currentQuestionnaire);
        final NinchatComplexFormLikeQuestionnaireAdapter mPreAudienceQuestionnaireAdapter = new NinchatComplexFormLikeQuestionnaireAdapter(
                new NinchatQuestionnaire(questionnaireList),
                new NinchatComplexFormLikeQuestionnaireAdapter.Callback() {
                    @Override
                    public void onError(int position) {
                        mRecyclerView.smoothScrollToPosition(position);
                        close();
                    }

                    @Override
                    public void onComplete() {
                        setResult(RESULT_OK, new Intent().putExtra(PAGE_INDEX, previousPageIndex + 1));
                        finish();
                    }
                });
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

    private void close() {
        setResult(RESULT_OK);
        finish();
    }
}
