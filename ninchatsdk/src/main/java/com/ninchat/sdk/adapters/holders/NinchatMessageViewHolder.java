package com.ninchat.sdk.adapters.holders;

import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.R;
import com.ninchat.sdk.activities.NinchatMediaActivity;
import com.ninchat.sdk.adapters.NinchatMessageAdapter;
import com.ninchat.sdk.adapters.NinchatMultiChoiceAdapter;
import com.ninchat.sdk.helper.NinchatAvatar;
import com.ninchat.sdk.models.NinchatFile;
import com.ninchat.sdk.models.NinchatMessage;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NinchatMessageViewHolder extends RecyclerView.ViewHolder {

    protected static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("HH:mm", new Locale("fi-FI"));
    protected final NinchatAvatar ninchatAvatar;
    private final Callback callback;

    public NinchatMessageViewHolder(View itemView, Callback callback) {
        super(itemView);
        this.callback = callback;
        ninchatAvatar = new NinchatAvatar();
    }

    private void bindMessage(final @IdRes int wrapperId,
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
                             final @DrawableRes
                             int repeatedMessageBackground) {
        itemView.findViewById(wrapperId).setVisibility(View.VISIBLE);
        itemView.findViewById(headerId).setVisibility(View.GONE);

        final TextView sender = itemView.findViewById(senderId);
        String senderNameOverride = NinchatSessionManager.getInstance().getName(senderId == R.id.ninchat_chat_message_agent_name);
        sender.setText(senderNameOverride != null ? senderNameOverride : ninchatMessage.getSender());

        final TextView timestamp = itemView.findViewById(timestampId);
        timestamp.setText(TIMESTAMP_FORMATTER.format(ninchatMessage.getTimestamp()));
        ninchatAvatar.setAvatar(itemView.getContext(), itemView.findViewById(avatarId), ninchatMessage, isContinuedMessage);
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

    public void bind(final NinchatMessage data, final boolean isContinuedMessage) {
        if (data.getType() == NinchatMessage.Type.PADDING) {
            itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.VISIBLE);
        } else if (data.getType() == NinchatMessage.Type.META) {
            itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
            final TextView start = itemView.findViewById(R.id.ninchat_chat_message_meta);
            start.setText(data.getMessage());
            start.setVisibility(View.VISIBLE);
        } else if (data.getType() == NinchatMessage.Type.END) {
            itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
            final TextView end = itemView.findViewById(R.id.ninchat_chat_message_end_text);
            end.setText(NinchatSessionManager.getInstance().getChatEnded());
            final Button closeButton = itemView.findViewById(R.id.ninchat_chat_message_close);
            closeButton.setText(NinchatSessionManager.getInstance().getCloseChat());
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.onClickListener();
                }
            });
            itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.VISIBLE);
        } else if (data.getType().equals(NinchatMessage.Type.WRITING)) {
            itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_image).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(isContinuedMessage ? View.GONE : View.VISIBLE);
            ninchatAvatar.setAvatar(itemView.getContext(), itemView.findViewById(R.id.ninchat_chat_message_agent_avatar), data, isContinuedMessage);
            itemView.findViewById(R.id.ninchat_chat_message_agent_message).setVisibility(View.GONE);

            String agentNameOverride = NinchatSessionManager.getInstance().getName(true);
            final TextView agentName = itemView.findViewById(R.id.ninchat_chat_message_agent_name);
            agentName.setText(agentNameOverride != null ? agentNameOverride : data.getSender());

            itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper)
                    .setBackgroundResource(isContinuedMessage ?
                            R.drawable.ninchat_chat_bubble_left_repeated :
                            R.drawable.ninchat_chat_bubble_left);
            final ImageView image = itemView.findViewById(R.id.ninchat_chat_message_agent_writing);
            image.setVisibility(View.VISIBLE);
            GlideApp.with(image.getContext()).clear(image);
            image.setBackgroundResource(R.drawable.ninchat_icon_chat_writing_indicator);
            final AnimationDrawable animationDrawable = (AnimationDrawable) image.getBackground();
            animationDrawable.start();
            if (isContinuedMessage) {
                itemView.findViewById(R.id.ninchat_chat_message_agent).setPadding(0, 0, 0, 0);
            }
        } else if (data.getType().equals(NinchatMessage.Type.MULTICHOICE)) {
            itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.VISIBLE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_image).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_title).setVisibility(isContinuedMessage ? View.GONE : View.VISIBLE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_writing).setVisibility(View.GONE);
            ninchatAvatar.setAvatar(itemView.getContext(), itemView.findViewById(R.id.ninchat_chat_message_agent_avatar), data, isContinuedMessage);

            String agentNameOverride = NinchatSessionManager.getInstance().getName(true);
            final TextView agentName = itemView.findViewById(R.id.ninchat_chat_message_agent_name);
            agentName.setText(agentNameOverride != null ? agentNameOverride : data.getSender());

            itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper)
                    .setBackgroundResource(isContinuedMessage ?
                            R.drawable.ninchat_chat_bubble_left_repeated :
                            R.drawable.ninchat_chat_bubble_left);
            final TextView message = itemView.findViewById(R.id.ninchat_chat_message_agent_message);
            final Spanned messageText = data.getMessage();
            message.setText(messageText);
            message.setVisibility(messageText != null ? View.VISIBLE : View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.VISIBLE);
            final RecyclerView options = itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice_options);
            options.setLayoutManager(messageText != null ? new LinearLayoutManager(itemView.getContext()) : new GridLayoutManager(itemView.getContext(), 2));
            if (messageText == null) {
                options.addItemDecoration(new RecyclerView.ItemDecoration() {
                    @Override
                    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                        outRect.top = 0;
                        outRect.bottom = 0;
                        if (parent.getChildLayoutPosition(view) % 2 == 0) {
                            outRect.left = 0;
                            outRect.right = 2;
                        } else {
                            outRect.left = 2;
                            outRect.right = 0;
                        }
                    }
                });
            }
            options.setAdapter(new NinchatMultiChoiceAdapter(data, this, messageText == null));
            final Button sendButton = itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice_send);
            sendButton.setText(NinchatSessionManager.getInstance().getSubmitButtonText());
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        NinchatSessionManager.getInstance().sendUIAction(data.getMultiChoiceData());
                    } catch (final JSONException e) {
                        Log.e(NinchatMessageAdapter.class.getSimpleName(), "Error when sending multichoice answer!", e);
                    }
                }
            });
            sendButton.setVisibility(messageText != null ? View.VISIBLE : View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_wrapper).getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            if (isContinuedMessage) {
                itemView.findViewById(R.id.ninchat_chat_message_agent).setPadding(0, 0, 0, 0);
            }
        } else if (data.isRemoteMessage()) {
            itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_user).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_writing).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent_multichoice).setVisibility(View.GONE);
            bindMessage(R.id.ninchat_chat_message_agent,
                    R.id.ninchat_chat_message_agent_title,
                    R.id.ninchat_chat_message_agent_name,
                    R.id.ninchat_chat_message_agent_timestamp,
                    R.id.ninchat_chat_message_agent_message,
                    R.id.ninchat_chat_message_agent_image,
                    R.id.ninchat_chat_message_agent_video_play_image,
                    R.id.ninchat_chat_message_agent_avatar,
                    data, isContinuedMessage,
                    R.drawable.ninchat_chat_bubble_left,
                    R.drawable.ninchat_chat_bubble_left_repeated);
        } else {
            itemView.findViewById(R.id.ninchat_chat_message_meta).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_agent).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_end).setVisibility(View.GONE);
            itemView.findViewById(R.id.ninchat_chat_message_padding).setVisibility(View.GONE);
            bindMessage(R.id.ninchat_chat_message_user,
                    R.id.ninchat_chat_message_user_title,
                    R.id.ninchat_chat_message_user_name,
                    R.id.ninchat_chat_message_user_timestamp,
                    R.id.ninchat_chat_message_user_message,
                    R.id.ninchat_chat_message_user_image,
                    R.id.ninchat_chat_message_user_video_play_image,
                    R.id.ninchat_chat_message_user_avatar,
                    data, isContinuedMessage,
                    R.drawable.ninchat_chat_bubble_right,
                    R.drawable.ninchat_chat_bubble_right_repeated);
        }
        this.callback.onRequiredAnimationChange();
    }

    public void optionToggled(final NinchatMessage message, final int position) {
        this.callback.onOptionToggled(message, position);
    }


    public interface Callback {
        void onClickListener();

        void onOptionToggled(final NinchatMessage message, final int position);

        void onRequiredAnimationChange();
    }
}