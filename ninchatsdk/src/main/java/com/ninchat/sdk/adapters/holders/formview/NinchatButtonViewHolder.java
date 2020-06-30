package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.ninchat.sdk.R;
import com.ninchat.sdk.events.OnRequireStepChange;
import com.ninchat.sdk.models.questionnaire2.NinchatQuestionnaire;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import static com.ninchat.sdk.helper.NinchatQuestionnaire.hasButton;

public class NinchatButtonViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatButtonViewHolder.class.getSimpleName();

    private final Button mPrevious;
    private final Button mNext;
    private int itemPosition;
    WeakReference<NinchatQuestionnaire> questionnaire;

    public NinchatButtonViewHolder(@NonNull View itemView, final int position,
                                   final NinchatQuestionnaire ninchatQuestionnaire) {
        super(itemView);
        mPrevious = (Button) itemView.findViewById(R.id.ninchat_button_previous);
        mNext = (Button) itemView.findViewById(R.id.ninchat_button_next);
        itemPosition = position;
        questionnaire = new WeakReference(ninchatQuestionnaire);
        this.bind();
    }

    public void bind() {
        final JSONObject currentItem = questionnaire.get().getItem(itemPosition);
        if (!hasButton(currentItem, true)) {
            mPrevious.setVisibility(View.INVISIBLE);
        } else {
            mPrevious.setText(currentItem.optString("back"));
            // todo want to check all required field
            mPrevious.setOnClickListener(v -> mayBeFireComplete(OnRequireStepChange.back));
        }
        if (!hasButton(currentItem, false)) {
            mNext.setVisibility(View.INVISIBLE);
        } else {
            mNext.setText(currentItem.optString("next"));
            // todo want to check all required field
            mNext.setOnClickListener(v -> mayBeFireComplete(OnRequireStepChange.forward));
        }
    }

    private void mayBeFireComplete(final int moveType) {
        final JSONObject rootItem = questionnaire.get().getItem(itemPosition);
        if (rootItem.optBoolean("fireEvent", false)) {
            EventBus.getDefault().post(new OnRequireStepChange(moveType));
        }
    }
}
