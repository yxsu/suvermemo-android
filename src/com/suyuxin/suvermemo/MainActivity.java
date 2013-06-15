package com.suyuxin.suvermemo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.view.*;
import android.widget.*;
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

public class MainActivity extends DataActivity{

    private List<String> notebook_list_in_view;
    private NotebookListAdapter adapter;
    private boolean in_sync = false;

    private void BeginDownloadNote(String notebook_guid)
    {
        Intent intent = new Intent(this, ServiceDownloadNote.class);
        intent.putExtra("notebook_guid", notebook_guid);
        startService(intent);
    }


    private void UpdateNotebookList()
    {
        Intent intent = new Intent(this, ServiceUpdateNotebookList.class);
        startService(intent);
    }

	private class NotebookListAdapter extends ArrayAdapter<String> {

        private NotebookListAdapter(Context context, int resource, int textViewResourceId, List<String> objects) {
            super(context, resource, textViewResourceId, objects);
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
            if(list_notebook_guid == null)
                return convertView;
			//set status of notebook
            ImageView iv_notebook_status = (ImageView)convertView.findViewById(R.id.imageView_notebook_status);
            if(notebooks_having_local_contents.contains(list_notebook_guid.get(position)))
                iv_notebook_status.setVisibility(ImageView.VISIBLE);
            else
                iv_notebook_status.setVisibility(ImageView.INVISIBLE);
            //set OnClickListener
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    if(notebooks_having_local_contents.contains(list_notebook_guid.get(position)))
                    {//enter into notebook
                        Intent intent = new Intent(getContext(), NoteActivity.class);
                        intent.putExtra("notebook_guid", list_notebook_guid.get(position));
                        intent.putExtra("notebook_count",
                                notebook_info.get(list_notebook_guid.get(position)).note_number);
                        startActivity(intent);
                    }
                    else
                    {//first sync or login
                        if(getItem(position).equals(getResources().getString(R.string.text_empty_notebook_list)))
                        {
                            if(!evernote_session.isLoggedIn())
                            {
                                evernote_session.authenticate(getBaseContext());
                            }
                            UpdateNotebookList();
                        }
                        else
                        {//normally update content of a notebook
                            UpdateNotebookList();
                            BeginDownloadNote(list_notebook_guid.get(position));
                        }
                    }

                }
            });
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
	}

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_task:
                //open task panel
                if(task_notebook_guid != null)
                {
                    Intent intent = new Intent(this, TaskPanel.class);
                    intent.putExtra("notebook_guid", task_notebook_guid);
                    startActivity(intent);
                }else{
                    Toast.makeText(this, R.string.info_need_create_task_notebook, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_download_sound:
                //download sound
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
                break;
            case R.id.action_update_task:
                //update content of task
                Intent intent = new Intent(this, ServiceDownloadTask.class);
                intent.putExtra("notebook_guid", task_notebook_guid);
                startService(intent);
                break;
            case R.id.action_update_local_notebook:
                //update local notebook
                UpdateNotebookList();
                Iterator<String> iter_guid = notebooks_having_local_contents.iterator();
                while(iter_guid.hasNext())
                {
                    BeginDownloadNote(iter_guid.next());
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//set adapter of notebook list
		setContentView(R.layout.activity_main);
        notebook_list_in_view = getNotebookNames();
		ListView view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
		adapter = new NotebookListAdapter(this,
				R.layout.notebook_row,
                R.id.text_notebook_name,
				getNotebookNames());
		view_notebook_list.setAdapter(adapter);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
	      case EvernoteSession.REQUEST_CODE_OAUTH:
	    	// update notebook list when oauth activity returns result
	        if (resultCode == Activity.RESULT_OK) {
                UpdateNotebookList();
	        }
	        break;
	    }
	}
	
}