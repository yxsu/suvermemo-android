package com.suyuxin.suvermemo;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.thrift.transport.TTransportException;

import java.util.Map;

/**
 * Created by suyuxin on 13-5-25.
 */
public class ServiceDownload extends IntentService {

    protected String auth_token;
    protected NoteStore.Client client;
    protected NoteDbAdapter database;
    protected Map<String, NoteDbAdapter.NotebookInfo> notebook_info;// notebook_guid -> NotebookInfo
    public ServiceDownload(String name) {
        super(name);
        //set evernote client
        auth_token = DataActivity.evernote_session.getAuthToken();
        try {
            client = DataActivity.evernote_session.getClientFactory().createNoteStoreClient().getClient();
        } catch (TTransportException e) {
            Log.e("ServiceDownload", e.toString(), e);
        }
        //set database
        database = new NoteDbAdapter(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    protected void updateProgress(String title, String content, int index)
    {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentText(content);
        builder.setContentTitle(title);
        NotificationManager manager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(index, builder.build());
    }
}
