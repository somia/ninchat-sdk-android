package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatImageGetter;

import org.json.JSONObject;

import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireMiscUtil.setAnimation;

public class NinchatTextViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    public NinchatTextViewHolder(@NonNull View itemView,
                                 JSONObject questionnaireElement,
                                 boolean isFormLikeQuestionnaire, int position) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.text_view_content);
        bind(questionnaireElement, isFormLikeQuestionnaire, position, false);
    }

    public void bind(JSONObject questionnaireElement, boolean isFormLikeQuestionnaire, int position, boolean isUpdate) {
        if (questionnaireElement == null) {
            return;
        }
        String labelText = getLabel(questionnaireElement);
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background)
            );
        }
        mTextView.setAutoLinkMask(0);
        mTextView.setMovementMethod(LinkMovementMethod.getInstance());
        // there might be some images images
        mTextView.setText(Html.fromHtml(labelText, new NinchatImageGetter(mTextView, true, null), null));
        if (!isUpdate)
            setAnimation(itemView, position, position != 0);
    }
}
