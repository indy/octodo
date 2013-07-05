package io.indy.octodo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import io.indy.octodo.model.DriveDatabase;
import io.indy.octodo.model.DriveManager;

public abstract class DriveBaseActivity extends SherlockFragmentActivity {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    protected DriveManager mDriveManager;
    protected DriveDatabase mDriveDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (D) {
            Log.d(TAG, "onCreate");
        }

        mDriveManager = new DriveManager(this);
        mDriveDatabase = new DriveDatabase(mDriveManager);
    }

    public abstract void onDriveInitialised();

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        mDriveManager.onActivityResult(requestCode, resultCode, data);
    }

}
