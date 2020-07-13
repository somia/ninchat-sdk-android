package com.ninchat.sdk.adapters.holders.formview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatImageGetter;
import com.ninchat.sdk.models.questionnaire.NinchatQuestionnaire;

import org.json.JSONObject;
import static com.ninchat.sdk.helper.NinchatQuestionnaire.getLabel;

public class NinchatTextViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatTextViewHolder.class.getSimpleName();
    private final TextView mContent;

    public NinchatTextViewHolder(@NonNull View itemView, final int position,
                                 final NinchatQuestionnaire ninchatQuestionnaire) {
        super(itemView);
        mContent = (TextView) itemView.findViewById(R.id.text_view_content);
        bind(position, ninchatQuestionnaire);
    }

    public void bind(final int position, final NinchatQuestionnaire ninchatQuestionnaire) {
        final JSONObject item = ninchatQuestionnaire.getItem(position);
        final String text = getLabel(item);
        mContent.setAutoLinkMask(0);
        mContent.setMovementMethod(LinkMovementMethod.getInstance());
        // there might be some images images
        mContent.setText(Html.fromHtml(text, new NinchatImageGetter(mContent, true, null), null));
    }
}
