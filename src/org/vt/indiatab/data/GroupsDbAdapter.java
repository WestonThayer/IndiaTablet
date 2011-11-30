package org.vt.indiatab.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class GroupsDbAdapter {
	
	public static final String TABLE_NAME = "groups";
	public static final String COL_ID = "_id";
	public static final String COL_NAME = "name";
	public static final String COL_DUES = "dues";
	public static final String COL_FEES = "fees";
	public static final String COL_RATE = "rate";
	
	private Context ctx;
	private SQLiteDatabase db;
	private DbHelper helper;
	
	public GroupsDbAdapter(Context context) {
		ctx = context;
	}
	
	public void open() throws SQLException {
		helper = new DbHelper(ctx);
		db = helper.getWritableDatabase();
	}
	
	public void close() {
		helper.close();
	}
	
	/*
	 * Create, update, and delete member rows.
	 */
	
	public long createGroup(String name, int dues, int fees, int rate) {
		ContentValues values = new ContentValues();
		values.put(COL_NAME, name);
		values.put(COL_DUES, dues);
		values.put(COL_FEES, fees);
		values.put(COL_RATE, rate);
		
		return db.insert(TABLE_NAME, null, values);
	}
	
	public boolean updateGroup(long id, String name, int dues, int fees,
			int rate) {
		ContentValues values = new ContentValues();
		values.put(COL_NAME, name);
		values.put(COL_DUES, dues);
		values.put(COL_FEES, fees);
		values.put(COL_RATE, rate);
		
		return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
	}
	
	public boolean deleteGroup(long id) {
		// Delete the group's respective members
		MembersDbAdapter mDb = new MembersDbAdapter(ctx);
		mDb.open();
		mDb.deleteGroupMembers(id);
		mDb.close();
		
		return db.delete(TABLE_NAME, COL_ID + "=" + id, null) > 0;
	}
	
	/*
	 * Grab members as a Cursor
	 */
	
	public Cursor fetchGroup(long id) {
		Cursor c = db.query(true, TABLE_NAME, new String[] {COL_ID, COL_NAME,
				COL_DUES, COL_FEES, COL_RATE},
				COL_ID + "=" + id, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public Cursor fetchGroups() {
		Cursor c = db.query(TABLE_NAME, new String[] {COL_ID, COL_NAME,
				COL_DUES, COL_FEES, COL_RATE},
				null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
}
