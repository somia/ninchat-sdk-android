package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnRequireStepChange;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.hasButton;

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
        mPrevious = (Button) itemView.findViewById(R.id.ninchat_button_previous);
        mNext = (Button) itemView.findViewById(R.id.ninchat_button_next);
        mPreviousImage = (ImageView) itemView.findViewById(R.id.ninchat_image_button_previous);
        mNextImage = (ImageView) itemView.findViewById(R.id.ninchat_image_button_next);
        itemPosition = position;
        questionnaire = new WeakReference(ninchatQuestionnaire);
        this.bind();
    }

    public void bind() {
        final JSONObject currentItem = questionnaire.get().getItem(itemPosition);
        mPrevious.setVisibility(View.INVISIBLE);
        mPreviousImage.setVisibility(View.INVISIBLE);
        mNext.setVisibility(View.INVISIBLE);
        mNextImage.setVisibility(View.INVISIBLE);

        if (hasButton(currentItem, true)) {
            final String text = currentItem.optString("back");
            if ("true".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
                mPreviousImage.setVisibility(View.VISIBLE);
                mPreviousImage.setOnClickListener(v -> mayBeFireComplete(OnRequireStepChange.back));
            } else {
                mPrevious.setVisibility(View.VISIBLE);
                mPrevious.setText(text);
                mPrevious.setOnClickListener(v -> mayBeFireComplete(OnRequireStepChange.back));
            }
        }

        if (hasButton(currentItem, false)) {
            final String text = currentItem.optString("next");
            if ("true".equalsIgnoreCase(text) || TextUtils.isEmpty(text)) {
                mNextImage.setVisibility(View.VISIBLE);
                mNextImage.setOnClickListener(v -> mayBeFireComplete(OnRequireStepChange.forward));
            } else {
                mNext.setVisibility(View.VISIBLE);
                mNext.setText(text);
                mNext.setOnClickListener(v -> mayBeFireComplete(OnRequireStepChange.forward));
            }
        }
    }

    private void mayBeFireComplete(final int moveType) {
        final JSONObject rootItem = questionnaire.get().getItem(itemPosition);
        if (rootItem.optBoolean("fireEvent", false)) {
            EventBus.getDefault().post(new OnRequireStepChange(moveType));
        }
    }
}
