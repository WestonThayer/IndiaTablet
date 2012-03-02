package org.vt.indiatab.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * A class to help with access to the "members" table.
 * 
 * @author Weston Thayer
 *
 */
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
	
	// Simulation columns
	public static final String COL_LOAN_AMNT_SIM = "loan_amount_sim";
	public static final String COL_LOAN_DURATION_SIM = "loan_duration_sim";
	
	public static final int NO_LOAN = 0;
	
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
		values.put(COL_LOAN_AMNT, 0);
		values.put(COL_LOAN_REASON, "reason");
		values.put(COL_LOAN_PROG, 0);
		values.put(COL_LOAN_DURATION, NO_LOAN);
		
		// Not even a fake one
		values.put(COL_LOAN_AMNT_SIM, 0);
		values.put(COL_LOAN_DURATION_SIM, NO_LOAN);
		
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
	
	public boolean advanceMemberLoans(long group) {
		Cursor c = fetchOutstandingMembers(group);
		if (c.getCount() > 0) {
			do {
				long id = c.getLong(c.getColumnIndex(COL_ID));
				int cProg = c.getInt(c.getColumnIndex(COL_LOAN_PROG));
				
				ContentValues values = new ContentValues();
				values.put(COL_LOAN_PROG, cProg + 1);
				
				if (!(db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0)) {
					return false;
				}
			}
			while (c.moveToNext());
			
			c.close();
			return true;
		}
		
		c.close();
		return false;
	}
	
	public boolean absolveMemberLoans(long group) {
		ContentValues values = new ContentValues();
		values.put(COL_LOAN_AMNT, 0);
		values.put(COL_LOAN_REASON, "reason");
		values.put(COL_LOAN_PROG, 0);
		values.put(COL_LOAN_DURATION, NO_LOAN);
		
		return db.update(TABLE_NAME, values, COL_GROUP + "=" + group, null) > 0;
	}
	
	public boolean payMemberLoans(long id) {
		ContentValues values = new ContentValues();
		values.put(COL_LOAN_AMNT, 0);
		values.put(COL_LOAN_REASON, "reason");
		values.put(COL_LOAN_PROG, 0);
		values.put(COL_LOAN_DURATION, NO_LOAN);
		
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
				COL_GROUP + "=" + group,
				null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public Cursor fetchOutstandingMembers(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID,
				COL_NAME,
				COL_LOAN_AMNT,
				COL_LOAN_REASON,
				COL_LOAN_PROG,
				COL_LOAN_DURATION,
				COL_PIC
				},
				COL_GROUP + "=" + group +
				" AND " + COL_LOAN_DURATION + "<>" + NO_LOAN,
				null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public int getMemberCount(long group) {
		Cursor c = fetchMembers(group);
		int count = c.getCount();
		c.close();
		
		return count;
	}
	
	public boolean isMemberLoanDue(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_LOAN_PROG,
				COL_LOAN_DURATION
				},
				COL_GROUP + "=" + group +
				" AND " + COL_LOAN_PROG + "=" + COL_LOAN_DURATION +
				" AND " + COL_LOAN_DURATION + "<>" + NO_LOAN,
				null, null, null, null, null);
		boolean due = (c.getCount() > 0);
		c.close();
		
		return due;
	}
	
	/*
	 * Simulation specifics
	 */
	
	/**
	 * Request a member who has no loans out.
	 * 
	 * @param group		The group
	 * @return			Returns 1 or no members
	 */
	public Cursor fetchFreeMember(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID,
				COL_LOAN_AMNT_SIM,
				COL_LOAN_DURATION_SIM
		},
		COL_GROUP + "=" + group +
		" AND " + COL_LOAN_DURATION + "=" + NO_LOAN,
		null, null, null, null,
		"1");
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	/**
	 * Attempt to find a member with no loans out and give them some amount.
	 * 
	 * @param group		The group
	 * @param amount	The amount to give them
	 * @return			True if the loan was given, false if else
	 */
	public boolean giveSimLoanToFreeMember(long group, int amount) {
		Cursor c = fetchFreeMember(group);
		if (c.getCount() == 0) {
			c.close();
			return false;
		}
		long id = c.getLong(c.getColumnIndex(COL_ID));
		c.close();
		
		ContentValues values = new ContentValues();
		values.put(COL_LOAN_AMNT_SIM, amount);
		values.put(COL_LOAN_DURATION_SIM, 1);
		
		db.update(TABLE_NAME, values, COL_ID + "=" + id, null);
		
		return true;
	}
	
	/**
	 * Grab the member who has the previous meeting's simulated loan.
	 * 
	 * @param group		The group
	 * @return			One member if there are any
	 */
	public Cursor fetchSimOutstandingMember(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID,
				COL_LOAN_AMNT_SIM,
				COL_LOAN_DURATION_SIM
		},
		COL_GROUP + "=" + group +
		" AND " + COL_LOAN_DURATION_SIM + "<>" + NO_LOAN,
		null, null, null, null,
		"1");
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	/**
	 * Attempts to pay a simulated loan back into the pot.
	 * 
	 * @param group		The group
	 * @return			Returns and array with the amount first, duration
	 * 					second, or null if there isn't a member to pay
	 */
	public int[] paySimMemberLoan(long group) {
		Cursor c = fetchSimOutstandingMember(group);
		if (c.getCount() == 0) {
			c.close();
			return null;
		}
		long id = c.getLong(c.getColumnIndex(COL_ID));
		int amount = c.getInt(c.getColumnIndex(COL_LOAN_AMNT_SIM));
		int duration = c.getInt(c.getColumnIndex(COL_LOAN_DURATION_SIM));
		c.close();
		
		ContentValues values = new ContentValues();
		values.put(COL_LOAN_AMNT_SIM, 0);
		values.put(COL_LOAN_DURATION_SIM, NO_LOAN);
		
		db.update(TABLE_NAME, values, COL_ID + "=" + id, null);
		
		return new int[] {amount, duration};
	}
}
