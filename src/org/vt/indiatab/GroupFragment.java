package org.vt.indiatab;

import org.vt.indiatab.data.GroupsDbAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class GroupFragment extends Fragment {
	
	public static final String GROUP_NAME_EXTRA = "group_name_extra";
	
	private ListView lv;
	private SimpleCursorAdapter adapter;
	private Cursor c;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Declare that this Fragment can add to the ActionBar and Menu
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.group_fragment, container, false);
		lv = (ListView) root.findViewById(R.id.group_listview);
		
		// Click to look at group
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				//start the new activity
				Intent i = new Intent(getActivity(), TabsActivity.class);
				i.putExtra(MembersFragment.GROUP_EXTRA, id);
				
				TextView name = (TextView) v.findViewById(R.id.group_row_name);
				i.putExtra(GROUP_NAME_EXTRA, name.getText());
				
				getActivity().startActivity(i);
			}
		});
		
		// Long press to delete group
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				showDeleteGroupDialog(id);
				
				return true;
			}
		});
		
		return root;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// We have to set the adapter here because the activity is now
		// managing the database
		String[] from = new String[] {
				GroupsDbAdapter.COL_NAME,
				GroupsDbAdapter.COL_DUES,
				GroupsDbAdapter.COL_FEES,
				GroupsDbAdapter.COL_RATE
				};
		int[] to = new int[] {
				R.id.group_row_name,
				R.id.group_row_dues,
				R.id.group_row_fees,
				R.id.group_row_rate
				};
		
		c = ((HomeActivity) getActivity()).dbAdapter.fetchGroups();
		getActivity().startManagingCursor(c);
		adapter = new SimpleCursorAdapter(getActivity(), R.layout.group_row,
				c, from, to, 0);
		
		lv.setAdapter(adapter);
	}
	
	/*
	 * Options Menu and ActionBar.
	 */
	
	private static final int MENU_ADD = 0;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, MENU_ADD, 0, R.string.groupfragment_add).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS |
				MenuItem.SHOW_AS_ACTION_WITH_TEXT);;
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			showAddGroupDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*
	 * Show the Dialogs that allow for the addition and deletion of groups.
	 */
	
	private void showAddGroupDialog() {
		GroupDialog d = GroupDialog.newInstance();
		d.setTargetFragment(GroupFragment.this, 0);
		d.show(getFragmentManager(), "group_dialog");
	}
	
	public static class GroupDialog extends DialogFragment {
		
		private static final String NAME = "name";
		private static final String DUES = "dues";
		private static final String FEES = "fees";
		private static final String RATE = "rate";
		
		private EditText name, dues, fees, rate;
		
		static GroupDialog newInstance() {
			GroupDialog d = new GroupDialog();
			return d;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setTitle("Create Group")
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dismiss();
				}
			});
			
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.group_add, null, false);
			
			name = (EditText) v.findViewById(R.id.group_add_name);
			dues = (EditText) v.findViewById(R.id.group_add_dues);
			fees = (EditText) v.findViewById(R.id.group_add_fees);
			rate = (EditText) v.findViewById(R.id.group_add_rate);
			
			// Restore values if we've rotated the screen
			if (savedInstanceState != null) {
				String savedName = savedInstanceState.getString(NAME);
				if (savedName != null) {
					name.setText(savedName);
				}
				String savedDues = savedInstanceState.getString(DUES);
				if (savedDues != null) {
					dues.setText(savedDues);
				}
				String savedFees = savedInstanceState.getString(FEES);
				if (savedFees != null) {
					name.setText(savedFees);
				}
				String savedRate = savedInstanceState.getString(RATE);
				if (savedRate != null) {
					rate.setText(savedRate);
				}
			}
			
			builder.setView(v);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					HomeActivity a = ((HomeActivity) getActivity());
					GroupFragment f = (GroupFragment) getTargetFragment();
					
					a.dbAdapter.createGroup(
							name.getText().toString(),
							Integer.parseInt(dues.getText().toString()),
							Integer.parseInt(fees.getText().toString()),
							Integer.parseInt(rate.getText().toString())
							);
					f.adapter.changeCursor(a.dbAdapter.fetchGroups());
					
					dismiss();
				}
			});
			
			return builder.create();
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putString(NAME, name.getText().toString());
			outState.putString(DUES, dues.getText().toString());
			outState.putString(FEES, fees.getText().toString());
			outState.putString(RATE, rate.getText().toString());
		}
	}
	
	private void showDeleteGroupDialog(long id) {
		DeleteGroupDialog d = DeleteGroupDialog.newInstance();
		Bundle args = new Bundle();
		args.putLong("id", id);
		d.setArguments(args);
		
		d.setCancelable(true);
		d.setTargetFragment(GroupFragment.this, 0);
		d.show(getFragmentManager(), "del_group");
	}
	
	public static class DeleteGroupDialog extends DialogFragment {
		
		static DeleteGroupDialog newInstance() {
			DeleteGroupDialog d = new DeleteGroupDialog();
			return d;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final long id = getArguments().getLong("id");
			
			return new AlertDialog.Builder(getActivity())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Delete Group")
					.setMessage("Delete this group and all its members?")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							HomeActivity a = ((HomeActivity) getActivity());
							GroupFragment f = (GroupFragment) getTargetFragment();
							
							a.dbAdapter.deleteGroup(id);
							f.adapter.changeCursor(a.dbAdapter.fetchGroups());
						}
					})
					.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dismiss();
						}
					})
					.create();
		}
	}
}