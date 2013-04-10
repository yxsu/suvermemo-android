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


@SuppressLint("ValidFragment")
public class DataActivity extends Activity {

	//basic information about application
	private static final String CONSUMER_KEY = "suyuxin-9809";
	private static final String CONSUMER_SECRET = "f2541e0d8ea719ff";
	private static final EvernoteSession.EvernoteService EVERNOTE_SERVICE
	  	= EvernoteSession.EvernoteService.PRODUCTION;
		
	protected EvernoteSession evernote_session;
	protected final int DIALOG_PROGRESS = 101;
	
	//common used data
	protected String[] notebook_names = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set evernote session
		evernote_session = EvernoteSession.getInstance(this,
				CONSUMER_KEY, CONSUMER_SECRET, EVERNOTE_SERVICE);
		//
		
		
	}
	
	

	  @SuppressWarnings("deprecation")
	  @Override
	  protected Dialog onCreateDialog(int id) {
	    switch (id) {
	      case DIALOG_PROGRESS:
	        return new ProgressDialog(DataActivity.this);
	    }
	    return super.onCreateDialog(id);
	  }

	  @Override
	  @SuppressWarnings("deprecation")
	  protected void onPrepareDialog(int id, Dialog dialog) {
	    switch (id) {
	      case DIALOG_PROGRESS:
	        ((ProgressDialog) dialog).setIndeterminate(true);
	        dialog.setCancelable(false);
	        ((ProgressDialog) dialog).setMessage(getString(R.string.text_load));
	    }
	  }
}
