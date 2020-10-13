package com.ninchat.sdk.helper

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import android.widget.TextView
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.ninchat.sdk.helper.glidewrapper.GlideWrapper
import java.lang.ref.WeakReference

class NinchatImageGetter(
        container: TextView,
        private val matchParentWidth: Boolean = true,
        private val imagesHandler: HtmlImagesHandler? = null,
) : ImageGetter {
    private val mContainer: WeakReference<TextView> = WeakReference(container)
    private var density = container.resources.displayMetrics.density

    override fun getDrawable(path: String): Drawable {
        imagesHandler?.addImage(path)
        val drawable = BitmapDrawablePlaceholder()
        // Load Image to the Drawable
        mContainer.get()?.apply {
            post {
                GlideWrapper.loadImageAsBitmap(context, path, drawable)
            }
        }

        return drawable
    }

    inner class BitmapDrawablePlaceholder : BitmapDrawable(mContainer.get()?.resources, Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)), Target<Bitmap> {
        private var drawable: Drawable? = null
            set(value) {
                field = value
                value?.let { drawable ->
                    val drawableWidth = (drawable.intrinsicWidth * density).toInt()
                    val drawableHeight = (drawable.intrinsicHeight * density).toInt()
                    mContainer.get()?.let {
                        val maxWidth = it.measuredWidth
                        if (drawableWidth > maxWidth || matchParentWidth) {
                            val calculatedHeight = maxWidth * drawableHeight / drawableWidth
                            drawable.setBounds(0, 0, maxWidth, calculatedHeight)
                            setBounds(0, 0, maxWidth, calculatedHeight)
                        } else {
                            drawable.setBounds(0, 0, drawableWidth, drawableHeight)
                            setBounds(0, 0, drawableWidth, drawableHeight)
                        }
                    }

                    mContainer.get()?.text = mContainer.get()?.text
                }
            }

        override fun draw(canvas: Canvas) {
            drawable?.draw(canvas)
        }

        override fun onLoadStarted(placeholderDrawable: Drawable?) {
            placeholderDrawable?.let {
                drawable = it
            }
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            errorDrawable?.let {
                drawable = it
            }
        }

        override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
            drawable = BitmapDrawable(mContainer.get()!!.resources, bitmap)
        }

        override fun onLoadCleared(placeholderDrawable: Drawable?) {
            placeholderDrawable?.let {
                drawable = it
            }
        }

        override fun getSize(sizeReadyCallback: SizeReadyCallback) {
            sizeReadyCallback.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        }

        override fun removeCallback(cb: SizeReadyCallback) {}
        override fun setRequest(request: Request?) {}
        override fun getRequest(): Request? {
            return null
        }

        override fun onStart() {}
        override fun onStop() {}
        override fun onDestroy() {}
    }

    interface HtmlImagesHandler {
        fun addImage(uri: String?)
    }
}