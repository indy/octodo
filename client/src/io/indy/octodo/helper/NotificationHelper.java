
package io.indy.octodo.helper;

import android.app.Activity;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

// Show the user notifications
public class NotificationHelper {

    private Activity mActivity;

    public NotificationHelper(Activity activity) {
        mActivity = activity;
    }

    public void showConfirmation(String message) {
        Crouton.makeText(mActivity, message, Style.CONFIRM).show();
    }
}
