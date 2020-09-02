package com.ninchat.sdk.helper;

import android.content.Context;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.NinchatSessionManager;
import com.ninchat.sdk.models.NinchatMessage;
import com.ninchat.sdk.models.NinchatUser;

public class NinchatAvatar {
    public void setAvatar(final Context mContext,
                          final ImageView avatar,
                          final NinchatMessage ninchatMessage,
                          final boolean hideAvatar) {
        final NinchatUser user = NinchatSessionManager.getInstance().getMember(ninchatMessage.getSenderId());
        final String userAvatarText = getUserAvatarText(user, ninchatMessage.isRemoteMessage());
        // set avatar
        setAvatar(mContext, avatar, userAvatarText);
        final boolean invisible = NinchatSessionManager.getInstance().showAvatars(ninchatMessage.isRemoteMessage());
        if (!invisible) {
            showAvatar(avatar);
        } else if (hideAvatar) {
            hideAvatar(avatar, invisible);
        }
    }

    @VisibleForTesting
    protected void setAvatar(final Context mContext, final ImageView avatar, final String avatarText) {
        if (TextUtils.isEmpty(avatarText)) {
            return;
        }
        GlideApp.with(mContext)
                .load(avatarText)
                .circleCrop()
                .into(avatar);
    }

    @VisibleForTesting
    protected String getUserAvatarText(final NinchatUser user, final boolean remoteMessage) {
        String avatarText = user != null ? user.getAvatar() : null;

        if (TextUtils.isEmpty(avatarText)) {
            avatarText = NinchatSessionManager.getInstance().getDefaultAvatar(remoteMessage);
        }

        return avatarText;
    }

    @VisibleForTesting
    protected void showAvatar(final ImageView avatar) {
        if (avatar == null) {
            return;
        }
        avatar.setVisibility(View.GONE);
    }

    @VisibleForTesting
    protected void hideAvatar(final ImageView avatar, final boolean invisible) {
        if (avatar == null) {
            return;
        }
        avatar.setVisibility(invisible ? View.INVISIBLE : View.GONE);
    }
}
