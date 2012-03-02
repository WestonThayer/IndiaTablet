package org.vt.indiatab.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * A class to aid access to the "groups" table.
 * 
 * @author Weston Thayer
 *
 */
public class GroupsDbAdapter {
	
	public static final String TABLE_NAME = "groups";
	public static final String COL_ID = "_id";
	public static final String COL_NAME = "name";
	// TODO: what happens if a member can't bring the money and has to skip?
	public static final String COL_DUES = "dues";
	public static final String COL_FEES = "fees";
	// TODO: the idea is that you fill in the X here, but there's probably a
	// better way of setting the interest rate UI-wise
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
	
	/**
	 * Add a row for a new group.
	 * 
	 * @param name		The name of the group
	 * @param dues		The amount each member MUST bring to each meeting
	 * @param fees		The amount collected by the accountant each meeting
	 * @param rate		The compound interest rate ($X / $100 / Meeting)
	 * @return			Returns the unique row ID of the new group
	 */
	public long createGroup(String name, int dues, int fees, int rate) {
		// TODO: there's no restrictions on length for any field. It would be a
		// good idea to check your inputs to the table.
		
		ContentValues values = new ContentValues();
		values.put(COL_NAME, name);
		values.put(COL_DUES, dues);
		values.put(COL_FEES, fees);
		values.put(COL_RATE, rate);
		
		return db.insert(TABLE_NAME, null, values);
	}
	
	/**
	 * Update an existing group row.
	 * 
	 * @param id		The unique row ID
	 * @param name
	 * @param dues
	 * @param fees
	 * @param rate
	 * @return			Returns true if successful, false if else
	 */
	public boolean updateGroup(long id, String name, int dues, int fees,
			int rate) {
		ContentValues values = new ContentValues();
		values.put(COL_NAME, name);
		values.put(COL_DUES, dues);
		values.put(COL_FEES, fees);
		values.put(COL_RATE, rate);
		
		return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
	}
	
	/**
	 * Remove a group row.
	 * 
	 * @param id	The unique row ID
	 * @return		True if it was deleted
	 */
	public boolean deleteGroup(long id) {
		// Delete the group's respective members
		MembersDbAdapter mDb = new MembersDbAdapter(ctx);
		mDb.open();
		mDb.deleteGroupMembers(id);
		mDb.close();
		
		// Delete the group's meetings
		MeetingsDbAdapter mdb = new MeetingsDbAdapter(ctx);
		mdb.open();
		mdb.deleteMeetings(id);
		mdb.close();
		
		return db.delete(TABLE_NAME, COL_ID + "=" + id, null) > 0;
	}
	
	/*
	 * Grab members as a Cursor
	 */
	
	/**
	 * Find a group row and return a Cursor to it.
	 * 
	 * @param id
	 * @return		Returns a Cursor that points to the row and every column
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
	
	/**
	 * Get all of the rows.
	 * 
	 * @return
	 */
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
