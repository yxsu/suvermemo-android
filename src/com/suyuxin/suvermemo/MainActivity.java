package com.suyuxin.suvermemo;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteCollectionCounts;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.TException;
import com.evernote.thrift.transport.TTransportException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends DataActivity{
	
	
	private class NotebookListAdapter extends ArrayAdapter<String> {

		public NotebookListAdapter(Context context, int resource,
				int textViewResourceId, String[] objects) {
			super(context, resource, textViewResourceId, objects);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView == null)
			{
				convertView = LayoutInflater.from(getContext()).inflate(R.layout.notebook_row,
						parent, false);
			}
			//set notebook name
			TextView text = (TextView)convertView.findViewById(R.id.text_notebook_name);
			text.setText(getItem(position));
			//set sync button of each notebook in list
			Button sync = (Button)convertView.findViewById(R.id.button_notebook_sync);
			sync.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if(getItem(position).equals(getResources().getString(R.string.text_empty_notebook_list)))
					{
						if(!evernote_session.isLoggedIn())
						{
							evernote_session.authenticate(getContext());
						}
						new UpdateNotebookList().execute();
					}
					else
					{//normally update content of a notebook
						new UpdateNotebookList().execute();
						
					}
				}
			});
			return convertView;
		}
		
	}
	
	private class DownloadNotes extends AsyncTask<String, Integer, Void>
	{
		ProgressBar progress_bar = null;
		private final int progress_bar_max_value = 10000;
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(progress_bar == null)
			{
				progress_bar = (ProgressBar)findViewById(R.id.progressBar_sync);
			}
			progress_bar.setMax(progress_bar_max_value);
			progress_bar.setProgress(0);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			progress_bar.setProgress(values[0]);
		}

		@SuppressWarnings("finally")
		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			String auth_token = evernote_session.getAuthToken();
			String notebook_guid = params[0];
			try {
				NoteStore.Client client = evernote_session.getClientFactory().createNoteStoreClient().getClient();
				//set notebook filter
				NoteFilter filter = new NoteFilter();
				filter.setNotebookGuid(notebook_guid);
				//get note list
				int offset = 0;
				List<Note> note_list = new ArrayList<Note>();
				NoteList list_buffer;
				do
				{
					list_buffer = client.findNotes(auth_token, filter, offset, 20000);
					offset += list_buffer.getNotesSize();
					note_list.addAll(list_buffer.getNotes());
					//update progress
					publishProgress(offset * progress_bar_max_value / list_buffer.getTotalNotes());
				}while(list_buffer.getTotalNotes() > note_list.size());
				//get note content
				database.open();
				for(int index = 0; index < note_list.size(); index++)
				{
					publishProgress(index);
					//download content
					Note note = note_list.get(index);
					note.setContent(client.getNoteContent(auth_token, note.getGuid()));
					//save to database
					database.insertNote(note, note.getUpdated(), 0);
				}
					
			}catch(Exception e) {
				Toast.makeText(getApplication(), R.string.error_download_note, Toast.LENGTH_LONG).show();
				
			}finally {
				database.close();
				return null;	
			}
		}
		
	}
	
	private class UpdateNotebookList extends AsyncTask<Void, Integer, Void>
	{
		private static final String LogTag = "UpdateNotebookList";
		ProgressBar progress_bar = null;
		private final int progress_bar_max_value = 200;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			if(progress_bar == null)
			{
				progress_bar = (ProgressBar)findViewById(R.id.progressBar_sync);
			}
			progress_bar.setMax(progress_bar_max_value);
			progress_bar.setProgress(0);
		}

		@Override
		protected void onPostExecute(Void names) {
			// TODO Auto-generated method stub
			super.onPostExecute(names);
			ListView view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
			NotebookListAdapter adapter = new NotebookListAdapter(getBaseContext(),
					R.layout.notebook_row,
					R.id.text_notebook_name,
					getNotebookNames());
			view_notebook_list.setAdapter(adapter);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			progress_bar.setProgress(values[0]);
		}

		@SuppressWarnings("finally")
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub	
			String auth_token = evernote_session.getAuthToken();
			try {
				NoteStore.Client client = evernote_session.getClientFactory().createNoteStoreClient().getClient();
				List<Notebook> cloud_notebooks = client.listNotebooks(auth_token);
				//get count of notes in each notebook
				Map<String, Integer> cloud_counts = client.findNoteCounts(auth_token, new NoteFilter(), false)
						.getNotebookCounts();
				//update progress
				publishProgress(progress_bar_max_value / 3);
				//open database
				database.open();
				notebook_info = database.getNotebookList();
				publishProgress(progress_bar_max_value / 2);
				ListIterator<Notebook> iterator = cloud_notebooks.listIterator();
				//for update progress
				int index = 0;
				int progress_slice = progress_bar_max_value / (2 * cloud_notebooks.size());
				while(iterator.hasNext())
				{
					Notebook notebook = iterator.next();
					String guid = notebook.getGuid();
					if(notebook_info.containsKey(guid))
					{
						if(notebook_info.get(guid).note_number != cloud_counts.get(guid))
						{//need to update
							database.updateNotebook(notebook.getName(), guid, cloud_counts.get(guid));
						}
					}
					else
					{//store new notebook into database
						database.insertNotebook(notebook.getName(), guid, cloud_counts.get(guid));
					}
					//update progress
					index++;
					publishProgress(progress_bar_max_value / 2 + progress_slice * index);
				}
				publishProgress(progress_bar_max_value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(LogTag, e.toString(), e);
				database.close();
			}finally{
				database.close();
				return null;
			}
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set adapter of notebook list
		setContentView(R.layout.activity_main);
		ListView view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
		NotebookListAdapter adapter = new NotebookListAdapter(this,
				R.layout.notebook_row,
				R.id.text_notebook_name,
				getNotebookNames());
		view_notebook_list.setAdapter(adapter);
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
	      case EvernoteSession.REQUEST_CODE_OAUTH:
	    	// update notebook list when oauth activity returns result
	        if (resultCode == Activity.RESULT_OK) {
	        	new UpdateNotebookList().execute();
	        }
	        break;
	    }
	}
	
}