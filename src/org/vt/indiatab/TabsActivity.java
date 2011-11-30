package org.vt.indiatab;

import org.vt.indiatab.data.MembersDbAdapter;

import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.MenuItem;
import android.support.v4.view.ViewPager;
import android.support.v4.view.Window;

public class TabsActivity extends FragmentActivity {

	private static final String INDEX = "index";
	
	public MembersDbAdapter dbAdapter;
	
	private ViewPager  mViewPager;
    private TabsAdapter mTabsAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_ITEM_TEXT);
		
		dbAdapter = new MembersDbAdapter(this);
		dbAdapter.open();
		
		ActionBar actionBar = getSupportActionBar();
		
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		Tab tab1 = actionBar.newTab().setText("Members");
		Tab tab2 = actionBar.newTab().setText("Overview");
		Tab tab3 = actionBar.newTab().setText("Simulator");
		
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
	
	@Override
	public void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
	
	/*
	 * Handle the up structure home button behavior
	 */
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
