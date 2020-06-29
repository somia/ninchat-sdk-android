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
import com.ninchat.sdk.adapters.NinchatFormLikeQuestionnaireAdapter;
import com.ninchat.sdk.events.RequireStateChange;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getElements;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getMatchingElement;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getNextElement;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getQuestionnaireElementByTarget;


public final class NinchatPreAudienceQuestionnaireActivity extends NinchatBaseActivity {
    private final String TAG = NinchatPreAudienceQuestionnaireActivity.class.getSimpleName();
    public static final int REQUEST_CODE = NinchatPreAudienceQuestionnaireActivity.class.hashCode() & 0xffff;
    protected static final String QUEUE_ID = "queueId";
    private String queueId;
    private int lastElement = 0;
    private NinchatFormLikeQuestionnaireAdapter mPreAudienceQuestionnaireAdapter;
    private RecyclerView mRecyclerView;

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
        EventBus.getDefault().register(this);
        final Intent intent = getIntent();
        queueId = intent.getStringExtra(QUEUE_ID);
        // get a list of items
        mRecyclerView = (RecyclerView) findViewById(R.id.questionnaire_form_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        final NinchatQuestionnaire questionnaire = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires()
                .getNinchatPreAudienceQuestionnaire();

        final JSONObject groupQuestionnaire = getNextElement(questionnaire, lastElement);
        final JSONArray questionnaireElements = getElements(groupQuestionnaire);
        mPreAudienceQuestionnaireAdapter = new NinchatFormLikeQuestionnaireAdapter(new NinchatQuestionnaire(questionnaireElements));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void close() {
        setResult(RESULT_OK, new Intent().putExtra(NinchatPreAudienceQuestionnaireActivity.QUEUE_ID, queueId));
        finish();
    }

    private void showView(final NinchatQuestionnaire questionnaire){

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(RequireStateChange requireStateChange) {
        final NinchatQuestionnaire questionnaire = NinchatSessionManager
                .getInstance()
                .getNinchatQuestionnaires()
                .getNinchatPreAudienceQuestionnaire();

        final JSONObject groupQuestionnaire = getNextElement(questionnaire, lastElement);
        final String targetElement = getMatchingElement(questionnaire, groupQuestionnaire);
        lastElement = getQuestionnaireElementByTarget(questionnaire, targetElement);
        mPreAudienceQuestionnaireAdapter.updateContent(nextQuestionnaire(questionnaire, lastElement));
        mRecyclerView.setAdapter(mPreAudienceQuestionnaireAdapter);
        mPreAudienceQuestionnaireAdapter.notifyDataSetChanged();
    }

    private NinchatQuestionnaire nextQuestionnaire(final NinchatQuestionnaire questionnaire, final int index) {
        final JSONObject groupQuestionnaire = getNextElement(questionnaire, index);
        final JSONArray questionnaireElements = getElements(groupQuestionnaire);
        return new NinchatQuestionnaire(questionnaireElements);
    }
}
