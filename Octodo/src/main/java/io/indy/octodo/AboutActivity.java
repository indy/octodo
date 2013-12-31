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

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends SherlockActivity {

    static private final boolean D = true;
    static private final String TAG = AboutActivity.class.getSimpleName();

    static void ifd(final String message) {
        if (AppConfig.DEBUG && D) Log.d(TAG, message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView version = (TextView)findViewById(R.id.version);
        version.setText("v" + AppConfig.VERSION_NAME + (AppConfig.DEBUG ? "d" : "r"));

        TextView source = (TextView)findViewById(R.id.source_link);
        asLink(source, "https://github.com/indy/octodo");

        CharSequence res = "";
        Acknowledgement[] thirdParties = {
                new Acknowledgement("ActionBarSherlock",
                        "http://actionbarsherlock.com",
                        "Licensed under the Apache License, Version 2.0"),
                new Acknowledgement("ViewPagerIndicator",
                        "http://viewpagerindicator.com",
                        "Licensed under the Apache License, Version 2.0"),
                new Acknowledgement("EventBus",
                        "https://github.com/greenrobot/EventBus",
                        "Licensed under the Apache License, Version 2.0")
        };
        for (Acknowledgement a : thirdParties) {
            res = TextUtils.concat(res, TextUtils.concat(a.asFormatted(), "\n", "\n"));
        }
        res = res.subSequence(0, res.length() - 2);

        TextView ackView = (TextView)findViewById(R.id.acknowledgements);
        ackView.setText(res);
        ackView.setMovementMethod(LinkMovementMethod.getInstance());

        TextView email = (TextView)findViewById(R.id.textViewEmail);
        asLink(email, "mailto://octodo@indy.io", "octodo@indy.io");
        TextView google = (TextView)findViewById(R.id.textViewGooglePlus);
        asLink(google, "https://google.com/+InderjitGill", "+InderjitGill");
        TextView twitter = (TextView)findViewById(R.id.textViewTwitter);
        asLink(twitter, "https://twitter.com/InderjitGill", "@InderjitGill");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ifd("clicked " + item);

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void asLink(TextView source, String url, String urlText) {
        SpannableString ssURL = new SpannableString(urlText);
        ssURL.setSpan(new URLSpan(url), 0, urlText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        source.setText(ssURL, TextView.BufferType.SPANNABLE);
        source.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void asLink(TextView source, String url) {
        SpannableString ssURL = new SpannableString(url);
        ssURL.setSpan(new URLSpan(url), 0, url.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        source.setText(ssURL, TextView.BufferType.SPANNABLE);
        source.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private static class Acknowledgement {
        public final String mLibraryName;
        public final String mUrl;
        public final String mLicense;

        public Acknowledgement(String libraryName, String url, String license) {
            mLibraryName = libraryName;
            mUrl = url;
            mLicense = license;
        }

        CharSequence asFormatted() {
            SpannableString libraryName = new SpannableString(mLibraryName);
            libraryName.setSpan(new StyleSpan(Typeface.BOLD), 0, mLibraryName.length(), 0);

            SpannableString url = new SpannableString(mUrl);
            url.setSpan(new URLSpan(mUrl), 0, mUrl.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            SpannableString ssLicense = new SpannableString(mLicense);

            return TextUtils.concat(libraryName, "\n", url, "\n", ssLicense);
        }
    }
}
