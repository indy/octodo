
package io.indy.octodo.helper;

import android.app.Activity;
import android.view.ViewGroup.LayoutParams;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.keyboardsurfer.android.widget.crouton.Style.Builder;

// Show the user notifications
public class NotificationHelper {

    private Activity mActivity;

    private static final int OCTODO_GREEN = 0xdd00eb45;

    private static final Style CONFIRM = new Builder().setDuration(1000)
            .setBackgroundColorValue(OCTODO_GREEN).setHeight(LayoutParams.WRAP_CONTENT).build();

    public NotificationHelper(Activity activity) {
        mActivity = activity;
    }

    public static void cancelAllNotifications() {
        Crouton.cancelAllCroutons();
    }

    public void showConfirmation(String message) {
        Crouton.makeText(mActivity, message, NotificationHelper.CONFIRM).show();
    }
}
