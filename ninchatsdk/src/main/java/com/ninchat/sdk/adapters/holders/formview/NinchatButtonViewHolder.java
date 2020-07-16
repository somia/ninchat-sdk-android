package com.ninchat.sdk.adapters.holders.formview;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnNextQuestionnaire;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.hasButton;


public class NinchatButtonViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatButtonViewHolder.class.getSimpleName();

    private final Button mPrevious;
    private final Button mNext;
    private final ImageView mPreviousImage;
    private final ImageView mNextImage;
    private int itemPosition;
    WeakReference<NinchatQuestionnaire> questionnaire;

    public NinchatButtonViewHolder(@NonNull View itemView, final int position,
                                   final NinchatQuestionnaire ninchatQuestionnaire) {
        super(itemView);
        mPrevious = itemView.findViewById(R.id.ninchat_button_previous);
        mNext = itemView.findViewById(R.id.ninchat_button_next);
        mPreviousImage = itemView.findViewById(R.id.ninchat_image_button_previous);
        mNextImage = itemView.findViewById(R.id.ninchat_image_button_next);
        itemPosition = position;
        questionnaire = new WeakReference(ninchatQuestionnaire);
        this.bind();
    }

    public void bind() {
        final JSONObject currentItem = questionnaire.get().getItem(itemPosition);
        mPrevious.setVisibility(View.GONE);
        mNext.setVisibility(View.GONE);
        mPreviousImage.setVisibility(View.GONE);
        mNextImage.setVisibility(View.GONE);
        if (hasButton(currentItem, true)) {
            final String text = currentItem.optString("back");
            if ("true".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
                mPreviousImage.setVisibility(View.VISIBLE);
                mPreviousImage.setOnClickListener(v -> mayBeFireComplete(OnNextQuestionnaire.back));
            } else {
                mPrevious.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mPrevious.setTooltipText(NinchatSessionManager.getInstance().getTranslation(text));
                }
                mPrevious.setText(NinchatSessionManager.getInstance().getTranslation(text));
                mPrevious.setOnClickListener(v -> mayBeFireComplete(OnNextQuestionnaire.back));
            }
        }

        if (hasButton(currentItem, false)) {
            final String text = currentItem.optString("next");
            if ("true".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
                mNextImage.setVisibility(View.VISIBLE);
                mNextImage.setOnClickListener(v -> mayBeFireComplete(OnNextQuestionnaire.forward));
            } else {
                mNext.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mNext.setTooltipText(NinchatSessionManager.getInstance().getTranslation(text));
                }
                mNext.setText(NinchatSessionManager.getInstance().getTranslation(text));
                mNext.setOnClickListener(v -> mayBeFireComplete(OnNextQuestionnaire.forward));
            }
        }
    }

    private void mayBeFireComplete(final int moveType) {
        final JSONObject rootItem = questionnaire.get().getItem(itemPosition);
        if (rootItem.optBoolean("fireEvent", false)) {
            if ("_register".equalsIgnoreCase(rootItem.optString("type", ""))) {
                EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.register));
            } else if ("_complete".equalsIgnoreCase(rootItem.optString("type", ""))) {
                EventBus.getDefault().post(new OnNextQuestionnaire(OnNextQuestionnaire.complete));
            } else {
                EventBus.getDefault().post(new OnNextQuestionnaire(moveType));
            }

        }
    }
}
