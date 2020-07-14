package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatImageGetter;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONObject;
import static com.ninchat.sdk.helper.questionnaire.NinchatQuestionnaireItemGetter.*;

public class NinchatTextViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatTextViewHolder.class.getSimpleName();
    private final TextView mContent;
    private final boolean isFormLikeQuestionnaire;

    public NinchatTextViewHolder(@NonNull View itemView, final int position,
                                 final NinchatQuestionnaire ninchatQuestionnaire,
                                 final boolean isFormLikeQuestionnaire) {
        super(itemView);
        mContent = (TextView) itemView.findViewById(R.id.text_view_content);
        this.isFormLikeQuestionnaire = isFormLikeQuestionnaire;
        bind(position, ninchatQuestionnaire);
    }

    public void bind(final int position, final NinchatQuestionnaire ninchatQuestionnaire) {
        final JSONObject item = ninchatQuestionnaire.getItem(position);
        String text = getLabel(item);
        if (isFormLikeQuestionnaire) {
            itemView.setBackground(
                    ContextCompat.getDrawable(itemView.getContext(), R.drawable.ninchat_chat_form_questionnaire_background)
            );
        }

        mContent.setAutoLinkMask(0);
        mContent.setMovementMethod(LinkMovementMethod.getInstance());
        // there might be some images images
        mContent.setText(Html.fromHtml(text, new NinchatImageGetter(mContent, true, null), null));
    }
}
