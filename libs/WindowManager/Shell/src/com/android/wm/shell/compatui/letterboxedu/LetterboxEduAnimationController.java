/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.android.wm.shell.compatui.letterboxedu;

import static com.android.internal.R.styleable.WindowAnimation_windowEnterAnimation;
import static com.android.internal.R.styleable.WindowAnimation_windowExitAnimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.AnyRes;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.IntProperty;
import android.util.Log;
import android.util.Property;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.animation.Animation;

import com.android.internal.policy.TransitionAnimation;

/**
 * Controls the enter/exit animations of the letterbox education.
 */
// TODO(b/215316431): Add tests
class LetterboxEduAnimationController {
    private static final String TAG = "LetterboxEduAnimation";

    private final TransitionAnimation mTransitionAnimation;
    private final String mPackageName;
    @AnyRes
    private final int mAnimStyleResId;

    @Nullable
    private Animation mDialogAnimation;
    @Nullable
    private Animator mBackgroundDimAnimator;

    LetterboxEduAnimationController(Context context) {
        mTransitionAnimation = new TransitionAnimation(context, /* debug= */ false, TAG);
        mAnimStyleResId = (new ContextThemeWrapper(context,
                android.R.style.ThemeOverlay_Material_Dialog).getTheme()).obtainStyledAttributes(
                com.android.internal.R.styleable.Window).getResourceId(
                com.android.internal.R.styleable.Window_windowAnimationStyle, 0);
        mPackageName = context.getPackageName();
    }

    /**
     * Starts both background dim fade-in animation and the dialog enter animation.
     */
    void startEnterAnimation(@NonNull LetterboxEduDialogLayout layout, Runnable endCallback) {
        // Cancel any previous animation if it's still running.
        cancelAnimation();

        final View dialogContainer = layout.getDialogContainer();
        mDialogAnimation = loadAnimation(WindowAnimation_windowEnterAnimation);
        if (mDialogAnimation == null) {
            endCallback.run();
            return;
        }
        mDialogAnimation.setAnimationListener(getAnimationListener(
                /* startCallback= */ () -> dialogContainer.setAlpha(1),
                /* endCallback= */ () -> {
                    mDialogAnimation = null;
                    endCallback.run();
                }));

        mBackgroundDimAnimator = getAlphaAnimator(layout.getBackgroundDim(),
                /* endAlpha= */ LetterboxEduDialogLayout.BACKGROUND_DIM_ALPHA,
                mDialogAnimation.getDuration());
        mBackgroundDimAnimator.addListener(getDimAnimatorListener());

        dialogContainer.startAnimation(mDialogAnimation);
        mBackgroundDimAnimator.start();
    }

    /**
     * Starts both the background dim fade-out animation and the dialog exit animation.
     */
    void startExitAnimation(@Nullable LetterboxEduDialogLayout layout, Runnable endCallback) {
        // Cancel any previous animation if it's still running.
        cancelAnimation();

        if (layout == null) {
            endCallback.run();
            return;
        }

        final View dialogContainer = layout.getDialogContainer();
        mDialogAnimation = loadAnimation(WindowAnimation_windowExitAnimation);
        if (mDialogAnimation == null) {
            endCallback.run();
            return;
        }
        mDialogAnimation.setAnimationListener(getAnimationListener(
                /* startCallback= */ () -> {},
                /* endCallback= */ () -> {
                    dialogContainer.setAlpha(0);
                    mDialogAnimation = null;
                    endCallback.run();
                }));

        mBackgroundDimAnimator = getAlphaAnimator(layout.getBackgroundDim(), /* endAlpha= */ 0,
                mDialogAnimation.getDuration());
        mBackgroundDimAnimator.addListener(getDimAnimatorListener());

        dialogContainer.startAnimation(mDialogAnimation);
        mBackgroundDimAnimator.start();
    }

    /**
     * Cancels all animations and resets the state of the controller.
     */
    void cancelAnimation() {
        if (mDialogAnimation != null) {
            mDialogAnimation.cancel();
            mDialogAnimation = null;
        }
        if (mBackgroundDimAnimator != null) {
            mBackgroundDimAnimator.cancel();
            mBackgroundDimAnimator = null;
        }
    }

    private Animation loadAnimation(int animAttr) {
        Animation animation = mTransitionAnimation.loadAnimationAttr(mPackageName, mAnimStyleResId,
                animAttr, /* translucent= */ false);
        if (animation == null) {
            Log.e(TAG, "Failed to load animation " + animAttr);
        }
        return animation;
    }

    private Animation.AnimationListener getAnimationListener(Runnable startCallback,
            Runnable endCallback) {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                startCallback.run();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                endCallback.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        };
    }

    private AnimatorListenerAdapter getDimAnimatorListener() {
        return new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mBackgroundDimAnimator = null;
            }
        };
    }

    private static Animator getAlphaAnimator(
            Drawable drawable, int endAlpha, long duration) {
        Animator animator = ObjectAnimator.ofInt(drawable, DRAWABLE_ALPHA, endAlpha);
        animator.setDuration(duration);
        return animator;
    }

    private static final Property<Drawable, Integer> DRAWABLE_ALPHA = new IntProperty<Drawable>(
            "alpha") {
        @Override
        public void setValue(Drawable object, int value) {
            object.setAlpha(value);
        }

        @Override
        public Integer get(Drawable object) {
            return object.getAlpha();
        }
    };
}