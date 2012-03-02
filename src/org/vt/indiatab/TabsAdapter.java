package org.vt.indiatab;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.view.ViewPager;

/**
 * A simple ViewPager that I took from a sample application.
 * 
 * @author Weston Thayer
 *
 */
public class TabsAdapter extends FragmentPagerAdapter
		implements ViewPager.OnPageChangeListener, ActionBar.TabListener {

	private final Context ctx;
    private final ActionBar actionBar;
    private final ViewPager mViewPager;
    private final ArrayList<String> tabs = new ArrayList<String>();
    private final long group;

    public TabsAdapter(FragmentActivity activity, ActionBar actionBar,
    		ViewPager pager, long group) {
        super(activity.getSupportFragmentManager());
        ctx = activity;
        this.actionBar = actionBar;
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
        this.group = group;
    }

    public void addTab(ActionBar.Tab tab, Class<?> clss) {
        tabs.add(clss.getName());
        actionBar.addTab(tab.setTabListener(this));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Override
    public Fragment getItem(int position) {
    	// Make sure the group id is passed down
    	Bundle args = new Bundle();
    	args.putLong(MembersFragment.GROUP_EXTRA, group);
    	
        return Fragment.instantiate(ctx, tabs.get(position), args);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
    		int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        actionBar.setSelectedNavigationItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {}
}
