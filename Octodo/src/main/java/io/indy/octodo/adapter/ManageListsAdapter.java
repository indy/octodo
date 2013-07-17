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

package io.indy.octodo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import io.indy.octodo.model.TaskList;
import io.indy.octodo.view.ManageListItemView;

public class ManageListsAdapter extends ArrayAdapter<TaskList> {

    static private final boolean D = true;
    static private final String TAG = ManageListsAdapter.class.getSimpleName();
    static void ifd(final String message) { if(D) Log.d(TAG, message); }

    public ManageListsAdapter(Context context, List<TaskList> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        ifd("getView position: " + position);

        ManageListItemView manageListItemView = (ManageListItemView)v;
        if (manageListItemView == null) {
            manageListItemView = new ManageListItemView(getContext());
        }

        manageListItemView.setup(getItem(position));

        return manageListItemView;
    }
}
