package com.suyuxin.suvermemo;

import android.content.Intent;
import android.widget.Toast;
import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import com.evernote.thrift.TException;

import java.util.*;

/**
 * Created by suyuxin on 13-5-28.
 */
public class ServiceDownloadTask extends ServiceDownload {

    private static final String LogTag = "Update Task";
    private static final String TAG_UNFINISHED = "未完";
    private static final String TAG_IMPORTANT = "重要";
    private static final String TAG_UNIMPORTANT = "不重要";
    private static final String TAG_URGENT = "紧急";
    private static final String TAG_NOT_URGENT = "不紧急";
    private static final String TAG_DAILY = "长期";
    private Map<String, String> tag_names;//tag guid -> tag name
    private String notebook_guid;
    private ArrayList<NoteDbAdapter.TaskInfo> cloud_task;
    public ServiceDownloadTask() {
        super("name");
    }

    @Override
    protected void onHandleIntent(Intent intent){
        super.onHandleIntent(intent);
        try
        {
            notebook_guid = intent.getStringExtra("notebook_guid");
            updateProgress(LogTag, "Download Cloud Task...", 0);
            downloadCloudTaskInfo();
            updateTask();
            updateProgress(LogTag, "finished", 0);
            //update title and content of task
            Intent next_intent = new Intent(this, ServiceDownloadNote.class);
            next_intent.putExtra("notebook_guid", notebook_guid);
            startService(next_intent);
        }catch (Exception e)
        {
            Toast.makeText(getApplication(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void downloadCloudTaskInfo() throws EDAMUserException, EDAMSystemException, TException, EDAMNotFoundException {
        //get tag information
        List<Tag> list_tag = client.listTagsByNotebook(auth_token, notebook_guid);
        tag_names = new HashMap<String, String>();
        Iterator<Tag> tag_iter = list_tag.iterator();
        while(tag_iter.hasNext())
        {
            Tag tag = tag_iter.next();
            tag_names.put(tag.getGuid(), tag.getName());
        }
        //get note information
        NoteFilter filter = new NoteFilter();
        filter.setTagGuidsIsSet(true);
        filter.setNotebookGuid(notebook_guid);
        int offset = 0;
        int max_note_number = 20000;
        Set<Note> cloud = new HashSet<Note>();
        NoteList list_buffer;
        do
        {
            list_buffer = client.findNotes(auth_token, filter, offset, max_note_number);
            offset += list_buffer.getNotesSize();
            cloud.addAll(list_buffer.getNotes());
            updateProgress(LogTag, "Fetch head date of notes " + offset + " - " + (offset + list_buffer.getNotesSize()), 0);
        }while(list_buffer.getTotalNotes() > cloud.size());
        //add task into cloud_task
        cloud_task = new ArrayList<NoteDbAdapter.TaskInfo>();
        Iterator<Note> iter = cloud.iterator();
        while(iter.hasNext())
        {
            Note note = iter.next();
            Iterator<String> iter_tag_guid = note.getTagGuidsIterator();
            boolean is_finished = true;
            NoteDbAdapter.TaskInfo task = new NoteDbAdapter.TaskInfo();
            task.guid = note.getGuid();
            task.update_time = note.getUpdated();
            while(iter_tag_guid.hasNext())
            {
                String name = tag_names.get(iter_tag_guid.next());
                if(name.equals(TAG_UNFINISHED))
                    is_finished = false;
                else if(name.equals(TAG_IMPORTANT))
                    task.is_important = true;
                else if(name.equals(TAG_UNIMPORTANT))
                    task.is_important = false;
                else if(name.equals(TAG_URGENT))
                    task.is_urgent = true;
                else if(name.equals(TAG_NOT_URGENT))
                    task.is_urgent = false;
                else if(name.equals(TAG_DAILY))
                    task.is_daily = true;
            }
            if(is_finished == false)
                cloud_task.add(task);
        }
    }
    private void updateTask()
    {
        database.open();
        Map<String, NoteDbAdapter.TaskInfo> local_task = database.getLocalTasks(notebook_guid);
        Iterator<NoteDbAdapter.TaskInfo> iter = cloud_task.iterator();
        while(iter.hasNext())
        {
            NoteDbAdapter.TaskInfo task = iter.next();
            if(local_task.containsKey(task.guid))
            {//update
                if(task.update_time > local_task.get(task.guid).update_time)
                    database.updateTask(task.guid, task.is_important, task.is_urgent, task.is_daily);
                //remove record
                local_task.remove(task.guid);
            }
            else
            {//add new
                database.insertTask(task.guid, task.is_important, task.is_urgent, task.is_daily);
            }
        }
        //remove all finished tasks
        Iterator<String> iter_local_guid = local_task.keySet().iterator();
        while(iter_local_guid.hasNext())
        {
            database.deleteTask(iter_local_guid.next());
        }
        database.close();
    }

    private void uploadTask()
    {

    }
}
