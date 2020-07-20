package com.ninchat.sdk.adapters.holders.formview;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnNextQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.hasButton;


public class NinchatButtonViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatButtonViewHolder.class.getSimpleName();

    private final Button mPrevious;
    private final Button mNext;
    private final ImageView mPreviousImage;
    private final ImageView mNextImage;

    public NinchatButtonViewHolder(@NonNull View itemView,
                                   final JSONObject questionnaireElement,
                                   final boolean isFormLikeQuestionnaire) {
        super(itemView);
        mPrevious = itemView.findViewById(R.id.ninchat_button_previous);
        mNext = itemView.findViewById(R.id.ninchat_button_next);
        mPreviousImage = itemView.findViewById(R.id.ninchat_image_button_previous);
        mNextImage = itemView.findViewById(R.id.ninchat_image_button_next);
        this.bind(questionnaireElement, isFormLikeQuestionnaire);
    }

    public void bind(final JSONObject questionnaireElement, final boolean isFormLikeQuestionnaire) {
        mPrevious.setVisibility(View.GONE);
        mNext.setVisibility(View.GONE);
        mPreviousImage.setVisibility(View.GONE);
        mNextImage.setVisibility(View.GONE);
        if (hasButton(questionnaireElement, true)) {
            final String text = questionnaireElement.optString("back");
            if ("true".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
                mPreviousImage.setVisibility(View.VISIBLE);
                mPreviousImage.setOnClickListener(v -> {
                    mPreviousImage.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_secondary_onclicked_button));
                    mayBeFireComplete(questionnaireElement, OnNextQuestionnaire.back);
                });
            } else {
                mPrevious.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mPrevious.setTooltipText(NinchatSessionManager.getInstance().getTranslation(text));
                }

                mPrevious.setText(NinchatSessionManager.getInstance().getTranslation(text));
                mPrevious.setOnClickListener(v -> {
                    mPrevious.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_secondary_onclicked_button));
                    mayBeFireComplete(questionnaireElement, OnNextQuestionnaire.back);
                });
            }
        }

        if (hasButton(questionnaireElement, false)) {
            final String text = questionnaireElement.optString("next");
            if ("true".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
                mNextImage.setVisibility(View.VISIBLE);
                mNextImage.setOnClickListener(v -> {
                    mNextImage.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_primary_oncliked_button));
                    mayBeFireComplete(questionnaireElement, OnNextQuestionnaire.forward);
                });
            } else {
                mNext.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mNext.setTooltipText(NinchatSessionManager.getInstance().getTranslation(text));
                }
                mNext.setText(NinchatSessionManager.getInstance().getTranslation(text));
                mNext.setOnClickListener(v -> {
                    mNext.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_primary_oncliked_button));
                    mayBeFireComplete(questionnaireElement, OnNextQuestionnaire.forward);
                });
            }
        }
    }

    private void mayBeFireComplete(final JSONObject questionnaireElement, final int moveType) {
        if (questionnaireElement != null && questionnaireElement.optBoolean("fireEvent", false)) {
            if ("_register".equalsIgnoreCase(questionnaireElement.optString("type", ""))) {
                EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.register));
            } else if ("_complete".equalsIgnoreCase(questionnaireElement.optString("type", ""))) {
                EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.complete));
            } else {
                EventBus.getDefault().post(new OnNextQuestionnaire(moveType));
            }

        }
    }
}
