package org.vt.indiatab;

import org.vt.indiatab.data.MeetingsDbAdapter;
import org.vt.indiatab.data.MembersDbAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class LoanActivity extends FragmentActivity {
	
	public static final String MEMBER_ID_EXTRA = "member_id_extra";
	public static final String GROUP_ID_EXTRA = "group_id_extra";
	public static final String MEMBER_LOAN_AMOUNT_EXTRA = "member_loan_amount_extra";
	public static final String MEMBER_LOAN_REASON_EXTRA = "member_loan_reason_extra";
	public static final String MEMBER_LOAN_DURATION_EXTRA = "member_loan_duration_extra";

	private SeekBar seekAmount, seekDuration;
	private String currencyChar, loanDurationUnit;
	private TextView amountFeed, durationFeed, amountAvailable;
	private int[] loanAmounts, loanDurations;
	private long memberId, group;
	private int postPot, finalLoan, finalDuration;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loan);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_ITEM_TEXT);
		
		// We're canceled unless the user clicks OK
		setResult(RESULT_CANCELED);
		
		// Grab the member's ID and get the name and pic
		String name = "";
		byte[] b = null;
		MembersDbAdapter membersDb = new MembersDbAdapter(this);
		membersDb.open();
		memberId = getIntent().getLongExtra(MEMBER_ID_EXTRA, -1);
		if (memberId != -1) {
			Cursor c = membersDb.fetchMember(memberId);
			int nameC = c.getColumnIndex(MembersDbAdapter.COL_NAME);
			int picC = c.getColumnIndex(MembersDbAdapter.COL_PIC);
			
			name = c.getString(nameC);
			b = c.getBlob(picC);
			
			c.close();
		}
		membersDb.close();
		
		// Grab the group ID, the find out how much is in the pot
		group = getIntent().getLongExtra(GROUP_ID_EXTRA, -1);
		MeetingsDbAdapter meetingsDb = new MeetingsDbAdapter(this);
		meetingsDb.open();
		Cursor c = meetingsDb.getLatestMeeting(group);
		postPot = c.getInt(c.getColumnIndex(MeetingsDbAdapter.COL_POST_POT));
		c.close();
		meetingsDb.close();
		
		// Action Bar setup
		ActionBar actionBar = getSupportActionBar();
		
		actionBar.setTitle(name + " > New Loan");
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// Picture of member and name
		if (b != null) {
			Bitmap pic = BitmapFactory.decodeByteArray(b, 0, b.length);
			((ImageView) findViewById(R.id.loan_image)).setImageBitmap(pic);
		}
		
		currencyChar = getResources().getString(R.string.currency_symbol);
		loanDurationUnit = getResources().getString(R.string.loan_duration_unit);
		loanAmounts = getResources().getIntArray(R.array.loan_amounts_custom);
		loanDurations = getResources().getIntArray(R.array.loan_durations);
		
		// Feeds - determine 
		finalLoan = loanAmounts[0];
		finalDuration = loanDurations[0];
		if ((postPot - finalLoan) < 0) {
			// Our initial guess is too high. We need to set a custom amount
			// off the bat
			finalLoan = postPot;
		}
		
		amountFeed = (TextView) findViewById(R.id.loan_seek_amount_feed);
		amountFeed.setText(currencyChar + finalLoan);
		
		durationFeed = (TextView) findViewById(R.id.loan_seek_duration_feed);
		durationFeed.setText(finalDuration + " " + loanDurationUnit);
		
		amountAvailable = (TextView) findViewById(R.id.loan_amount_avilable);
		amountAvailable.setText(currencyChar + (postPot - finalLoan) + " " +
				getResources().getString(R.string.available));
		
		// The SeekBars
		
		seekAmount = (SeekBar) findViewById(R.id.loan_seek_amount);
		seekAmount.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				int loanAmount = loanAmounts[progress];
				
				if ((postPot - loanAmount) < 0) {
					// We can't let them exceed the amount available
					seekBar.setProgress(progress - 1);
				}
				else {
					finalLoan = loanAmounts[progress];
					amountFeed.setText(currencyChar + finalLoan);
					amountAvailable.setText(currencyChar +
							(postPot - finalLoan) + " " +
							getResources().getString(R.string.available));
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		seekDuration = (SeekBar) findViewById(R.id.loan_seek_duration);
		seekDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				finalDuration = loanDurations[progress];
				durationFeed.setText(finalDuration + " " + loanDurationUnit);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		
		// OK & Cancel buttons
		
		Button ok = (Button) findViewById(R.id.loan_ok);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				MembersDbAdapter membersDb = new MembersDbAdapter(LoanActivity.this);
				membersDb.open();
				membersDb.giveLoanToMember(memberId, finalLoan, "reason", finalDuration);
				membersDb.close();
				
				Intent i = new Intent();
				i.putExtra(MEMBER_ID_EXTRA, memberId);
				i.putExtra(MEMBER_LOAN_AMOUNT_EXTRA, finalLoan);
				i.putExtra(MEMBER_LOAN_REASON_EXTRA, "reason");
				i.putExtra(MEMBER_LOAN_DURATION_EXTRA, finalDuration);
				setResult(RESULT_OK, i);
				finish();
			}
		});
		
		Button cancel = (Button) findViewById(R.id.loan_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/*
	 * Currently, it forgets if you've entered a custom value. Annoying to fix
	 * because the SeekBars auto update on a rotate and override whatever value
	 * you recover. Deal with it later.
	 * TODO: Deal with it.
	 * 
	private static final String FINAL_LOAN = "final_loan";
	private static final String FINAL_DURATION = "final_duration";
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(FINAL_LOAN, finalLoan);
		outState.putInt(FINAL_DURATION, finalDuration);
	}*/
	
	public void setCustomLoan(int loanAmount, int duration) {		
		if ((postPot - loanAmount) < 0) {
			// We can't let them exceed the amount available
			Toast.makeText(this, R.string.out_of_bounds, Toast.LENGTH_SHORT).show();
		}
		else {
			finalLoan = loanAmount;
			amountFeed.setText(currencyChar + finalLoan);
			amountAvailable.setText(currencyChar +
					(postPot - finalLoan) + " " +
					getResources().getString(R.string.available));
		}
		
		finalDuration = duration;
		durationFeed.setText(finalDuration + " " + loanDurationUnit);
	}
	
	/*
	 * Handle the up structure home button behavior and Action Bar buttons
	 */
	
	private static final int MENU_CUSTOM = 0;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_CUSTOM, 0, "Custom")
				.setIcon(R.drawable.edit_white)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
						MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		
		super.onCreateOptionsMenu(menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        case MENU_CUSTOM:
	        	showCustomDialog();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	/*
	 * Show the Dialogs that allow for custom loan amounts and durations.
	 */
	
	private void showCustomDialog() {
		CustomDialog d = CustomDialog.newInstance();
		d.show(getSupportFragmentManager(), "custom_dialog");
	}
	
	public static class CustomDialog extends DialogFragment {
		
		private static final String AMOUNT = "amount";
		private static final String DURATION = "duration";
		
		private EditText amount, duration;
		
		static CustomDialog newInstance() {
			CustomDialog d = new CustomDialog();
			return d;
		}
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
			.setTitle(R.string.loan_custom_title)
			.setNegativeButton("Cancel", null);
			
			LayoutInflater inflater = getActivity().getLayoutInflater();
			View v = inflater.inflate(R.layout.loan_custom_dialog, null, false);
			
			amount = (EditText) v.findViewById(R.id.loan_custom_dialog_amount);
			duration = (EditText) v.findViewById(R.id.loan_custom_dialog_duration);
			
			// Restore values if we've rotated the screen
			if (savedInstanceState != null) {
				String savedAmount = savedInstanceState.getString(AMOUNT);
				if (savedAmount != null) {
					amount.setText(savedAmount);
				}
				String savedDuration = savedInstanceState.getString(DURATION);
				if (savedDuration != null) {
					duration.setText(savedDuration);
				}
			}
			
			builder.setView(v);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					LoanActivity a = (LoanActivity) getActivity();
					
					try {
						a.setCustomLoan(Integer.parseInt(amount.getText().toString()),
								Integer.parseInt(duration.getText().toString()));
					}
					catch (NumberFormatException e) {
						Toast.makeText(a, R.string.number_format_exception,
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			return builder.create();
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putString(AMOUNT, amount.getText().toString());
			outState.putString(DURATION, duration.getText().toString());
		}
	}
}
