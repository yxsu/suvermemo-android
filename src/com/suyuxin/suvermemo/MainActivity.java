package com.suyuxin.suvermemo;

import java.util.List;
import java.util.ListIterator;

import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
						else
						{
							new UpdateNotebookList().execute();
						}
					}
				}
			});
			return convertView;
		}
		
	}
	
	private class UpdateNotebookList extends AsyncTask<Void, Integer, String[]>
	{
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
		protected void onPostExecute(String[] names) {
			// TODO Auto-generated method stub
			super.onPostExecute(names);
			ListView view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
			NotebookListAdapter adapter = new NotebookListAdapter(getBaseContext(),
					R.layout.notebook_row,
					R.id.text_notebook_name,
					names);
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
		protected String[] doInBackground(Void... arg0) {
			// TODO Auto-generated method stub	
			String auth_token = evernote_session.getAuthToken();
			NoteStore.Client client;
			String[] notebook_names = null;
			publishProgress(progress_bar_max_value / 3);
			try {
				client = evernote_session.getClientFactory().createNoteStoreClient().getClient();
				List<Notebook> notebooks = client.listNotebooks(auth_token);
				notebook_names = new String[notebooks.size()];
				//open database
				database.open();
				ListIterator<Notebook> iterator = notebooks.listIterator();
				int index = 0;
				int progress_slice = 2 * progress_bar_max_value / 3 / notebooks.size();
				while(iterator.hasNext())
				{
					Notebook notebook = iterator.next();
					//get names to display
					notebook_names[index] = notebook.getName();
					index++;
					//store into database
					database.inertNotebook(notebook.getName(), notebook.getGuid(), 1);
					//update progress
					publishProgress(progress_bar_max_value / 3 + progress_slice * index);
				}
				database.close();
				publishProgress(progress_bar_max_value);
			} catch (TTransportException e) {
				// TODO Auto-generated catch block
				Toast.makeText(getApplication(), R.string.error_list_notebooks, Toast.LENGTH_LONG).show();
			}finally{
				return notebook_names;
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