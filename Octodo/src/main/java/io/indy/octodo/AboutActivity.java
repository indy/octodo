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

package io.indy.octodo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockActivity {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CharSequence content = buildContent();

        TextView tv = (TextView)findViewById(R.id.about_textview);
        tv.setText(content, BufferType.SPANNABLE);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (D)
            Log.d(TAG, "clicked " + item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    CharSequence buildContent() {

        Acknowledgement sherlock = new Acknowledgement("ActionBarSherlock",
                "Copyright 2012 Jake Wharton", "http://actionbarsherlock.com",
                "Licensed under the Apache License, Version 2.0");

        Acknowledgement vpi = new Acknowledgement("ViewPagerIndicator",
                "Copyright 2012 Jake Wharton", "http://viewpagerindicator.com",
                "Licensed under the Apache License, Version 2.0");

        Acknowledgement[] thirdParties = {
                sherlock, vpi
        };

        CharSequence res = "Octodo was made with the following libraries:\n\n";
        for (Acknowledgement a : thirdParties) {
            res = TextUtils.concat(res, TextUtils.concat(a.asFormatted(), "\n"));
        }

        res = TextUtils
                .concat(res,
                        "Copyright 2013 Inderjit Gill\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.");

        return res;
    }

    private class Acknowledgement {
        public final String mLibraryName;

        public final String mCopyright;

        public final String mURL;

        public final String mLicense;

        public Acknowledgement(String libraryName, String copyright, String url, String license) {
            mLibraryName = libraryName;
            mCopyright = copyright;
            mURL = url;
            mLicense = license;
        }

        CharSequence asFormatted() {
            SpannableString ssLibraryName = new SpannableString(mLibraryName);
            ssLibraryName.setSpan(new StyleSpan(Typeface.BOLD), 0, mLibraryName.length(), 0);

            SpannableString ssCopyright = new SpannableString(mCopyright);

            SpannableString ssURL = new SpannableString(mURL);
            ssURL.setSpan(new URLSpan(mURL), 0, mURL.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString ssLicense = new SpannableString(mLicense);

            return TextUtils.concat(ssLibraryName, "\n", ssCopyright, "\n", ssURL, "\n", ssLicense,
                    "\n");
        }
    }
}
