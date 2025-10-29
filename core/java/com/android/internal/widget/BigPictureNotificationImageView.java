/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.widget;

import android.annotation.AttrRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.StyleRes;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.android.internal.R;

/**
 * Aggressively optimized BigPictureNotificationImageView for low-RAM devices.
 * - Forces low-RAM dimensions
 * - Avoids large bitmap allocations
 * - Disables drawing cache
 */
@RemoteViews.RemoteView
public class BigPictureNotificationImageView extends ImageView {

    private static final String TAG = BigPictureNotificationImageView.class.getSimpleName();

    private final int mMaximumDrawableWidth;
    private final int mMaximumDrawableHeight;

    public BigPictureNotificationImageView(@NonNull Context context) {
        this(context, null, 0, 0);
    }

    public BigPictureNotificationImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public BigPictureNotificationImageView(@NonNull Context context, @Nullable AttributeSet attrs,
                                           @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BigPictureNotificationImageView(@NonNull Context context, @Nullable AttributeSet attrs,
                                           @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        final boolean isLowRam = true;

        final Resources res = context.getResources();

        final int widthResId = isLowRam
                ? R.dimen.notification_big_picture_max_width_low_ram
                : R.dimen.notification_big_picture_max_width;
        final int heightResId = isLowRam
                ? R.dimen.notification_big_picture_max_height_low_ram
                : R.dimen.notification_big_picture_max_height;

        mMaximumDrawableWidth = res.getDimensionPixelSize(widthResId);
        mMaximumDrawableHeight = res.getDimensionPixelSize(heightResId);

        setAdjustViewBounds(true);
        setCropToPadding(true);
        setDrawingCacheEnabled(false);
        setMaxWidth(mMaximumDrawableWidth);
        setMaxHeight(mMaximumDrawableHeight);
        setScaleType(ScaleType.CENTER_INSIDE);
    }

    @Override
    @android.view.RemotableViewMethod(asyncImpl = "setImageURIAsync")
    public void setImageURI(@Nullable Uri uri) {
        setImageDrawable(loadImage(uri));
    }

    /** @hide **/
    public Runnable setImageURIAsync(@Nullable Uri uri) {
        final Drawable drawable = loadImage(uri);
        return () -> setImageDrawable(drawable);
    }

    @Override
    @android.view.RemotableViewMethod(asyncImpl = "setImageIconAsync")
    public void setImageIcon(@Nullable Icon icon) {
        setImageDrawable(loadImage(icon));
    }

    /** @hide **/
    public Runnable setImageIconAsync(@Nullable Icon icon) {
        final Drawable drawable = loadImage(icon);
        return () -> setImageDrawable(drawable);
    }

    private Drawable loadImage(@Nullable Uri uri) {
        if (uri == null) return null;
        return LocalImageResolver.resolveImage(uri, getContext(), mMaximumDrawableWidth,
                mMaximumDrawableHeight);
    }

    private Drawable loadImage(@Nullable Icon icon) {
        if (icon == null) return null;
        Drawable drawable = LocalImageResolver.resolveImage(icon, getContext(),
                mMaximumDrawableWidth, mMaximumDrawableHeight);
        return drawable != null ? drawable : icon.loadDrawable(getContext());
    }
}
