package com.ninchat.sdk.helper.glidewrapper;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ninchat.sdk.GlideApp;
import com.ninchat.sdk.helper.NinchatImageGetter;
import com.ninchat.sdk.ninchatmedia.presenter.INinchatMediaCallback;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class GlideWrapper {
    public static void loadImage(Context context, String fileUrl, ImageView imageView) {
        GlideApp.with(context)
                .load(fileUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView);
    }

    public static void loadImage(Context context, String fileUrl, ImageView imageView, INinchatMediaCallback callback) {
        GlideApp.with(context)
                .load(fileUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        callback.onLoadError();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        callback.onLoadSuccess();
                        return false;
                    }
                })
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
