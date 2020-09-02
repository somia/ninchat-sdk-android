package com.ninchat.sdk.adapters.holders.formview;

import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnNextQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.hasButton;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.setAnimation;


public class NinchatButtonViewHolder extends RecyclerView.ViewHolder {
    private String TAG = NinchatButtonViewHolder.class.getSimpleName();

    private TextView mPrevious;
    private TextView mNext;
    private ImageView mPreviousImage;
    private ImageView mNextImage;

    public NinchatButtonViewHolder(@NonNull View itemView,
                                   JSONObject questionnaireElement,
                                   boolean isFormLikeQuestionnaire,
                                   int position) {
        super(itemView);
        mPrevious = itemView.findViewById(R.id.ninchat_button_previous);
        mNext = itemView.findViewById(R.id.ninchat_button_next);
        mPreviousImage = itemView.findViewById(R.id.ninchat_image_button_previous);
        mNextImage = itemView.findViewById(R.id.ninchat_image_button_next);
        this.bind(questionnaireElement, isFormLikeQuestionnaire, position, false);
    }

    public void bind(JSONObject questionnaireElement, boolean isFormLikeQuestionnaire, int position, boolean isUpdate) {
        mPrevious.setVisibility(View.GONE);
        mNext.setVisibility(View.GONE);
        mPreviousImage.setVisibility(View.GONE);
        mNextImage.setVisibility(View.GONE);
        if (hasButton(questionnaireElement, true)) {
            String text = questionnaireElement.optString("back");
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
            String text = questionnaireElement.optString("next");
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

        if (!isUpdate)
            setAnimation(itemView, position, position != 0);
    }

    private void mayBeFireComplete(JSONObject questionnaireElement, int moveType) {
        if (questionnaireElement != null && questionnaireElement.optBoolean("fireEvent", false)) {
            if ("thankYouText".equalsIgnoreCase(questionnaireElement.optString("type", ""))) {
                EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.thankYou));
            } else {
                EventBus.getDefault().post(new OnNextQuestionnaire(moveType));
            }

        }
    }
}
