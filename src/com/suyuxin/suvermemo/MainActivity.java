package com.suyuxin.suvermemo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;

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
	
	private ListView view_notebook_list;
	private boolean in_sync = false;
	
	private void BeginDownloadSound()
	{
		Iterator<String> iter = getWordsToDownloadSound().iterator();
		int notification_id = 0;
		while(iter.hasNext())
		{
			Intent intent = new Intent(this, ServiceDownloadSound.class);
			intent.putExtra("word", iter.next());
			intent.putExtra("id", notification_id);
			startService(intent);
			notification_id++;
		}
	}
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
					if(in_sync)
					{
						Toast.makeText(getBaseContext(), R.string.text_wait_for_download, Toast.LENGTH_LONG).show();
						return;
					}
					if(getItem(position).equals(getResources().getString(R.string.text_empty_notebook_list)))
					{
						if(!evernote_session.isLoggedIn())
						{
							evernote_session.authenticate(getBaseContext());
						}
						new UpdateNotebookList().execute();
					}
					else if(getItem(position).equals(getResources().getString(R.string.text_sync_sound_file)))
					{//download pronunciation file
						BeginDownloadSound();
					}
					else
					{//normally update content of a notebook
						new UpdateNotebookList().execute();
						new DownloadNotes().execute(list_notebook_guid[position]);
					}
				}
			});
			if(position >= list_notebook_guid.length)
				return convertView;
			
			Button enter = (Button)convertView.findViewById(R.id.button_notebook_enter);
			enter.setVisibility(Button.INVISIBLE);
			enter.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(in_sync)
					{
						Toast.makeText(getBaseContext(), R.string.text_wait_for_download, Toast.LENGTH_LONG).show();
						return;
					}
					Intent intent = new Intent(getContext(), NoteActivity.class);
					intent.putExtra("notebook_guid", list_notebook_guid[position]);
					intent.putExtra("notebook_count", 
						notebook_info.get(list_notebook_guid[position]).note_number);
					startActivity(intent);
				}
			});
			//test whether to show "enter" button
			if(list_notebook_guid != null)
			{
				if(notebooks_having_local_contents.contains(list_notebook_guid[position]))
					enter.setVisibility(Button.VISIBLE);
				else
					enter.setVisibility(Button.INVISIBLE);
			}
			return convertView;
		}
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//store the status
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//
		view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
		NotebookListAdapter adapter = new NotebookListAdapter(this,
				R.layout.notebook_row,
				R.id.text_notebook_name,
				getNotebookNames());
		view_notebook_list.setAdapter(adapter);
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	private class DownloadNotes extends AsyncTask<String, Integer, String>
	{
		ProgressBar progress_bar = null;
		private final int progress_bar_max_value = 1000;
		private static final String LogTag = "DownloadNotes";
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			//store this info of this notebook into shared preferences
			if(progress_bar.getProgress() < progress_bar_max_value)
				Toast.makeText(getBaseContext(), R.string.text_need_sync_again, Toast.LENGTH_LONG).show();
			else
			{
				notebooks_having_local_contents.add(result);
				Toast.makeText(getBaseContext(), R.string.text_download_note_finished, Toast.LENGTH_SHORT).show();
			}
			in_sync = false;
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
			in_sync = true;
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			progress_bar.setProgress(values[0]);
		}

		@SuppressWarnings("finally")
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String auth_token = evernote_session.getAuthToken();
			String notebook_guid = params[0];
			try {
				NoteStore.Client client = evernote_session.getClientFactory().createNoteStoreClient().getClient();
				//set notebook filter
				NoteFilter filter = new NoteFilter();
				filter.setNotebookGuid(notebook_guid);
				//get note list information
				int offset = 0;
				int max_note_number = 20000;
				List<Note> cloud_note_list = new ArrayList<Note>();
				NoteList list_buffer;
				Log.i(LogTag, "Begin to download notebook :" + notebook_guid);
				publishProgress(0);
				do
				{
					list_buffer = client.findNotes(auth_token, filter, offset, max_note_number);
					offset += list_buffer.getNotesSize();
					cloud_note_list.addAll(list_buffer.getNotes());
					//update progress
					publishProgress(offset * progress_bar_max_value / list_buffer.getTotalNotes());
					Log.i(LogTag, "Fetch head date of notes " + offset + " - " + (offset + list_buffer.getNotesSize()));
				}while(list_buffer.getTotalNotes() > cloud_note_list.size());
				//get note content
				database.open();
				//open local note
				Map<String, NoteDbAdapter.NoteInfo> local_notes = database.getNote(notebook_guid);
				for(int index = 0; index < cloud_note_list.size(); index++)
				{
					Note note = cloud_note_list.get(index);
					if(local_notes.containsKey(note.getGuid()))
					{//update content of local note
						if(local_notes.get(note.getGuid()).update_time < note.getUpdated())
						{
							note.setContent(client.getNoteContent(auth_token, note.getGuid()));
							database.updateNoteContent(note);
							//
							publishProgress(index * progress_bar_max_value / cloud_note_list.size());
							Log.i(LogTag, "Update note : " + note.getTitle());
						}
					}
					else
					{//download new content
						note.setContent(client.getNoteContent(auth_token, note.getGuid()));
						//save to database
						database.insertNote(note, note.getUpdated(), 0);
						//
						publishProgress(index * progress_bar_max_value / cloud_note_list.size());
						Log.i(LogTag, "Download note : " + note.getTitle());
					}
				}
				publishProgress(progress_bar_max_value);
					
			}catch(Exception e) {
				Toast.makeText(getApplication(), R.string.error_download_note, Toast.LENGTH_LONG).show();
				
			}finally {
				database.close();
				return notebook_guid;	
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
			notebook_info = database.getNotebookList();
			database.close();
			ListView view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
			NotebookListAdapter adapter = new NotebookListAdapter(getBaseContext(),
					R.layout.notebook_row,
					R.id.text_notebook_name,
					getNotebookNames());
			adapter.notifyDataSetChanged();
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
			}finally{
				return null;
			}
		}
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set adapter of notebook list
		setContentView(R.layout.activity_main);
		view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
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