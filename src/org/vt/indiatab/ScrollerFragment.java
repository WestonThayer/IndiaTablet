package org.vt.indiatab;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

/**
 * ScrollerFragment was designed to make this application easier to use on
 * the Aakash tablet, which uses a resistive touch screen. Resistive screens
 * (as opposed to the capacitive screens found on most Android devices) rely on
 * pressure to figure out where touch as occurred. As a result, scrolling
 * through a list by dragging a finger becomes more difficult. Buttons are
 * usually easier to press.
 * 
 * This solution kind of sucks. It just sends key clicks for the up and down
 * arrow keys when an on screen button is pressed. This Fragment was giving me
 * issues, so it is currently not used.
 * 
 * @author Weston Thayer
 *
 */
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