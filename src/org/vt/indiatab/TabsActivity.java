package org.vt.indiatab;

import org.vt.indiatab.data.GroupsDbAdapter;
import org.vt.indiatab.data.MeetingsDbAdapter;
import org.vt.indiatab.data.MembersDbAdapter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.support.v4.view.Window;
import android.widget.Toast;

public class TabsActivity extends FragmentActivity {

	private static final String INDEX = "index";
	
	private ViewPager  mViewPager;
    private TabsAdapter mTabsAdapter;
    private ActionBar actionBar;
    private Tab tab1, tab2, tab3;
    private long group;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_ITEM_TEXT);
		
		group = getIntent().getLongExtra(MembersFragment.GROUP_EXTRA, -1);
		
		actionBar = getSupportActionBar();
		
		String groupName = getIntent().getStringExtra(GroupFragment.GROUP_NAME_EXTRA);
		actionBar.setTitle(groupName);
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		tab1 = actionBar.newTab().setText("Members");
		tab2 = actionBar.newTab().setText("Overview");
		tab3 = actionBar.newTab().setText("Simulator");
		
		mViewPager = (ViewPager)findViewById(R.id.pager);
		
		mTabsAdapter = new TabsAdapter(this, actionBar, mViewPager,
				getIntent().getLongExtra(MembersFragment.GROUP_EXTRA, -1));
		
		mTabsAdapter.addTab(tab1, MembersFragment.class);
		mTabsAdapter.addTab(tab2, OverviewFragment.class);
		mTabsAdapter.addTab(tab3, SimulatorFragment.class);

        if (savedInstanceState != null) {
        	actionBar.setSelectedNavigationItem(savedInstanceState.getInt(INDEX));
        }
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(INDEX, getSupportActionBar().getSelectedNavigationIndex());
    }
	
	/*
	 * Handle the up structure home button behavior and add the common actions
	 */
	
	public static final int MENU_ADVANCE = 0;
	public static final int MENU_ADD = 1;
	public static final int MENU_DELETE_MEETINGS = 2;
	public static final int MENU_DELETE_SIMULATIONS = 3;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ADVANCE, Menu.FIRST, "Advance")
				.setIcon(R.drawable.next_white)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		super.onCreateOptionsMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        case MENU_ADVANCE:
	        	MembersDbAdapter membersDb = new MembersDbAdapter(this);
	        	membersDb.open();
	        	MeetingsDbAdapter meetingsDb = new MeetingsDbAdapter(this);
	        	meetingsDb.open();
	        	GroupsDbAdapter groupsDb = new GroupsDbAdapter(this);
	        	groupsDb.open();
	        	Cursor c;
	        	
	        	// Check if this is even allowed
	        	if (membersDb.isMemberLoanDue(group)) {
	        		membersDb.close();
	        		
	        		Toast.makeText(this, R.string.outstanding_loans,
	        				Toast.LENGTH_SHORT).show();
	        		actionBar.selectTab(tab1);
	        	}
	        	else {
		        	// (Last meetings POST_POT) + (#Members * Dues) - Fees
		        	
		        	c = groupsDb.fetchGroup(group);
		        	int dues = c.getInt(c.getColumnIndex(GroupsDbAdapter.COL_DUES));
		        	int fees = c.getInt(c.getColumnIndex(GroupsDbAdapter.COL_FEES));
		        	c.close();
		        	
		        	int numMembers = membersDb.getMemberCount(group);
		        	int initPot = (numMembers * dues) - fees;
		        	
		        	if (numMembers != 0) {		        		
		        		// Create the next meeting
			        	if (meetingsDb.isFirstMeeting(group)) {
			        		meetingsDb.createMeeting(1, group, initPot, initPot);
			        	}
			        	else {
			        		c = meetingsDb.getLatestMeeting(group);
			        		int postPot = c.getInt(c.getColumnIndex(
			        				MeetingsDbAdapter.COL_POST_POT));
			        		int postPotSim = c.getInt(c.getColumnIndex(
			        				MeetingsDbAdapter.COL_POST_POT_SIM));
			        		int nextMeeting = meetingsDb.getNextMeetingNumber(c);
			        		c.close();
			        		
			        		// Give loans after the first meeting
			        		membersDb.giveSimLoanToFreeMember(group, postPotSim);
			        		meetingsDb.updateLatestSimMeeting(group, 0, postPotSim);
			        		
			        		meetingsDb.createMeeting(nextMeeting, group,
			        				postPot + initPot, initPot);
			        		
			        		// Pay back the loan
			        		int[] ret = membersDb.paySimMemberLoan(group);
			        		if (ret != null) {
				        		int simAmnt = ret[0];
				        		int simDur = ret[1];
				        		c = groupsDb.fetchGroup(group);
								double rate = (double) c.getInt(c.getColumnIndex(GroupsDbAdapter.COL_RATE));
								c.close();
								simAmnt = (int) Math.ceil(simAmnt * Math.pow((1 + (rate / 100.0)), simDur));
								meetingsDb.updateLatestSimMeeting(group, simAmnt, 0);
			        		}
			        	}
			        	
			        	// Move all members with loans out forward
			        	membersDb.advanceMemberLoans(group);
			        	
			        	notifyTabs();
			        	
			        	// Inform the user that we've moved on
			        	String currency = getResources().getString(R.string.currency_symbol);
			        	String intro = getResources().getString(R.string.meeting_advanced);
			        	
			        	Toast.makeText(this, intro + " " + currency +
			        			(numMembers * dues), Toast.LENGTH_SHORT).show();
		        	}
		        	else {
		        		Toast.makeText(this, R.string.no_members_yet,
		        				Toast.LENGTH_SHORT).show();
		        	}
	        	}
	        	
	        	membersDb.close();
	        	meetingsDb.close();
	        	groupsDb.close();
	        	
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	public void notifyTabs() {
		/*
    	 * This was no fun at all. The Fragment's tag is auto generated
    	 * by ActionBarSherlock's FragmentPagerAdapter using the
    	 * actual ViewPager's id and the tab's position. Terrible.
    	 */
		FragmentManager fm = getSupportFragmentManager();
		
		// Members Tab
		String tag = FragmentPagerAdapter.makeFragmentName(mViewPager.getId(), 0);
		MembersFragment mf = (MembersFragment) fm.findFragmentByTag(tag);
    	if (mf != null) {
    		mf.changeAdapterCursor();
    	}
		
    	// Overview Tab
    	tag = FragmentPagerAdapter.makeFragmentName(mViewPager.getId(), 1);
    	OverviewFragment of = (OverviewFragment) fm.findFragmentByTag(tag);
    	if (of != null) {
    		of.changeAdapterCursor();
    	}
    	
    	// Simulator Tab
    	tag = FragmentPagerAdapter.makeFragmentName(mViewPager.getId(), 2);
    	SimulatorFragment sf = (SimulatorFragment) fm.findFragmentByTag(tag);
    	if (sf != null) {
    		sf.changeAdapterCursor();
    	}
	}
}
