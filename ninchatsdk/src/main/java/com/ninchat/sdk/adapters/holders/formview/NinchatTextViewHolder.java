package com.ninchat.sdk.adapters.holders.formview;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.ninchat.sdk.R;
import com.ninchat.sdk.utils.misc.Misc;

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

        // there might be some images images
        mTextView.setText(Misc.toRichText(labelText, mTextView));
        if (!isUpdate)
            setAnimation(itemView, position, position != 0);
    }
}
