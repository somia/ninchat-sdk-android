package com.ninchat.sdk.adapters.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ninchat.sdk.helper.NinchatAvatar;
import com.ninchat.sdk.models.NinchatMessage;

public class NinchatMessageViewHolder extends RecyclerView.ViewHolder {
    private final String TAG = NinchatMessageViewHolder.class.getSimpleName();
    protected final NinchatAvatar ninchatAvatar;
    protected final NinchatPaddingViewHolder paddingViewHolder;
    protected final NinchatMetaViewHolder metaViewHolder;
    protected final NinchatEndViewHolder endViewHolder;
    protected final NinchatWritingViewHolder writingViewHolder;
    protected final NinchatOptionsViewHolder multipleChoiceViewHolder;
    protected final NinchatRemoteMessageViewHolder remoteMessageViewHolder;
    protected final NinchatGeneralViewHolder generalViewHolder;
    private final Callback callback;

    public NinchatMessageViewHolder(View itemView, Callback callback) {
        super(itemView);
        this.callback = callback;
        ninchatAvatar = new NinchatAvatar();
        paddingViewHolder = new NinchatPaddingViewHolder(itemView);
        metaViewHolder = new NinchatMetaViewHolder(itemView);
        endViewHolder = new NinchatEndViewHolder(itemView);
        writingViewHolder = new NinchatWritingViewHolder(itemView);
        multipleChoiceViewHolder = new NinchatOptionsViewHolder(itemView);
        remoteMessageViewHolder = new NinchatRemoteMessageViewHolder(itemView);
        generalViewHolder = new NinchatGeneralViewHolder(itemView);
    }

    public void bind(final NinchatMessage data, final boolean isContinuedMessage) {
        if (data.getType() == NinchatMessage.Type.PADDING) {
            paddingViewHolder.bind();
        } else if (data.getType() == NinchatMessage.Type.META) {
            metaViewHolder.bind(data);
        } else if (data.getType() == NinchatMessage.Type.END) {
            endViewHolder.bind(callback);
        } else if (data.getType().equals(NinchatMessage.Type.WRITING)) {
            writingViewHolder.bind(data, ninchatAvatar, isContinuedMessage);
        } else if (data.getType().equals(NinchatMessage.Type.MULTICHOICE)) {
            multipleChoiceViewHolder.bind(data, ninchatAvatar, this, isContinuedMessage);
        } else if (data.isRemoteMessage()) {
            remoteMessageViewHolder.bind(data, ninchatAvatar, isContinuedMessage);
        } else {
            generalViewHolder.bind(data, ninchatAvatar, isContinuedMessage);
        }
        this.callback.onRequiredAnimationChange();
    }

    public void onMultiChoiceOptionToggled(final NinchatMessage message, final int choiceIndex) {
        this.callback.onOptionsToggled(message, choiceIndex, getAdapterPosition());
    }


    public interface Callback {
        void onChatClosed();

        void onOptionsToggled(final NinchatMessage message, final int choiceIndex, final int position);

        void onRequiredAnimationChange();
    }
}