package com.ninchat.sdk.helper.glidewrapper;

import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.helper.NinchatImageGetter;

import android.content.Context;
import android.widget.ImageView;

public class GlideWrapper {
    public static void loadImage(Context context, String fileUrl, ImageView imageView) {
        GlideApp.with(context)
                .load(fileUrl)
                .into(imageView);
    }

    public static void loadImageAsCircle(Context context, String fileUrl, ImageView imageView) {
        GlideApp.with(context)
                .load(fileUrl)
                .circleCrop()
                .into(imageView);
    }

    public static void loadImageAsBitmap(Context context, String fileUrl, NinchatImageGetter.BitmapDrawablePlaceholder drawable) {
        GlideApp.with(context)
                .asBitmap()
                .load(fileUrl)
                .into(drawable);
    }
}
