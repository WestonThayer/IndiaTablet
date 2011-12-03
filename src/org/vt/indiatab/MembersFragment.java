package org.vt.indiatab;

import org.vt.indiatab.data.GroupsDbAdapter;
import org.vt.indiatab.data.MeetingsDbAdapter;
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
				// Check to see if anybody is allowed to take out a loan
				// (has the game begun)
				
				MeetingsDbAdapter mdb = new MeetingsDbAdapter(getActivity());
				mdb.open();
				if (!mdb.isFirstMeeting(group)) {
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
						i.putExtra(LoanActivity.GROUP_ID_EXTRA, group);
						startActivityForResult(i, RQ_LOAN);
					}
					else if (progress < duration) {
						Toast.makeText(getActivity(), R.string.one_loan_at_a_time,
								Toast.LENGTH_SHORT).show();
					}
					else {
						showPayLoanDialog(group, id);
					}
				}
				else {
					Toast.makeText(getActivity(), R.string.need_to_start,
							Toast.LENGTH_SHORT).show();
				}
				mdb.close();
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
	
	public void changeAdapterCursor(MembersDbAdapter db) {
		adapter.changeCursor(((TabsActivity) getActivity()).dbAdapter
				.fetchMembers(group));
	}
	
	/*
	 * Options Menu and ActionBar.
	 */
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.add(0, TabsActivity.MENU_ADD, 0, "Add Member")
				.setIcon(R.drawable.add_member_white)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case TabsActivity.MENU_ADD:
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
				//int memberId = data.getIntExtra(LoanActivity.MEMBER_ID_EXTRA, -1);
				int amount = data.getIntExtra(LoanActivity.MEMBER_LOAN_AMOUNT_EXTRA, -1);
				//int duration = data.getIntExtra(LoanActivity.MEMBER_LOAN_DURATION_EXTRA, -1);
				
				MeetingsDbAdapter mdb = new MeetingsDbAdapter(getActivity());
				mdb.open();
				
				mdb.updateLatestMeeting(group, 0, amount);
				((TabsActivity) getActivity()).notifyTabs(mdb, null);
				
				mdb.close();
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
							
							a.dbAdapter.deleteMember(id);
							a.notifyTabs(null, null);
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
	
	private void showPayLoanDialog(long group, long id) {
		PayLoanDialog d = PayLoanDialog.newInstance();
		Bundle args = new Bundle();
		args.putLong("group", group);
		args.putLong("id", id);
		d.setArguments(args);
		
		d.setCancelable(true);
		d.setTargetFragment(MembersFragment.this, 0);
		d.show(getFragmentManager(), "del_member");
	}
	
	public static class PayLoanDialog extends DialogFragment {
		
		static PayLoanDialog newInstance() {
			PayLoanDialog d = new PayLoanDialog();
			return d;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final long group = getArguments().getLong("group");
			final long id = getArguments().getLong("id");
			
			return new AlertDialog.Builder(getActivity())
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle("Pay Debt")
					.setPositiveButton("Pay", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							TabsActivity a = ((TabsActivity) getActivity());
							
							// Grab the amount he took out and for how long
							Cursor c = a.dbAdapter.fetchMember(id);
							int payment = c.getInt(c.getColumnIndex(MembersDbAdapter.COL_LOAN_AMNT));
							int duration = c.getInt(c.getColumnIndex(MembersDbAdapter.COL_LOAN_DURATION));
							c.close();
							
							// Find out how much interest he pays
							// Currently, rate is X per $100 per meeting
							GroupsDbAdapter gdb = new GroupsDbAdapter(getActivity());
							gdb.open();
							c = gdb.fetchGroup(group);
							double rate = (double) c.getInt(c.getColumnIndex(GroupsDbAdapter.COL_RATE));
							c.close();
							gdb.close();
							
							
							// Pay the loan using compound interest:
							// Total = initialAmount * (1 + rate/100)^duration
							MeetingsDbAdapter mdb = new MeetingsDbAdapter(getActivity());
							mdb.open();
							a.dbAdapter.payMemberLoans(id);
							payment = (int) Math.ceil(payment * Math.pow((1 + (rate / 100.0)), duration));
							mdb.updateLatestMeeting(group, payment, 0);
							a.notifyTabs(mdb, null);
							mdb.close();
						}
					})
					.setNegativeButton("Extend", null)
					.create();
		}
	}
}
