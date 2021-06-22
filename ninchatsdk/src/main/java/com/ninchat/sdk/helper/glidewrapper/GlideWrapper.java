package com.ninchat.sdk.helper.glidewrapper;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.R;
import com.ninchat.sdk.helper.NinchatImageGetter;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

public class GlideWrapper {
    public static void loadImage(Context context, String fileUrl, ImageView imageView) {
        GlideApp.with(context)
                .load(fileUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView);
    }

    public static void loadImage(Context context, String fileUrl, ImageView imageView, @DrawableRes int id, int width, int height) {
        GlideApp.with(context)
                .load(fileUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(id)
                .override(width, height)
                .into(imageView);
    }

    public static void loadImageAsCircle(Context context, String fileUrl, ImageView imageView) {
        GlideApp.with(context)
                .load(fileUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(imageView);
    }

    public static void loadImageAsCircle(Context context, String fileUrl, ImageView imageView, int fallbackDrawable) {
        GlideApp.with(context)
                .load(fileUrl)
                .error(fallbackDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(imageView);
    }

    public static void loadImageAsBitmap(Context context, String fileUrl, NinchatImageGetter.BitmapDrawablePlaceholder drawable) {
        GlideApp.with(context)
                .asBitmap()
                .load(fileUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(drawable);
    }

    public static void loadImageAsBitmap(Context context, String fileUrl, NinchatImageGetter.BitmapDrawablePlaceholder drawable, int fallbackDrawable) {
        GlideApp.with(context)
                .asBitmap()
                .load(fileUrl)
                .error(fallbackDrawable)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(drawable);
    }
}
