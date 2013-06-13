package com.suyuxin.suvermemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by suyuxin on 13-5-27.
 */
public class TaskPanel extends DataActivity {

    private Map<String, NoteDbAdapter.TaskInfo> task_info;
    private String task_notebook_guid;
    private ListView listView_important_urgent;
    private ListView listView_unimportant_urgent;
    private ListView listView_important_not_urgent;
    private ListView listView_unimportant_not_urgent;
    private ListView listView_daily;

    protected class NormalTaskAdapter extends ArrayAdapter<String>
    {
        private NormalTaskAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
            super(context, resource, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if(convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.task_row,
                        parent, false);
            }
            //set content
            TextView text = (TextView)convertView.findViewById(R.id.textView_task_name);
            text.setText(getItem(position));
            text.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    String task_name = getItem(position);
                    //seek guid of current task
                    Iterator<String> iter_guid = task_info.keySet().iterator();
                    while(iter_guid.hasNext())
                    {
                        NoteDbAdapter.TaskInfo task = task_info.get(iter_guid.next());
                        if(task_name.endsWith(task.title))
                        {//open task content
                            Intent intent = new Intent(getContext(), TaskContent.class);
                            intent.putExtra("title", task.title);
                            intent.putExtra("guid", task.guid);
                            intent.putExtra("content", task.content);
                            startActivity(intent);
                            break;
                        }
                    }
                }
            });
            return convertView;
        }

    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_panel);
        //get task info
        task_notebook_guid = getIntent().getStringExtra("notebook_guid");
        database.open();
        task_info = database.getLocalTasks(task_notebook_guid);
        database.close();
        //set listView
        //set important and urgent list
        listView_important_urgent = (ListView)findViewById(R.id.listView_important_urgent);
        listView_important_urgent.setAdapter(
                new NormalTaskAdapter(this, R.layout.task_row, R.id.textView_task_name,
                                        getNormalTaskNames(true, true)));
        //set unimportant and urgent list
        listView_unimportant_urgent = (ListView)findViewById(R.id.listView_unimportant_urgent);
        listView_unimportant_urgent.setAdapter(
                new NormalTaskAdapter(this, R.layout.task_row, R.id.textView_task_name,
                                        getNormalTaskNames(false, true)));
        //set important but not urgent list
        listView_important_not_urgent = (ListView)findViewById(R.id.listView_important_not_urgent);
        listView_important_not_urgent.setAdapter(
                new NormalTaskAdapter(this, R.layout.task_row, R.id.textView_task_name,
                        getNormalTaskNames(true, false)));
        //set unimportant but not urgent list
        listView_unimportant_not_urgent = (ListView)findViewById(R.id.listView_unimportant_not_urgent);
        listView_unimportant_not_urgent.setAdapter(
                new NormalTaskAdapter(this, R.layout.task_row, R.id.textView_task_name,
                        getNormalTaskNames(false, false)));
        //set daily list
        listView_daily = (ListView)findViewById(R.id.listView_daily);
        listView_daily.setAdapter(new NormalTaskAdapter(this, R.layout.task_row, R.id.textView_task_name,
                getDailyTaskNames()));

    }

    private List<String> getNormalTaskNames(boolean is_important, boolean is_urgent)
    {
        List<String> list = new ArrayList<String>();
        Iterator<String> iter_guid = task_info.keySet().iterator();
        int index = 1;
        while(iter_guid.hasNext())
        {
            NoteDbAdapter.TaskInfo task = task_info.get(iter_guid.next());
            if(task.is_important == is_important && task.is_urgent == is_urgent && task.is_daily == false)
            {
                list.add(String.valueOf(index) + " : " + task.title);
                index++;
            }
        }
        return list;
    }

    private List<String> getDailyTaskNames()
    {
        List<String> list = new ArrayList<String>();
        Iterator<String> iter_guid = task_info.keySet().iterator();
        int index = 1;
        while(iter_guid.hasNext())
        {
            NoteDbAdapter.TaskInfo task = task_info.get(iter_guid.next());
            if(task.is_daily == true)
            {
                list.add(String.valueOf(index) + " : " + task.title);
                index++;
            }
        }
        return list;
    }

}