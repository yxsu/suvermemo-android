package com.suyuxin.suvermemo;

import java.util.List;

import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends DataActivity{
	
	NotebookListAdapter adapter_notebooks = null;
	
	
	class NotebookListAdapter extends ArrayAdapter<String> {

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
							updateNotebookList();
						}
					}
				}
			});
			return convertView;
		}
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set adapter of notebook list
		setContentView(R.layout.activity_main);
		ListView view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
		adapter_notebooks = new NotebookListAdapter(this,
				R.layout.notebook_row,
				R.id.text_notebook_name,
				new String[]{getResources().getString(R.string.text_empty_notebook_list)});
		view_notebook_list.setAdapter(adapter_notebooks);
		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
	      case EvernoteSession.REQUEST_CODE_OAUTH:
	    	// update notebook list when oauth activity returns result
	        if (resultCode == Activity.RESULT_OK) {
	        	updateNotebookList();
	        }
	        break;
	    }
	}
	
	protected void updateNotebookList()
	{
		//showDialog(DIALOG_PROGRESS);
		try {
			evernote_session.getClientFactory().createNoteStoreClient()
				.listNotebooks(new OnClientCallback<List<Notebook>>()
						{

							@Override
							public void onSuccess(List<Notebook> notebooks) {
								// TODO Auto-generated method stub
								if(notebook_names == null)
								{
									notebook_names = new String[notebooks.size()];
									for(int i = 0; i < notebooks.size(); i++)
									{
										notebook_names[i] = notebooks.get(i).getName();
									}
								}
							}

							@Override
							public void onException(Exception exception) {
								// TODO Auto-generated method stub
								Toast.makeText(getApplication(), R.string.error_list_notebooks, Toast.LENGTH_LONG).show();
			//					removeDialog(DIALOG_PROGRESS);
							}
					
						});
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplication(), R.string.error_create_notestore, Toast.LENGTH_LONG).show();
		//	removeDialog(DIALOG_PROGRESS);
		}
		
	}
	
}