package io.indy.octodo;

import io.indy.octodo.model.Task;

import java.util.List;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class TaskItemAdapter extends ArrayAdapter<Task> {

    private final String TAG = getClass().getSimpleName();
    private static final boolean D = true;

    public TaskItemAdapter(Context context, List<Task> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }
    
    private void addCheckBoxClickListener(CheckBox cb) {
        cb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox cb = (CheckBox) view;
                int id = (Integer) view.getTag();
                
                View parent = (View) view.getParent();
                TextView tv = (TextView) parent.findViewById(R.id.content);
                
                
                String s = (String) tv.getText();
                Log.d(TAG, s);

                if(cb.isChecked()) {
                    tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);                    
                    Log.d(TAG, "true isChecked on id: " + id);
                } else {
                    tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                    Log.d(TAG, "false isChecked on id: " + id);
                }
                
            }
        });        
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        Task item = getItem(position);
        
        if (convertView == null) {
            
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            //LayoutInflater layoutInflater = getLayoutInflater();
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(inflater);
            convertView = layoutInflater.inflate(R.layout.row_task, 
                                                 parent, false);
            
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.isDone);            
            addCheckBoxClickListener(cb);
        } 

        String taskString = item.getContent();
        TextView taskView = (TextView) convertView.findViewById(R.id.content);
        taskView.setText(taskString);

        Integer taskId = Integer.valueOf(item.getId());
        CheckBox isDone = (CheckBox) convertView.findViewById(R.id.isDone);
        isDone.setTag(taskId);

        return convertView;
    }
}
