package org.vt.indiatab.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class MeetingsDbAdapter {

	public static final String TABLE_NAME = "meetings";
	public static final String COL_ID = "_id";
	public static final String COL_MEETING_NUM = "meeting_num";
	public static final String COL_GROUP = "group_id";
	
	// Real columns
	public static final String COL_INIT_POT = "init_pot";
	public static final String COL_LOANS_IN = "loans_in";
	public static final String COL_LOANS_OUT = "loans_out";
	public static final String COL_POST_POT = "post_pot";
	
	// Simulation columns
	public static final String COL_INIT_POT_SIM = "init_pot_sim";
	public static final String COL_LOANS_IN_SIM = "loans_in_sim";
	public static final String COL_LOANS_OUT_SIM = "loans_out_sim";
	public static final String COL_POST_POT_SIM = "post_pot_sim";
	
	private Context ctx;
	private SQLiteDatabase db;
	private DbHelper helper;
	
	public MeetingsDbAdapter(Context context) {
		ctx = context;
	}
	
	public void open() throws SQLException {
		helper = new DbHelper(ctx);
		db = helper.getWritableDatabase();
	}
	
	public void close() {
		helper.close();
	}
	
	public long createMeeting(int meetingNum, long group, int initPot,
			int initPotSim) {
		ContentValues values = new ContentValues();
		values.put(COL_MEETING_NUM, meetingNum);
		values.put(COL_GROUP, group);
		values.put(COL_INIT_POT, initPot);
		values.put(COL_LOANS_IN, 0);
		values.put(COL_LOANS_OUT, 0);
		values.put(COL_POST_POT, initPot);
		
		// Simulator too
		values.put(COL_INIT_POT_SIM, initPotSim);
		values.put(COL_LOANS_IN_SIM, 0);
		values.put(COL_LOANS_OUT_SIM, 0);
		values.put(COL_POST_POT_SIM, initPotSim);
		
		return db.insert(TABLE_NAME, null, values);
	}
	
	public boolean updateLatestMeeting(long group, int loansIn, int loansOut) {
		Cursor c = getLatestMeeting(group);
		long id = c.getLong(c.getColumnIndex(COL_ID));
		int cInitPot = c.getInt(c.getColumnIndex(COL_INIT_POT));
		int cLoansIn = c.getInt(c.getColumnIndex(COL_LOANS_IN));
		int cLoansOut = c.getInt(c.getColumnIndex(COL_LOANS_OUT));
		int cInitPotSim = c.getInt(c.getColumnIndex(COL_INIT_POT_SIM));
		int cLoansInSim = c.getInt(c.getColumnIndex(COL_LOANS_IN_SIM));
		int cLoansOutSim = c.getInt(c.getColumnIndex(COL_LOANS_OUT_SIM));
		c.close();
		
		int tLoansIn = cLoansIn + loansIn;
		int tLoansOut = cLoansOut + loansOut;
		int tLoansInSim = cLoansInSim + loansIn;
		int tLoansOutSim = cLoansOutSim + loansOut;
		
		ContentValues values = new ContentValues();
		values.put(COL_LOANS_IN, tLoansIn);
		values.put(COL_LOANS_OUT, tLoansOut);
		values.put(COL_POST_POT, cInitPot + tLoansIn - tLoansOut);
		
		// Simulator too
		values.put(COL_LOANS_IN_SIM, tLoansInSim);
		values.put(COL_LOANS_OUT_SIM, tLoansOutSim);
		values.put(COL_POST_POT_SIM, cInitPotSim + tLoansInSim - tLoansOutSim);
		
		return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
	}
	
	public boolean updateLatestSimMeeting(long group, int loansIn, int loansOut) {
		Cursor c = getLatestMeeting(group);
		long id = c.getLong(c.getColumnIndex(COL_ID));
		int cInitPotSim = c.getInt(c.getColumnIndex(COL_INIT_POT_SIM));
		int cLoansInSim = c.getInt(c.getColumnIndex(COL_LOANS_IN_SIM));
		int cLoansOutSim = c.getInt(c.getColumnIndex(COL_LOANS_OUT_SIM));
		c.close();
		
		int tLoansIn = cLoansInSim + loansIn;
		int tLoansOut = cLoansOutSim + loansOut;
		
		ContentValues values = new ContentValues();
		values.put(COL_LOANS_IN_SIM, tLoansIn);
		values.put(COL_LOANS_OUT_SIM, tLoansOut);
		values.put(COL_POST_POT_SIM, cInitPotSim + tLoansIn - tLoansOut);
		
		return db.update(TABLE_NAME, values, COL_ID + "=" + id, null) > 0;
	}
	
	public boolean isFirstMeeting(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID
				},
				COL_GROUP + "=" + group, null, null, null, null, null);
		boolean first = (c.getCount() == 0);
		c.close();
		
		return first;
	}
	
	public Cursor getLatestMeeting(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID,
				COL_MEETING_NUM,
				COL_INIT_POT,
				COL_LOANS_IN,
				COL_LOANS_OUT,
				COL_POST_POT,
				COL_INIT_POT_SIM,
				COL_LOANS_IN_SIM,
				COL_LOANS_OUT_SIM,
				COL_POST_POT_SIM
				},
				COL_GROUP + "=" + group,
				null, null, null,
				COL_MEETING_NUM + " DESC",
				"1");
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public int getNextMeetingNumber(long group) {
		Cursor c = getLatestMeeting(group);
		
		if (c.getCount() == 0) {
			c.close();
			return 1;
		}
		else {
			int meetingNumC = c.getColumnIndex(COL_MEETING_NUM);
			int meetingNum = c.getInt(meetingNumC);
			c.close();
			
			return ++meetingNum;
		}
	}
	
	public int getNextMeetingNumber(Cursor c) {		
		if (c.getCount() == 0) {
			return 1;
		}
		else {
			int meetingNumC = c.getColumnIndex(COL_MEETING_NUM);
			int meetingNum = c.getInt(meetingNumC);
			
			return ++meetingNum;
		}
	}
	
	public Cursor fetchMeetings(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID,
				COL_MEETING_NUM,
				COL_INIT_POT,
				COL_LOANS_IN,
				COL_LOANS_OUT,
				COL_POST_POT
				},
				COL_GROUP + "=" + group, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public Cursor fetchSimMeetings(long group) {
		Cursor c = db.query(TABLE_NAME, new String[] {
				COL_ID,
				COL_MEETING_NUM,
				COL_INIT_POT_SIM,
				COL_LOANS_IN_SIM,
				COL_LOANS_OUT_SIM,
				COL_POST_POT_SIM
				},
				COL_GROUP + "=" + group, null, null, null, null, null);
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	}
	
	public boolean deleteMeetings(long group) {
		return db.delete(TABLE_NAME, COL_GROUP + "=" + group, null) > 0;
	}
}
