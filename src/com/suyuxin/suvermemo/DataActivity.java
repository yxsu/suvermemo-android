package com.suyuxin.suvermemo;


import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;
import com.suyuxin.suvermemo.NoteDbAdapter.NotebookInfo;

import android.os.Bundle;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;


@SuppressLint("ValidFragment")
public class DataActivity extends Activity {

	//basic information about application
	private static final String CONSUMER_KEY = "suyuxin-9809";
	private static final String CONSUMER_SECRET = "f2541e0d8ea719ff";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE
	  	= EvernoteSession.EvernoteService.PRODUCTION;
		
	protected EvernoteSession evernote_session;
	
	protected NoteDbAdapter database;
	protected Map<String, NotebookInfo> notebook_info;// notebook_guid -> NotebookInfo
	protected String[] list_notebook_guid;//used for ListView
	//common used data
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set evernote session
		evernote_session = EvernoteSession.getInstance(this,
				CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
		//set database
		database = new NoteDbAdapter(this);
		
		
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
		String[] names = new String[notebook_info.size()];
		list_notebook_guid = new String[notebook_info.size()];
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
	
}
