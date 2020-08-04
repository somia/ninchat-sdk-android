package com.ninchat.sdk.adapters.holders.conversationview;


import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatFormQuestionnaireAdapter;
import com.ninchat.sdk.events.OnComponentError;
import com.ninchat.sdk.events.OnItemLoaded;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;

public class NinchatConversationViewHolder extends RecyclerView.ViewHolder {
    private String TAG = NinchatConversationViewHolder.class.getSimpleName();
    TextView mTextView;
    ImageView mImageView;
    private RecyclerView mRecyclerView;
    private NinchatFormQuestionnaireAdapter mFormLikeAudienceQuestionnaireAdapter;
    private WeakReference<JSONObject> mQuestionnaireElementWeakReference;

    public NinchatConversationViewHolder(@NonNull View itemView,
                                         JSONObject questionnaireElement,
                                         int position) {
        super(itemView);
        EventBus.getDefault().register(this);
        mTextView = itemView.findViewById(R.id.ninchat_chat_message_bot_text);
        mImageView = itemView.findViewById(R.id.ninchat_chat_message_bot_writing);
        mRecyclerView = itemView.findViewById(R.id.questionnaire_conversation_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        mQuestionnaireElementWeakReference = new WeakReference(questionnaireElement);
        bind(questionnaireElement, position);
    }

    public void bind(JSONObject questionnaireElement, int position) {
        mTextView.setText("LightbotAgent");
        mImageView.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator);
        AnimationDrawable animationDrawable = (AnimationDrawable) mImageView.getBackground();
        animationDrawable.start();
        new Handler().postDelayed(() -> {
            animationDrawable.stop();
            itemView.findViewById(R.id.ninchat_chat_message_bot_writing_root).setVisibility(View.GONE);

            mFormLikeAudienceQuestionnaireAdapter = new NinchatFormQuestionnaireAdapter(
                    new NinchatQuestionnaire(getElements(questionnaireElement)), false);
            int spaceInPixelTop = itemView.getResources().getDimensionPixelSize(R.dimen.ninchat_questionnaire_item_margin_start);
            int spaceLeft = 0;
            int spaceRight = 0;
            mRecyclerView.addItemDecoration(new NinchatQuestionnaireItemDecoration(
                    spaceInPixelTop,
                    spaceLeft,
                    spaceRight
            ));
            mRecyclerView.setAdapter(mFormLikeAudienceQuestionnaireAdapter);
            mRecyclerView.setHasFixedSize(true);
            EventBus.getDefault().post(new OnItemLoaded(position));
        }, 1500);
    }

    @Subscribe
    public void onEvent(OnComponentError onComponentError) {
        String name = getName(mQuestionnaireElementWeakReference.get());
        if (TextUtils.isEmpty(name) || !name.equalsIgnoreCase(onComponentError.itemName)) {
            return;
        }
        mFormLikeAudienceQuestionnaireAdapter.notifyDataSetChanged();
        new Handler().post(() -> mRecyclerView.clearFocus());
    }
}
