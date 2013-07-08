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

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.List;

import io.indy.octodo.adapter.ManageListsAdapter;
import io.indy.octodo.async.HistoricTaskListsAsyncTask;
import io.indy.octodo.async.TaskListsAsyncTask;
import io.indy.octodo.controller.MainController;
import io.indy.octodo.helper.AnimationHelper;
import io.indy.octodo.model.TaskList;

public class ManageListsActivity extends DriveBaseActivity implements OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private static final boolean D = true;

    private List<TaskList> mTaskLists;

    private ManageListsAdapter mAdapter;

    private MainController mController;

    private ListView mListView;

    private LinearLayout mSectionAddList;

    private Button mButtonAddList;

    private EditText mEditText;

    public void onDriveInitialised() {
        Log.d(TAG, "onDriveInitialised");
        new TaskListsAsyncTask(mDriveDatabase).execute();
        new HistoricTaskListsAsyncTask(mDriveDatabase).execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_lists);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListView = (ListView)findViewById(R.id.listViewTaskLists);

        mController = new MainController(this, mDriveDatabase);

        /*
        mTaskLists = mController.getDeleteableTaskLists();

        mAdapter = new ManageListsAdapter(this, mTaskLists);

        mSectionAddList = (LinearLayout)findViewById(R.id.sectionAddList);
        mButtonAddList = (Button)findViewById(R.id.buttonAddList);
        mEditText = (EditText)findViewById(R.id.editText);

        // Bind the Adapter to the List View
        mListView.setAdapter(mAdapter);

        mButtonAddList.setOnClickListener(this);
        */

        mDriveManager.initialise();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) {
            Log.d(TAG, "onDestroy");
        }
        mController.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_manage_lists, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_discard_lists:
                for (TaskList tl : mTaskLists) {
                    if (tl.isSelected()) {
                        mController.deleteList(tl.getId());
                    }
                }
                refreshTaskLists();
                // send an event to MainActivity?
                break;
            case R.id.menu_add_list:
                toggleAddListView();
                break;
            case android.R.id.home:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (D) {
            Log.d(TAG, "onClick");
        }
        String name = mEditText.getText().toString();
        mController.addList(name);

        refreshTaskLists();

        mEditText.setText("");
    }

    private void refreshTaskLists() {
        List<TaskList> taskLists = mController.getDeleteableTaskLists();
        mTaskLists.clear();
        mTaskLists.addAll(taskLists);
        mAdapter.notifyDataSetChanged();
    }

    private void toggleAddListView() {
        if (D) {
            Log.d(TAG, "toggleAddListView");
        }

        Animation anim;

        if (mSectionAddList.getVisibility() == View.GONE) {
            anim = AnimationHelper.slideDownAnimation();
            mSectionAddList.startAnimation(anim);
            mSectionAddList.setVisibility(View.VISIBLE);
        } else {
            anim = AnimationHelper.slideUpAnimation();
            mSectionAddList.startAnimation(anim);
            mSectionAddList.setVisibility(View.GONE);
        }
    }

}
