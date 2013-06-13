package com.suyuxin.suvermemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by suyuxin on 13-6-13.
 */
public class TaskContent extends Activity {
    private ListView view_progress;
    private String title;
    private String guid_task;
    private List<String[]> progress;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get intent data
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        guid_task = intent.getStringExtra("guid");
        String raw_content = intent.getStringExtra("content");
        ReadProgressFromRawContent(raw_content);
        //set view
        setContentView(R.layout.activity_task_content);
        TextView tv_title = (TextView)findViewById(R.id.textView_title);
        tv_title.setText(title);
        EditText et_content = (EditText)findViewById(R.id.editText_content);
        et_content.setText(ReadTaskContentFromRawContent(raw_content));
        //set progress
        view_progress = (ListView)findViewById(R.id.listView_task_progress);
        view_progress.setAdapter(new ProgressAdapter(this, R.id.editText_date, progress));
    }

    private void ReadProgressFromRawContent(String raw_content)
    {
        //find the table
        int table_index = raw_content.indexOf("<table border");
        if(table_index <= 0)
        {
            progress = null;
            return;
        }
        progress = new ArrayList<String[]>();
        table_index = raw_content.indexOf("<tbody", table_index);
        String[] split_result = raw_content.substring(table_index).split("\n");
        //add table into list
        Queue<String> col_queue = new LinkedList<String>();
        for(String row : split_result)
        {
            if(row.startsWith("<td valign"))
                col_queue.add(row.substring(17, row.length() - 5));
            if(row.equals("</tr>"))
            {
                progress.add(new String[]{col_queue.poll(), col_queue.poll(), col_queue.poll()});
                col_queue.clear();
            }
        }
    }

    private String ReadTaskContentFromRawContent(String raw_content)
    {
        int table_index = raw_content.indexOf("<table");
        if(table_index > 0)
        {
            return raw_content.substring(0, table_index) + "</div></en-note>";
        }
        else
        {
            return raw_content;
        }
    }

    private class ProgressAdapter extends ArrayAdapter<String[]>
    {

        private ProgressAdapter(Context context, int textViewResourceId, List<String[]> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.row_task_progress,
                        parent, false);
            }
            EditText et_date = (EditText)convertView.findViewById(R.id.editText_date);
            et_date.setText(getItem(position)[0]);
            EditText et_progress = (EditText)convertView.findViewById(R.id.editText_task_progress_content);
            et_progress.setText(getItem(position)[1]);
            EditText et_extra = (EditText)convertView.findViewById(R.id.editText_task_progress_extra);
            et_extra.setText(getItem(position)[2]);
            return convertView;
        }
    }
}