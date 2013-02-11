package io.indy.octodo.helper;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationHelper {

    private static int slideDuration = 100;

    public static Animation slideDownAnimation() {
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f,
                Animation.RELATIVE_TO_SELF,
                0.0f,
                Animation.RELATIVE_TO_SELF,
                -1.0f,
                Animation.RELATIVE_TO_SELF,
                0.0f);
        anim.setDuration(slideDuration);
        return anim;
    }

    public static Animation slideUpAnimation() {
        Animation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f,
                Animation.RELATIVE_TO_SELF,
                0.0f,
                Animation.RELATIVE_TO_SELF,
                0.0f,
                Animation.RELATIVE_TO_SELF,
                -1.0f);
        anim.setDuration(slideDuration);
        return anim;
    }

}
