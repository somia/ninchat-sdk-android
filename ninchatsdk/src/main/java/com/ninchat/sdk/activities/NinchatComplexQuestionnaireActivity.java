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

        final JSONObject element = getNextElement(
                NinchatSessionManager.getInstance().getNinchatQuestionnaires().getNinchatPreAudienceQuestionnaire().getQuestionnaireList(),
                previousPageIndex
        );

        // if not a group element make a group element. List of elements for the recycler view
        JSONArray elements = getElements(element);
        final RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.questionnaire_form_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
        finish();
    }
}
