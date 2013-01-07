package io.indy.octodo;

import io.indy.octodo.model.Task;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TaskItemAdapter extends ArrayAdapter<Task> {

    public TaskItemAdapter(Context context, List<Task> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (convertView == null) {

            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            //LayoutInflater layoutInflater = getLayoutInflater();
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            convertView = layoutInflater.inflate(R.layout.task_row, 
                                                 parent, false);
        }

        Task item = getItem(position);
        String taskString = item.getContent();
        TextView taskView = (TextView) convertView.findViewById(R.id.content);

        taskView.setText(taskString);
        
        return convertView;
    }
}
