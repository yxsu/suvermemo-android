package com.suyuxin.suvermemo;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;
import com.suyuxin.suvermemo.NoteDbAdapter.NotebookInfo;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;


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
	protected String[] list_notebook_guid;//used for ListView
	protected Set<String> notebooks_having_local_contents;
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
	
	protected String[] getNotebookNames()
	{
		database.open();
		notebook_info = database.getNotebookList();
		//if the database is empty, return to the empty tag
		if(notebook_info.size() == 0)
		{
			database.close();
			return new String[]{getResources().getString(R.string.text_empty_notebook_list)};
		}
		String[] names = new String[notebook_info.size() + 1];
		list_notebook_guid = new String[notebook_info.size()];
		names[notebook_info.size()] = getResources().getString(R.string.text_sync_sound_file);
		int index = 0;
		//iterate the map of notebook_info
		Iterator<Entry<String, NotebookInfo>> iter_notebook = notebook_info.entrySet().iterator();
		while(iter_notebook.hasNext())
		{
			Entry<String, NotebookInfo> iter = iter_notebook.next();
			names[index] = iter.getValue().name + " : " + iter.getValue().note_number;
			list_notebook_guid[index] = iter.getKey();
			index++;
		}
		database.close();
		return names;
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
