package org.vt.indiatab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

public class ScrollerFragment extends Fragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.scroller_fragment, container, false);
		
		v.findViewById(R.id.scroller_up).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP));
			}
		});
		
		v.findViewById(R.id.scroller_down).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN));
			}
		});
		
		return v;
	}
}