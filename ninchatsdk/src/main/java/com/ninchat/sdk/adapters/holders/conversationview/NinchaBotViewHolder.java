package com.ninchat.sdk.adapters.holders.conversationview;


import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.adapters.NinchatFormLikeQuestionnaireAdapter;
import com.ninchat.sdk.helper.NinchatQuestionnaireItemDecoration;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.getElements;

public class NinchaBotViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchaBotViewHolder.class.getSimpleName();
    final JSONObject currentElement;
    final int currentPosition;
    final TextView mTextView;
    final ImageView mImageView;
    private RecyclerView mRecyclerView;
    private NinchatFormLikeQuestionnaireAdapter mFormLikeAudienceQuestionnaireAdapter;

    public NinchaBotViewHolder(@NonNull View itemView, final int position, final JSONObject currentElement) {
        super(itemView);
        this.currentPosition = position;
        this.currentElement = currentElement;
        mTextView = itemView.findViewById(R.id.ninchat_chat_message_bot_text);
        mImageView = itemView.findViewById(R.id.ninchat_chat_message_bot_writing);
        mRecyclerView = (RecyclerView) itemView.findViewById(R.id.questionnaire_conversation_rview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
        update(itemView);
    }

    public void update(View itemView) {
        mTextView.setText("LightbotAgent");
        mImageView.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator);
        final AnimationDrawable animationDrawable = (AnimationDrawable) mImageView.getBackground();
        animationDrawable.start();
        new Handler().postDelayed(() -> {
            animationDrawable.stop();
            mImageView.setVisibility(View.INVISIBLE);
            mFormLikeAudienceQuestionnaireAdapter = new NinchatFormLikeQuestionnaireAdapter(
                    new NinchatQuestionnaire(getElements(currentElement)));
            final int spaceInPixelTop = itemView.getResources().getDimensionPixelSize(R.dimen.items_margin_top);
            final int spaceLeft = 0;
            final int spaceRight = 0;
            mRecyclerView.addItemDecoration(new NinchatQuestionnaireItemDecoration(
                    spaceInPixelTop,
                    spaceLeft,
                    spaceRight
            ));
            mRecyclerView.setAdapter(mFormLikeAudienceQuestionnaireAdapter);
            mRecyclerView.setItemViewCacheSize(mFormLikeAudienceQuestionnaireAdapter.getItemCount());
        }, 1000);

    }
}
