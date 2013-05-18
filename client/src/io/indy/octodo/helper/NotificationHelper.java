
package io.indy.octodo.helper;

import io.indy.octodo.R;
import android.app.Activity;
import android.content.res.Resources;
import android.view.ViewGroup.LayoutParams;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Style.Builder;

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
