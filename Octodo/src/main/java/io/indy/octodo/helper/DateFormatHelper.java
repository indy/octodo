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

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
