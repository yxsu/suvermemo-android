package com.suyuxin.suvermemo;

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by suyuxin on 13-5-25.
 */
public class ServiceDownloadNote extends ServiceDownload {

    private static final String LogTag = "Download Notes";

    public ServiceDownloadNote()
    {
        super("Name");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String auth_token = DataActivity.evernote_session.getAuthToken();
        String notebook_guid = intent.getStringExtra("notebook_guid");
        try {
            NoteStore.Client client = DataActivity.evernote_session.getClientFactory().createNoteStoreClient().getClient();
            //set notebook filter
            NoteFilter filter = new NoteFilter();
            filter.setNotebookGuid(notebook_guid);
            //get note list information
            int offset = 0;
            int max_note_number = 20000;
            List<Note> cloud_note_list = new ArrayList<Note>();
            NoteList list_buffer;
            Log.i(LogTag, "Begin to download notebook :" + notebook_guid);
            do
            {
                list_buffer = client.findNotes(auth_token, filter, offset, max_note_number);
                offset += list_buffer.getNotesSize();
                cloud_note_list.addAll(list_buffer.getNotes());
                updateProgress(LogTag, "Fetch head date of notes " + offset + " - " + (offset + list_buffer.getNotesSize()), 0);
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
                        updateProgress(LogTag, "Update note : " + note.getTitle(), 0);
                    }
                }
                else
                {//download new content
                    note.setContent(client.getNoteContent(auth_token, note.getGuid()));
                    //save to database
                    database.insertNote(note, note.getUpdated(), 0);
                    //
                    updateProgress(LogTag, "Download note : " + note.getTitle(), 0);
                }
            }
            updateProgress(LogTag, "finished", 0);

        }catch(Exception e) {
            Toast.makeText(getApplication(), R.string.error_download_note, Toast.LENGTH_LONG).show();

        }finally {
            database.close();
        }
    }

}