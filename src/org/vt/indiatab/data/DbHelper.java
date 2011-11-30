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
			MembersDbAdapter.COL_PIC_PATH + " text, " +
			MembersDbAdapter.COL_LOAN_AMNT + " integer, " +
			MembersDbAdapter.COL_LOAN_REASON + " text, " + //update this later
			MembersDbAdapter.COL_LOAN_PROG + " integer, " +
			MembersDbAdapter.COL_LOAN_DURATION + " integer);";
	
	private static final String DB_CREATE_2 = "create table " +
			GroupsDbAdapter.TABLE_NAME + " (" +
			GroupsDbAdapter.COL_ID + " integer primary key autoincrement, " +
			GroupsDbAdapter.COL_NAME + " text not null, " +
			GroupsDbAdapter.COL_DUES + " integer, " +
			GroupsDbAdapter.COL_FEES + " integer, " +
			GroupsDbAdapter.COL_RATE + " integer);";

	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_1);
		db.execSQL(DB_CREATE_2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + MembersDbAdapter.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + GroupsDbAdapter.TABLE_NAME);
		onCreate(db);
	}
}
