package com.suyuxin.suvermemo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.evernote.edam.type.Note;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class NoteDbAdapter{

	private static final String DB_NAME = "noteDb";
	private static final int DB_VERSION = 1;
	private final Context context;
	private SQLiteDatabase db;
	private NoteDbHelper db_helper;
	//custom data structure
	public class NotebookInfo
	{
		String name;
		int note_number;
		
		public NotebookInfo(String name, int note_number)
		{
			this.name = name;
			this.note_number = note_number;
		}
	}
	
	public class NoteInfo
	{
		String title;
		String content;
		long update_time;
		long show_time;
		int familiar_index;
		
		public NoteInfo(String title, String content, long update_time,
				long show_time, int familiar_index)
		{
			this.title = title;
			this.content = content;
			this.update_time = update_time;
			this.show_time = show_time;
			this.familiar_index = familiar_index;
		}
	}
	//information about columns of database
	public static final String TABLE_NAME_NOTE = "note";
	public static final String TABLE_NAME_NOTEBOOK = "notebook";
	public static final String COL_NOTE_NUMBER = "note_number";
	public static final String COL_NOTEBOOK_NAME = "notebook_name";
	public static final String COL_NOTE_GUID = "note_guid";
	public static final String COL_NOTEBOOK_GUID = "notebook_guid";
	public static final String COL_TITLE = "title";
	public static final String COL_CONTENT = "content";
	public static final String COL_CREATE_TIME = "create_time";
	public static final String COL_UPDATE_TIME = "update_time";
	public static final String COL_SHOW_TIME = "show_time";
	public static final String COL_FAMILIAR_INDEX = "familiar_index";
	//SQL statements
	private static final String STRING_CREATE_NOTEBOOK = "CREATE TABLE " 
			+ TABLE_NAME_NOTEBOOK + " ( " + COL_NOTEBOOK_GUID + " TEXT PRIMARY KEY, "
			+ COL_NOTEBOOK_NAME + " TEXT, "
			+ COL_NOTE_NUMBER + " INTEGER);";
	private static final String STRING_CREATE_NOTE = "CREATE TABLE " + TABLE_NAME_NOTE 
			+ " ( " + COL_NOTE_GUID + " TEXT PRIMARY KEY, " 
			+ COL_NOTEBOOK_GUID + " TEXT, "
			+ COL_TITLE + " TEXT, " 
			+ COL_CONTENT + " TEXT, "
			+ COL_CREATE_TIME + " DECIMAL(14,0), "
			+ COL_UPDATE_TIME + " DECIMAL(14,0), "
			+ COL_SHOW_TIME + " DECIMAL(14,0), "
			+ COL_FAMILIAR_INDEX + " INTEGER);";
	
	public NoteDbAdapter(Context ctx)
	{
		context = ctx;
		db_helper = new NoteDbHelper(context);
	}
	
	private static class NoteDbHelper extends SQLiteOpenHelper
	{
		public NoteDbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(STRING_CREATE_NOTEBOOK);
			db.execSQL(STRING_CREATE_NOTE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
			

		}

	}
	
	//open the database
	public NoteDbAdapter open() throws SQLException
	{
		db = db_helper.getWritableDatabase();
		return this;
	}
	
	public void close()
	{
		db_helper.close();
	}
	
	public long insertNotebook(String name, String guid, int note_number)
	{
		ContentValues values = new ContentValues();
		values.put(COL_NOTEBOOK_GUID, guid);
		values.put(COL_NOTEBOOK_NAME, name);
		values.put(COL_NOTE_NUMBER, note_number);
		
		return db.insert(TABLE_NAME_NOTEBOOK, null, values);
	}
	
	public boolean updateNotebook(String name, String guid, int note_number)
	{
		ContentValues values = new ContentValues();
		values.put(COL_NOTEBOOK_NAME, name);
		values.put(COL_NOTE_NUMBER, note_number);
		return db.update(TABLE_NAME_NOTEBOOK, values, COL_NOTEBOOK_GUID + "='" + guid+"'", null) > 0;
	}
	
	public Map<String, NotebookInfo> getNotebookList()
	{
		Cursor cursor = db.query(TABLE_NAME_NOTEBOOK, new String[]{COL_NOTEBOOK_GUID, COL_NOTEBOOK_NAME, COL_NOTE_NUMBER},
				null, null, null, null, null);
		Map<String, NotebookInfo> map = new HashMap<String, NotebookInfo>();
		
		if(cursor.moveToFirst())
		{
			do
			{
				map.put(cursor.getString(0), new NotebookInfo(cursor.getString(1), cursor.getInt(2)));
			}while(cursor.moveToNext());
		}
		return map;
	}
	
	public long insertNote(Note note, long show_time, int familiar_index)
	{
		ContentValues values = new ContentValues();
		values.put(COL_NOTE_GUID, note.getGuid());
		values.put(COL_NOTEBOOK_GUID, note.getNotebookGuid());
		values.put(COL_TITLE, note.getTitle());
		values.put(COL_CONTENT, note.getContent());
		values.put(COL_CREATE_TIME, note.getCreated());
		values.put(COL_UPDATE_TIME, note.getUpdated());
		values.put(COL_SHOW_TIME, show_time);
		values.put(COL_FAMILIAR_INDEX, familiar_index);
		return db.insert(TABLE_NAME_NOTE, null, values);
	}
	
	public boolean updateNoteContent(Note note)
	{
		ContentValues values = new ContentValues();
		values.put(COL_TITLE, note.getTitle());
		values.put(COL_CONTENT, note.getContent());
		values.put(COL_UPDATE_TIME, note.getUpdated());
		return db.update(TABLE_NAME_NOTE, values, COL_NOTE_GUID + "='" + note.getGuid() +"'", null) > 0;
	}
	
	public boolean updateNoteShowTime(String note_guid, long show_time, int familiar_index)
	{
		ContentValues values = new ContentValues();
		values.put(COL_SHOW_TIME, show_time);
		values.put(COL_FAMILIAR_INDEX, familiar_index);
		return db.update(TABLE_NAME_NOTE, values, COL_NOTE_GUID + " = " + note_guid, null) > 0;
	}
	
	public Map<String, NoteInfo> getNote(String notebook_guid)
	{
		Cursor cursor = db.query(TABLE_NAME_NOTE, 
				new String[]{COL_NOTE_GUID, COL_TITLE, COL_CONTENT, COL_UPDATE_TIME, COL_SHOW_TIME, COL_FAMILIAR_INDEX},
				COL_NOTEBOOK_GUID + "='" + notebook_guid + "'", null, null, null, null);
		//copy to hash map
		Map<String, NoteInfo> map = new HashMap<String, NoteInfo>();//note_guid -> NoteInfo
		if(cursor.moveToFirst())
		{
			do
			{
				map.put(cursor.getString(0), new NoteInfo(cursor.getString(1), cursor.getString(2),
						cursor.getLong(3), cursor.getLong(4), cursor.getInt(5)));
			}while(cursor.moveToNext());
		}
		return map;
	}
	
	public Map<String, NoteInfo> getOutOfDateNote(String notebook_guid)
	{
		//get the time of right now
		long now = Calendar.getInstance().getTimeInMillis();
		//query
		Cursor cursor = db.query(TABLE_NAME_NOTE, 
				new String[]{COL_NOTE_GUID, COL_TITLE, COL_CONTENT, COL_UPDATE_TIME, COL_SHOW_TIME, COL_FAMILIAR_INDEX},
				COL_NOTEBOOK_GUID + " ='" + notebook_guid + "' and " + COL_SHOW_TIME + " < " + now,
				null, null, null, null);
		//copy to hash map
		Map<String, NoteInfo> map = new HashMap<String, NoteInfo>();//note_guid -> NoteInfo
		if(cursor.moveToFirst())
		{
			do
			{
				map.put(cursor.getString(0), new NoteInfo(cursor.getString(1), cursor.getString(2),
						cursor.getLong(3), cursor.getLong(4), cursor.getInt(5)));
			}while(cursor.moveToNext());
		}
		return map;
	}
	
}
