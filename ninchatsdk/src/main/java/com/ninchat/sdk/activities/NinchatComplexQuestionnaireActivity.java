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
import com.ninchat.sdk.adapters.NinchatComplexFormLikeQuestionnaireAdapter;
import com.ninchat.sdk.events.RequireStateChange;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
        EventBus.getDefault().register(this);
        final int previousPageIndex = getIntent().getIntExtra(PAGE_INDEX, -1);
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.questionnaire_form_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final NinchatQuestionnaire questionnaire = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires()
                .getNinchatPreAudienceQuestionnaire();
        final JSONArray questionnaireList = getElements(getNextElement(questionnaire, previousPageIndex));
        final NinchatComplexFormLikeQuestionnaireAdapter mPreAudienceQuestionnaireAdapter =
                new NinchatComplexFormLikeQuestionnaireAdapter(new NinchatQuestionnaire(questionnaireList));
        final int spaceInPixel = getResources().getDimensionPixelSize(R.dimen.items_margin_top);
        mRecyclerView.addItemDecoration(new NinchatQuestionnaireItemDecoration(spaceInPixel));
        mRecyclerView.setAdapter(mPreAudienceQuestionnaireAdapter);
        mRecyclerView.setItemViewCacheSize(mPreAudienceQuestionnaireAdapter.getItemCount());
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onClose(final View view) {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    private void close() {
        setResult(RESULT_OK, null);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(RequireStateChange requireStateChange) {
        close();
    }
}
