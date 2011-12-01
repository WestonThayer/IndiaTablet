package org.vt.indiatab.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MembersDbAdapter {
	
	public static final String TABLE_NAME = "members";
	public static final String COL_ID = "_id";
	public static final String COL_NAME = "name";
	public static final String COL_GROUP = "group_id";
	public static final String COL_NOTES = "notes";
	public static final String COL_PIC = "pic";
	public static final String COL_LOAN_AMNT = "loan_amount";
	public static final String COL_LOAN_REASON = "loan_reason";
	public static final String COL_LOAN_PROG = "loan_progress";
	public static final String COL_LOAN_DURATION = "loan_duration";
	
	private Context ctx;
	private SQLiteDatabase db;
	private DbHelper helper;
	
	public MembersDbAdapter(Context context) {
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
	
	public long createMember(String name, long group, String notes, byte[] pic) {
		ContentValues values = new ContentValues();
		values.put(COL_NAME, name);
		values.put(COL_GROUP, group);
		values.put(COL_NOTES, notes);
		values.put(COL_PIC, pic);
		
		// Initially, nobody has a loan out
		values.put(COL_LOAN_DURATION, -1);
		
		return db.insert(TABLE_NAME, null, values);
	}
	
	public boolean updateMember(long id, String name, long group, String notes) {
		ContentValues values = new ContentValues();
		values.put(COL_NAME, name);
		values.put(COL_GROUP, group);
		values.put(COL_NOTES, notes);
		
		return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
	}
	
	/**
	 * Update a member's row with a loan.
	 * 
	 * @param id		Row ID of the member
	 * @param amount	Loan amount in some whole unit (dollars, rupees)
	 * @param reason	Small description of why
	 * @param duration	How many meetings they hold the loan for
	 * @return			True if successful
	 */
	public boolean giveLoanToMember(long id, int amount, String reason,
			int duration) {
		ContentValues values = new ContentValues();
		values.put(COL_LOAN_AMNT, amount);
		values.put(COL_LOAN_REASON, reason);
		values.put(COL_LOAN_PROG, 0);
		values.put(COL_LOAN_DURATION, duration);
		
		return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
	}
	
	/**
	 * Notify a member that they're closer to having a due loan.
	 * 
	 * @param id		The Row ID
	 * @param progress	How far they are into their loan
	 * @return			True if successful
	 */
	public boolean updateMemberLoan(long id, int progress) {
		ContentValues values = new ContentValues();
		values.put(COL_LOAN_PROG, progress);
		
		return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
	}
	
	public boolean deleteMember(long id) {
		return db.delete(TABLE_NAME, COL_ID + "=" + id, null) > 0;
	}
	
	public boolean deleteGroupMembers(long group) {
		return db.delete(TABLE_NAME, COL_GROUP + "=" + group, null) > 0;
	}
	
	/*
	 * Grab members as a Cursor
	 */
	
	public Cursor fetchMember(long id) {
		Cursor c = db.query(true, TABLE_NAME, new String[] {
				COL_ID,
				COL_NAME,
				COL_GROUP,
				COL_NOTES,
				COL_PIC,
				COL_LOAN_AMNT,
				COL_LOAN_PROG,
				COL_LOAN_DURATION,
				COL_LOAN_REASON
				},
				COL_ID + "=" + id, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public Cursor fetchMembers(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID,
				COL_NAME,
				COL_LOAN_PROG,
				COL_LOAN_DURATION,
				COL_PIC
				},
				COL_GROUP + "=" + group, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
}
