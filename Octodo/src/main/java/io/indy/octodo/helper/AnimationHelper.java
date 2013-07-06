/*
 * Copyright 2013 Inderjit Gill
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.indy.octodo.helper;

import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

public class AnimationHelper {

    public final static int SLIDE_DURATION = 100;

    public static Animation slideDownAnimation() {
        return makeSlideDown(Animation.RELATIVE_TO_SELF, 1.0f);
    }

    public static Animation slideDownAnimation(float distance) {
        return makeSlideDown(Animation.ABSOLUTE, distance);
    }

    public static Animation slideUpAnimation() {
        return makeSlideUp(Animation.RELATIVE_TO_SELF, 1.0f);
    }

    public static Animation slideUpAnimation(float distance) {
        return makeSlideUp(Animation.ABSOLUTE, distance);
    }

    private static Animation makeSlideDown(int type, float distance) {
        Animation anim = new TranslateAnimation(type, 0.0f, type, 0.0f, type, -distance, type, 0.0f);
        anim.setDuration(SLIDE_DURATION);
        return anim;
    }

    private static Animation makeSlideUp(int type, float distance) {
        Animation anim = new TranslateAnimation(type, 0.0f, type, 0.0f, type, 0.0f, type, -distance);
        anim.setDuration(SLIDE_DURATION);
        return anim;
    }
}
