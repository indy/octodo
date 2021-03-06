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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import io.indy.octodo.model.DriveStorage;

public abstract class DriveBaseActivity extends SherlockFragmentActivity {

    static private final boolean D = false;
    static private final String TAG = DriveBaseActivity.class.getSimpleName();

    static void ifd(final String message) {
        if (AppConfig.DEBUG && D) Log.d(TAG, message);
    }

    protected DriveStorage mDriveStorage;

    protected boolean mDriveDatabaseInitialised;

    protected boolean mHasDriveCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ifd("onCreate");

        mHasDriveCredentials = false;
        mDriveDatabaseInitialised = false;
        mDriveStorage = new DriveStorage(this);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        mDriveStorage.onActivityResult(requestCode, resultCode, data);
    }

    // need drive credentials before the drive database can be initialised
    public boolean hasDriveCredentials() {
        return mHasDriveCredentials;
    }

    public void driveCredentialsApproved() {
        mHasDriveCredentials = true;
    }

    public void onDriveDatabaseInitialised() {
        ifd("onDriveDatabaseInitialised");

        if(!mHasDriveCredentials) {
            // todo: throw an exception
        }

        mDriveDatabaseInitialised = true;
    }

    public boolean isDriveDatabaseInitialised() {
        return mDriveDatabaseInitialised;
    }

    public DriveStorage getDriveStorage() {
        return mDriveStorage;
    }

}
