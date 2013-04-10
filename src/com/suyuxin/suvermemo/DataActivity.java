package com.suyuxin.suvermemo;


import java.util.List;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

import android.os.Bundle;
import android.widget.Toast;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;


@SuppressLint("ValidFragment")
public class DataActivity extends Activity {

	//basic information about application
	private static final String CONSUMER_KEY = "suyuxin-9809";
	private static final String CONSUMER_SECRET = "f2541e0d8ea719ff";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE
	  	= EvernoteSession.EvernoteService.PRODUCTION;
		
	protected EvernoteSession evernote_session;
	
	protected NoteDbAdapter database;
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
		Cursor cursor = database.getNotebookList();
		//if the database is empty, return to the empty tag
		if(cursor.getCount() == 0)
		{
			database.close();
			return new String[]{getResources().getString(R.string.text_empty_notebook_list)};
		}
		String[] names = new String[cursor.getCount()];
		int index = 0;
		if(cursor.moveToFirst())
		{
			do {
				names[index] = cursor.getString(1);
				index++;
			}while(cursor.moveToNext());
		}
		database.close();
		return names;
	}
	
}
