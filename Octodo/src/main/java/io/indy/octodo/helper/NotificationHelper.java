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

import android.app.Activity;
import android.content.res.Resources;
import android.view.ViewGroup.LayoutParams;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Style.Builder;
import io.indy.octodo.R;

// Show the user notifications
public class NotificationHelper {

    private Activity mActivity;

    private Style mConfirmStyle;

    public NotificationHelper(Activity activity) {
        mActivity = activity;

        Resources res = activity.getResources();

        int octodoGreen = res.getColor(R.color.green);
        int duration = res.getInteger(R.integer.notification_duration);

        mConfirmStyle = new Builder().setDuration(duration)
            .setBackgroundColorValue(octodoGreen)
            .setHeight(LayoutParams.WRAP_CONTENT)
            .build();
    }

    public void cancelAllNotifications() {
        Crouton.cancelAllCroutons();
    }

    public void showConfirmation(String message) {
        Crouton.makeText(mActivity, message, mConfirmStyle).show();
    }
}
