package com.suyuxin.suvermemo;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;
import com.evernote.edam.type.Note;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by suyuxin on 13-6-13.
 */
public class TaskContent extends Activity implements View.OnClickListener{
    private ProgressAdapter adapter;
    private String title;
    private String guid_task;
    private String raw_content;
    private String task_notebook_guid;
    private List<String[]> progress;
    private String tag_today;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set tag string of today
        Calendar today = Calendar.getInstance();
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);
        tag_today = String.valueOf(month) + "." + String.valueOf(day);
        //get intent data
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        guid_task = intent.getStringExtra("guid");
        raw_content = intent.getStringExtra("content");
        task_notebook_guid = intent.getStringExtra("notebook_guid");
        ReadProgressFromRawContent();
        //set view
        setContentView(R.layout.activity_task_content);
        TextView tv_title = (TextView)findViewById(R.id.textView_title);
        tv_title.setText(title);
        WebView wv_content = (WebView)findViewById(R.id.webView_task_content);
        wv_content.loadData(ReadTaskContentFromRawContent(), "text/html; charset=UTF-8", null);
        //set progress
        ListView view_progress = (ListView)findViewById(R.id.listView_task_progress);
        adapter = new ProgressAdapter(this, R.id.editText_date, progress);
        view_progress.setAdapter(adapter);
        //set button
        ImageButton button_edit = (ImageButton)findViewById(R.id.imageButton_edit_today_progress);
        button_edit.setOnClickListener(this);
        ImageButton button_set_alarm = (ImageButton)findViewById(R.id.imageButton_set_alarm);
        button_set_alarm.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.imageButton_edit_today_progress)
        {//add progress and save it
            ProgressInputDialog dialog = new ProgressInputDialog();
            //set argument
            Bundle data = new Bundle();
            if(progress.isEmpty() || !progress.get(progress.size()-1)[0].equals(tag_today))
            {//add new progress content
                data.putString("content", null);
                data.putString("extra", null);
            }
            else
            {//modify existed progress content
                data.putString("content", progress.get(progress.size()-1)[1]);
                data.putString("extra", progress.get(progress.size()-1)[2]);
            }
            dialog.setArguments(data);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            dialog.show(ft, "progress-input-dialog");
        }
    }

    private void ReadProgressFromRawContent()
    {
        //find the table
        int table_index = raw_content.indexOf("<table border");
        if(table_index <= 0)
        {
            progress = new ArrayList<String[]>();
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

    private String ReadTaskContentFromRawContent()
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

    private void UpdateTodayProgress(String content, String extra)
    {
        if(progress.isEmpty() || !progress.get(progress.size()-1)[0].equals(tag_today))
        {//add new content
            progress.add(new String[]{tag_today, content, extra});
            SaveChangeIntoDatabase(true);
        }
        else
        {//modify existed content
            progress.get(progress.size()-1)[1] = content;
            progress.get(progress.size()-1)[2] = extra;
            SaveChangeIntoDatabase(false);
        }
        adapter.notifyDataSetChanged();
    }

    private void SaveChangeIntoDatabase(boolean is_new_day)
    {
        String new_raw_content;
        String[] today_content = progress.get(progress.size() - 1);
        //modify raw content
        if(is_new_day)
        {//add new content
            int end_index = raw_content.indexOf("</tbody>", raw_content.indexOf("<table"));
            new_raw_content = raw_content.substring(0, end_index) + "<tr>\n";
            for(String col : today_content)
            {
                if(col != null && !col.equals(""))
                    new_raw_content += "<td valign=\"top\">" + col + "</td>\n";
            }
            new_raw_content += "</tr>\n";
        }
        else
        {//modify the last item
            int last_content_index = raw_content.indexOf("<td valign=\"top\">",
                    raw_content.indexOf("<td valign=\"top\">" + tag_today) + 10) + 16;
            new_raw_content = raw_content.substring(0, last_content_index) + today_content[1] + "</td>\n";
            if(today_content[2] != null)
                new_raw_content += "<td valign=\"top\">" + today_content[2] + "</td>\n</tr>\n";
        }
        new_raw_content += "</tbody>\n</table>\n</div>\n</en-note>";
        //save into database
        NoteDbAdapter database = new NoteDbAdapter(this);
        database.open();
        Note note = new Note();
        note.setContent(new_raw_content);
        note.setTitle(title);
        note.setGuid(guid_task);
        note.setNotebookGuid(task_notebook_guid);
        note.setUpdated(Calendar.getInstance().getTimeInMillis());
        database.updateNoteContent(note);
        database.close();
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

    private class ProgressInputDialog extends DialogFragment implements View.OnClickListener
    {
        private EditText edit_text_progress_content;
        private EditText edit_text_progress_extra;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.dialog_input_task_progress, container, false);
            Bundle data = getArguments();
            edit_text_progress_content = (EditText)view.findViewById(R.id.editText_today_progress_content);
            edit_text_progress_content.setText(data.getString("content"));
            edit_text_progress_extra = (EditText)view.findViewById(R.id.editText_today_progress_extra);
            edit_text_progress_extra.setText(data.getString("extra"));
            ImageButton button_save = (ImageButton)view.findViewById(R.id.imageButton_today_progress_ok);
            button_save.setOnClickListener(this);
            return view;
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.imageButton_today_progress_ok)
            {
                UpdateTodayProgress(edit_text_progress_content.getText().toString(),
                        edit_text_progress_extra.getText().toString());
                dismiss();
            }
        }
    }
}