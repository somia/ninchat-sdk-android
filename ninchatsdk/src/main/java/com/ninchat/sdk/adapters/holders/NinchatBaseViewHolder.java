package com.ninchat.sdk.adapters.holders;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatMediaActivity;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatMessage;

import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class NinchatBaseViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = NinchatBaseViewHolder.class.getSimpleName();

    protected static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("HH:mm", new Locale("fi-FI"));

    NinchatBaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public abstract void bind(final NinchatMessage data, final boolean isContinuedMessage) throws Exception;

    public void bindMessage(
            final @IdRes int wrapperId,
            final @IdRes int headerId,
            final @IdRes int senderId,
            final @IdRes int timestampId,
            final @IdRes int messageView,
            final @IdRes int imageId,
            final @IdRes int playIconId,
            final @IdRes int avatarId,
            final NinchatMessage ninchatMessage,
            final boolean isContinuedMessage,
            int firstMessageBackground,
            @DrawableRes int repeatedMessageBackground
    ) {
        itemView.findViewById(wrapperId).setVisibility(View.VISIBLE);
        itemView.findViewById(headerId).setVisibility(View.GONE);

        final TextView sender = itemView.findViewById(senderId);
        String senderNameOverride = NinchatSessionManager.getInstance().getName(senderId == R.id.ninchat_chat_message_agent_name);
        sender.setText(senderNameOverride != null ? senderNameOverride : ninchatMessage.getSender());

        final TextView timestamp = itemView.findViewById(timestampId);
        timestamp.setText(TIMESTAMP_FORMATTER.format(ninchatMessage.getTimestamp()));
        //ninchatAvatar.setAvatar(itemView.getContext(), itemView.findViewById(avatarId), ninchatMessage, isContinuedMessage);
        final TextView message = itemView.findViewById(messageView);
        message.setVisibility(View.GONE);
        final Spanned messageContent = ninchatMessage.getMessage();
        final NinchatFile file = NinchatSessionManager.getInstance().getFile(ninchatMessage.getFileId());
        final ImageView image = itemView.findViewById(imageId);
        image.setVisibility(View.GONE);
        final View playIcon = itemView.findViewById(playIconId);
        playIcon.setVisibility(View.GONE);
        GlideApp.with(image.getContext()).clear(image);
        image.setBackground(null);
        if (messageContent != null) {
            message.setVisibility(View.VISIBLE);
            message.setAutoLinkMask(Linkify.ALL);
            message.setText(messageContent);
        } else if (file.isDownloadableFile()) {
            message.setVisibility(View.VISIBLE);
            message.setText(file.getFileLink());
            message.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            final int width = file.getWidth();
            final int height = file.getHeight();
            final float density = itemView.getResources().getDisplayMetrics().density;
            image.getLayoutParams().width = (int) (width * density);
            image.getLayoutParams().height = (int) (height * density);
            image.setBackgroundResource(isContinuedMessage ? repeatedMessageBackground : firstMessageBackground);
            image.setVisibility(View.VISIBLE);
            GlideApp.with(image.getContext())
                    .load(file.getUrl())
                    .placeholder(R.color.ninchat_colorPrimaryDark)
                    .override(width, height)
                    .into(image);
            if (file.isVideo()) {
                playIcon.setVisibility(View.VISIBLE);
            }
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.getContext().startActivity(NinchatMediaActivity.getLaunchIntent(v.getContext(), ninchatMessage.getFileId()));
                }
            });
        }
        itemView.findViewById(messageView).setBackgroundResource(isContinuedMessage ? repeatedMessageBackground : firstMessageBackground);
        if (isContinuedMessage) {
            itemView.findViewById(wrapperId).setPadding(0, 0, 0, 0);
        } else {
            itemView.findViewById(headerId).setVisibility(View.VISIBLE);
        }
    }
}
