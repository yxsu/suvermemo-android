package com.suyuxin.suvermemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class NoteDbHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "noteDb";
	private static final int DB_VERSION = 1;
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
			+ COL_NOTEBOOK_GUID + " REFERENCES " + TABLE_NAME_NOTEBOOK + "(" + COL_NOTEBOOK_GUID + ") "
			+ COL_TITLE + " TEXT, " 
			+ COL_CONTENT + " TEXT, "
			+ COL_CREATE_TIME + " INTEGER, "
			+ COL_UPDATE_TIME + " INTEGER, "
			+ COL_SHOW_TIME + " INTEGER, "
			+ COL_FAMILIAR_INDEX + " INTEGER);";
	
	public NoteDbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
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
