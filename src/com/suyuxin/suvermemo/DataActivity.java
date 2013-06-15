package com.suyuxin.suvermemo;


import java.io.File;
import java.util.*;
import java.util.Map.Entry;

import com.evernote.client.android.EvernoteSession;

import com.suyuxin.suvermemo.NoteDbAdapter.NotebookInfo;

import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;

import static java.util.Collections.sort;


@SuppressLint("ValidFragment")
public class DataActivity extends Activity {

    public static final String EXTERNAL_ROOT_PATH = "suvermemo";
    private static final String CONSUMER_KEY = "suyuxin-9809";
    private static final String CONSUMER_SECRET = "f2541e0d8ea719ff";
    private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE
            = EvernoteSession.EvernoteService.PRODUCTION;

    public static EvernoteSession evernote_session;

	protected NoteDbAdapter database;
	protected Map<String, NotebookInfo> notebook_info;// notebook_guid -> NotebookInfo
	protected List<String> list_notebook_guid;//used for ListView
	protected Set<String> notebooks_having_local_contents;

    protected static final String TASK_NOTEBOOK_NAME = "任务列表";
    protected String task_notebook_guid = null;
	//common used data
	private File external_root_path;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set evernote session
		evernote_session = EvernoteSession.getInstance(this,
				CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
		//set database
		database = new NoteDbAdapter(this);
		database.open();
		//read notebook status
		notebooks_having_local_contents = database.getNotebooksHavingContents();
		database.close();
		external_root_path = new File(Environment.getExternalStorageDirectory(), EXTERNAL_ROOT_PATH);
	}
	private class NotebookComparator implements Comparator<String>
    {
        @Override
        public int compare(String left, String right) {
            //test whether has local content
            if(notebooks_having_local_contents.contains(left) == notebooks_having_local_contents.contains(right))
            {//compare note number
                int left_count = notebook_info.get(left).note_number;
                int right_count = notebook_info.get(right).note_number;
                if(left_count < right_count)
                    return 1;
                else if(left_count > right_count)
                    return -1;
                else
                    return 0;
            }
            else if(notebooks_having_local_contents.contains(left))
                return -1;
            else
                return 1;
        }
    }
	protected List<String> getNotebookNames()
	{
		database.open();
		notebook_info = database.getNotebookList();
        database.close();
        List<String> list = new ArrayList<String>();
		//if the database is empty, return to the empty tag
		if(notebook_info.size() == 0)
		{
            list.add(getResources().getString(R.string.text_empty_notebook_list));
            return list;
        }
		list_notebook_guid = new ArrayList<String>(notebook_info.keySet());
        sort(list_notebook_guid, new NotebookComparator());
		Iterator<String> iter_guid = list_notebook_guid.iterator();
		//copy notebook title into list
		while(iter_guid.hasNext())
		{   //get guid of task notebook
            String guid = iter_guid.next();//notebook guid
            if(notebook_info.get(guid).name.equals(TASK_NOTEBOOK_NAME))
                task_notebook_guid = guid;//save guid of task notebook
            else
                list.add(notebook_info.get(guid).name + " : " + notebook_info.get(guid).note_number);
		}
        list_notebook_guid.remove(task_notebook_guid);
		return list;
	}

    protected Set<String> getWordsToDownloadSound()
	{
		database.open();
		String[] local_titles = database.getTitleOfAllNotes();
		database.close();
		Set<String> words = new HashSet<String>();
		//test which word needed to be download
		for(int i = 0; i < local_titles.length; i++)
		{
			//test whether the title is a word
			if(!local_titles[i].matches("[a-zA-Z]*"))
				continue;
			//test whether the file exist
			if(!new File(external_root_path, local_titles[i] + ".wav").exists())
			{
				words.add(local_titles[i]);
			}
		}
		return words;
	}
}
