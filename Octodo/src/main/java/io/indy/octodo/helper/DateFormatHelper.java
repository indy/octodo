
package io.indy.octodo.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.format.DateUtils;

public class DateFormatHelper {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static String formatTimeSpan(String timeToFormat) {

        String timeSpan = "";

        SimpleDateFormat iso8601Format = new SimpleDateFormat(DATE_FORMAT);

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }
            if (date != null) {
                long time = date.getTime();
                timeSpan = (String)DateUtils.getRelativeTimeSpanString(time);
                // Log.d(TAG, "timeSpan is " + timeSpan);
            }
        }
        return timeSpan;
    }

    public static String today() {
        Date date = new Date();
        String today = new SimpleDateFormat(DATE_FORMAT).format(date);
        return today;
    }

}
