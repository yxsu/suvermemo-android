package com.suyuxin.suvermemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class NoteDbAdapter{

	private static final String DB_NAME = "noteDb";
	private static final int DB_VERSION = 1;
	private final Context context;
	private SQLiteDatabase db;
	private NoteDbHelper db_helper;
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
			+ COL_NOTEBOOK_GUID + " TEXT "
			+ COL_TITLE + " TEXT, " 
			+ COL_CONTENT + " TEXT, "
			+ COL_CREATE_TIME + " INTEGER, "
			+ COL_UPDATE_TIME + " INTEGER, "
			+ COL_SHOW_TIME + " INTEGER, "
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
	
	public long inertNotebook(String name, String guid, int note_number)
	{
		ContentValues values = new ContentValues();
		values.put(COL_NOTEBOOK_GUID, guid);
		values.put(COL_NOTEBOOK_NAME, name);
		values.put(COL_NOTE_NUMBER, note_number);
		
		return db.insert(TABLE_NAME_NOTEBOOK, null, values);
	}
	
	public Cursor getNotebookList()
	{
		return db.query(TABLE_NAME_NOTEBOOK, new String[]{COL_NOTEBOOK_GUID, COL_NOTEBOOK_NAME, COL_NOTE_NUMBER},
					null, null, null, null, null);
	}
	
}
