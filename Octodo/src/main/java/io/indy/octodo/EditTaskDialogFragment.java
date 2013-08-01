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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import io.indy.octodo.controller.MainController;
import io.indy.octodo.model.Task;
import io.indy.octodo.model.TaskList;

public class EditTaskDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

    static private final boolean D = true;
    static private final String TAG = EditTaskDialogFragment.class.getSimpleName();

    void ifd(final String message) {
        if (D) Log.d(TAG, message);
    }

    private MainController mController;
    private Task mTask;

    private List<String> mListNames;
    private boolean mSetInitialSpinnerValue;

    private Spinner mSpinner;
    private EditText mEditText;


    public EditTaskDialogFragment(MainController controller, Task task) {
        super();

        mController = controller;
        mTask = task;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.dialog_edit_task, null);
        builder.setView(v);

        builder.setMessage(R.string.edit_task_title);

        builder.setPositiveButton(R.string.edit_task_positive, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onClickedPositive();
            }
        });
        builder.setNegativeButton(R.string.dialog_generic_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        // Create the AlertDialog object and return it
        AlertDialog alertDialog = builder.create();

        mSpinner = (Spinner) v.findViewById(R.id.spinner);
        mEditText = (EditText) v.findViewById(R.id.editText);

        mSpinner.setOnItemSelectedListener(this);

        Button delete = (Button) v.findViewById(R.id.button);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickedDelete();
            }
        });

        return alertDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ifd("onAttach");
        ifd("attaching to activity: " + System.identityHashCode(activity));
    }


    // Called at the start of the visible lifetime.
    @Override
    public void onStart() {
        super.onStart();
        ifd("onStart");

        init();
    }

    private void init() {
        mListNames = getListNames();
        mSetInitialSpinnerValue = true;
        populateSpinnerEntries(mListNames);
        mEditText.setText(mTask.getContent());
    }

    private List<String> getListNames() {
        List<TaskList> taskLists = mController.onGetTaskLists();
        List<String> listNames = new ArrayList<String>();

        for (TaskList taskList : taskLists) {
            listNames.add(taskList.getName());
        }

        return listNames;
    }

    private void populateSpinnerEntries(List<String> listNames) {

        ArrayAdapter<String> aa = new ArrayAdapter<String>(getDialog().getContext(),
                android.R.layout.simple_spinner_item,
                listNames
        );

        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(aa);
    }

    private void onClickedPositive() {
        int spinnerPosition = mSpinner.getSelectedItemPosition();
        String listName = mListNames.get(spinnerPosition);
        String content = mEditText.getText().toString().trim();

        ifd("saveTask: listName: " + listName + ", content: " + content);

        mController.onTaskEdited(mTask, content, listName);
    }

    private void onClickedDelete() {
        ifd("clicked delete");
        mController.onTaskDelete(mTask);
        getDialog().dismiss();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent,
                               View v, int position, long id) {

        if (mSetInitialSpinnerValue) {
            mSetInitialSpinnerValue = false;

            // set the spinner's initial value to the task's parent name
            String parentName = mTask.getParentName();
            for (int i = 0; i < mListNames.size(); i++) {
                if (parentName.equals(mListNames.get(i))) {
                    mSpinner.setSelection(i);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        ifd("nothing selected");
    }
}
