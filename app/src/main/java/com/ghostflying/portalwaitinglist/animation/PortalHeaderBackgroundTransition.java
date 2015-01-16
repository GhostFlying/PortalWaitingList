package com.ghostflying.portalwaitinglist.animation;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.transition.Transition;
import android.transition.TransitionValues;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.ghostflying.portalwaitinglist.R;

/**
 * Created by ghostflying on 1/14/15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PortalHeaderBackgroundTransition extends Transition {
    private static final String PROP_NAME_BACKGROUND = "ghostflying:view_background";

    private int mMode;

    public PortalHeaderBackgroundTransition(){

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PortalHeaderBackgroundTransition(Context context, AttributeSet attrs){
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PortalHeaderBackgroundTransition);
        mMode = a.getInt(R.styleable.PortalHeaderBackgroundTransition_mode, 0);
        a.recycle();
    }

    public void setMode(int mode){
        mMode = mode;
    }


    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues transitionValues){
        transitionValues.values.put(PROP_NAME_BACKGROUND, transitionValues.view.getBackground());
    }

    @Override
    public Animator createAnimator(ViewGroup sceneRoot,
                                   TransitionValues startValues,
                                   TransitionValues endValues){
        Drawable endBackground = (Drawable)endValues.values.get(PROP_NAME_BACKGROUND);
        if (endBackground instanceof ColorDrawable){
            if (mMode == 0){
                return ObjectAnimator.ofObject(
                        endBackground,
                        "color",
                        new ArgbEvaluator(),
                        Color.parseColor("#FFFFFF"),
                        ((ColorDrawable) endBackground).getColor());
            }
            else {
                return ObjectAnimator.ofObject(
                        endBackground,
                        "color",
                        new ArgbEvaluator(),
                        ((ColorDrawable) endBackground).getColor(),
                        Color.parseColor("#FFFFFF"));
            }
        }
        return null;
    }
}
