package com.suyuxin.suvermemo;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;

import java.util.ArrayList;
import java.util.Iterator;
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

        String notebook_guid = intent.getStringExtra("notebook_guid");
        try {
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
                updateProgress(LogTag, "Fetch head date of notes " + offset + " - " + (offset + list_buffer.getNotesSize()), 0);
                offset += list_buffer.getNotesSize();
                cloud_note_list.addAll(list_buffer.getNotes());
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
                    NoteDbAdapter.NoteInfo local_note = local_notes.get(note.getGuid());
                    if(local_note.update_time < note.getUpdated())
                    {
                        updateProgress(LogTag, "Update note : " + note.getTitle(), 0);
                        note.setContent(client.getNoteContent(auth_token, note.getGuid()));
                        database.updateNoteContent(note);
                    }
                    else if(local_note.update_time > note.getUpdated())
                    {//need to update cloud note
                        updateProgress(LogTag, "Upload note : " + note.getTitle(), 1);
                        note.setUpdated(local_note.update_time);
                        note.setContent(local_note.content);
                        note.setUpdateSequenceNum(note.getUpdateSequenceNum() - 1);
                        client.updateNote(auth_token, note);
                    }
                    //remove record
                    local_notes.remove(note.getGuid());
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
            //remove all deleted notes
            Iterator<String> iter_guid = local_notes.keySet().iterator();
            while(iter_guid.hasNext())
            {
                database.deleteNote(iter_guid.next(), notebook_guid);
            }
            updateProgress(LogTag, "finished", 0);

        }catch(Exception e) {
            Toast.makeText(getApplication(), R.string.error_download_note, Toast.LENGTH_LONG).show();

        }finally {
            database.close();
        }
    }

}