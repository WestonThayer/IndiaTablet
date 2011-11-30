package org.vt.indiatab;

import android.app.Activity;
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
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemLongClickListener;

public class MembersFragment extends Fragment {
	
	public static final String GROUP_EXTRA = "group_extra";
	
	private Cursor c;
	private MembersAdapter adapter;
	private long group;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		group = getArguments().getLong(GROUP_EXTRA);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.members_fragment, container, false);
		
		GridView grid = (GridView) root.findViewById(R.id.members_gridview);
		
		c = ((TabsActivity) getActivity()).dbAdapter.fetchMembers(group);
		getActivity().startManagingCursor(c);
		adapter = new MembersAdapter(getActivity(), c);
		
		grid.setAdapter(adapter);
		
		grid.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					int position, long id) {
				showDeleteMemberDialog(id);
				
				return true;
			}
		});
		
		return root;
	}
	
	/*
	 * Options Menu and ActionBar.
	 */
	
	private static final int MENU_ADD = 0;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, MENU_ADD, 0, "Add Member")
				.setIcon(android.R.drawable.ic_menu_add)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			Intent i = new Intent(getActivity(), AddMemberActivity.class);
			i.putExtra(GROUP_EXTRA, group);
			startActivityForResult(i, RQ_ADD_MEMBER);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*
	 * What to do when another Activity returns
	 */
	
	public static final int RQ_ADD_MEMBER = 1;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// Did they add a member? Refresh if so.
		if (requestCode == RQ_ADD_MEMBER && resultCode == Activity.RESULT_OK) {
			adapter.changeCursor(((TabsActivity) getActivity()).dbAdapter.fetchMembers(group));
		}
	}
	
	/*
	 * Dialogs
	 */
	
	private void showDeleteMemberDialog(long id) {
		DeleteMemberDialog d = DeleteMemberDialog.newInstance();
		Bundle args = new Bundle();
		args.putLong("id", id);
		d.setArguments(args);
		
		d.setCancelable(true);
		d.setTargetFragment(MembersFragment.this, 0);
		d.show(getFragmentManager(), "del_member");
	}
	
	public static class DeleteMemberDialog extends DialogFragment {
		
		static DeleteMemberDialog newInstance() {
			DeleteMemberDialog d = new DeleteMemberDialog();
			return d;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final long id = getArguments().getLong("id");
			
			return new AlertDialog.Builder(getActivity())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Delete Member")
					.setMessage("Delete this member?")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							TabsActivity a = ((TabsActivity) getActivity());
							MembersFragment f = (MembersFragment) getTargetFragment();
							
							a.dbAdapter.deleteMember(id);
							f.adapter.changeCursor(a.dbAdapter.fetchMembers(f.group));
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
