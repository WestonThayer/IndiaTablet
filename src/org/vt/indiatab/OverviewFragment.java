package org.vt.indiatab;

import org.vt.indiatab.data.GroupsDbAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OverviewFragment extends Fragment {

	private GroupsDbAdapter dbAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		dbAdapter = new GroupsDbAdapter(getActivity());
		dbAdapter.open();
	}
	
	@Override
	public void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.overview_fragment, container, false);
		
		return root;
	}
}
