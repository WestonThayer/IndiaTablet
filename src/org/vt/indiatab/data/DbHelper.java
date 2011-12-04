package org.vt.indiatab.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

	public static final String DB_NAME = "financialdb";
	public static final int DB_VERSION = 1;
	
	private static final String DB_CREATE_1 = "create table " +
			MembersDbAdapter.TABLE_NAME + " (" +
			MembersDbAdapter.COL_ID + " integer primary key autoincrement, " +
			MembersDbAdapter.COL_NAME + " text not null, " +
			MembersDbAdapter.COL_GROUP + " integer, " +
			MembersDbAdapter.COL_NOTES + " text, " +
			MembersDbAdapter.COL_PIC + " blob, " +
			MembersDbAdapter.COL_LOAN_AMNT + " integer, " +
			MembersDbAdapter.COL_LOAN_REASON + " text, " + //update this later
			MembersDbAdapter.COL_LOAN_PROG + " integer, " +
			MembersDbAdapter.COL_LOAN_DURATION + " integer " +
			MembersDbAdapter.COL_LOAN_AMNT_SIM + " integer " +
			MembersDbAdapter.COL_LOAN_DURATION_SIM + " integer);";
	
	private static final String DB_CREATE_2 = "create table " +
			GroupsDbAdapter.TABLE_NAME + " (" +
			GroupsDbAdapter.COL_ID + " integer primary key autoincrement, " +
			GroupsDbAdapter.COL_NAME + " text not null, " +
			GroupsDbAdapter.COL_DUES + " integer, " +
			GroupsDbAdapter.COL_FEES + " integer, " +
			GroupsDbAdapter.COL_RATE + " integer);";
	
	private static final String DB_CREATE_3 = "create table " +
			MeetingsDbAdapter.TABLE_NAME + " (" +
			MeetingsDbAdapter.COL_ID + " integer primary key autoincrement, " +
			MeetingsDbAdapter.COL_MEETING_NUM + " integer, " +
			MeetingsDbAdapter.COL_GROUP + " integer, " +
			MeetingsDbAdapter.COL_INIT_POT + " integer, " +
			MeetingsDbAdapter.COL_LOANS_IN + " integer, " +
			MeetingsDbAdapter.COL_LOANS_OUT + " integer, " +
			MeetingsDbAdapter.COL_POST_POT + " integer " +
			MeetingsDbAdapter.COL_INIT_POT_SIM + " integer, " +
			MeetingsDbAdapter.COL_LOANS_IN_SIM + " integer, " +
			MeetingsDbAdapter.COL_LOANS_OUT_SIM + " integer, " +
			MeetingsDbAdapter.COL_POST_POT_SIM + " integer);";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_1);
		db.execSQL(DB_CREATE_2);
		db.execSQL(DB_CREATE_3);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + MembersDbAdapter.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + GroupsDbAdapter.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + MeetingsDbAdapter.TABLE_NAME);
		onCreate(db);
	}
}
