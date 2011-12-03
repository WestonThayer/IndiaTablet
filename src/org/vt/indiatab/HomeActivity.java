package org.vt.indiatab;

import org.vt.indiatab.data.GroupsDbAdapter;

import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;

public class HomeActivity extends FragmentActivity {

	public GroupsDbAdapter dbAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.homeactivity_title);
        
        dbAdapter = new GroupsDbAdapter(this);
		dbAdapter.open();
    }
    
    @Override
	public void onDestroy() {
		/*
		 * There are some issues here with stuff staying open during rotations.
		 * We should look for a way to preserve the db connection. For right
		 * now, I'll be happy as long as I can switch activities.
		 */
		dbAdapter.close();
		super.onDestroy();
	}
}