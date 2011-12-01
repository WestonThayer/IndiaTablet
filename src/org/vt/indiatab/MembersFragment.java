package org.vt.indiatab;

import org.vt.indiatab.data.MembersDbAdapter;

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
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
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
		
		// Clicking allows for taking out loan, or repaying loan
		grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// Decide if they can take out a loan or if a loan is due
				
				MembersDbAdapter db = new MembersDbAdapter(getActivity());
				db.open();
				Cursor c = db.fetchMember(id);
				
				int durationC = c.getColumnIndex(MembersDbAdapter.COL_LOAN_DURATION);
				int duration = c.getInt(durationC);
				int progressC = c.getColumnIndex(MembersDbAdapter.COL_LOAN_PROG);
				int progress = c.getInt(progressC);
				
				c.close();
				db.close();
				
				if (duration == -1) {
					Intent i = new Intent(getActivity(), LoanActivity.class);
					i.putExtra(LoanActivity.MEMBER_ID_EXTRA, id);
					startActivityForResult(i, RQ_LOAN);
				}
				else if (progress < duration) {
					Toast.makeText(getActivity(), R.string.one_loan_at_a_time,
							Toast.LENGTH_SHORT).show();
				}
				else {
					
				}
			}
		});
		
		// Long press allows for delete
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
	public static final int RQ_LOAN = 2;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case RQ_ADD_MEMBER:
			if (resultCode == Activity.RESULT_OK) {
				adapter.changeCursor(((TabsActivity) getActivity())
						.dbAdapter.fetchMembers(group));
			}
			return;
		case RQ_LOAN:
			if (resultCode == Activity.RESULT_OK) {
				adapter.changeCursor(((TabsActivity) getActivity())
						.dbAdapter.fetchMembers(group));
			}
			return;
		default:
			return;
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
