package org.vt.indiatab;

import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;

public class HomeActivity extends FragmentActivity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.homeactivity_title);
    }
}