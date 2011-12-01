package org.vt.indiatab;

import org.vt.indiatab.data.MembersDbAdapter;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class LoanActivity extends FragmentActivity {
	
	public static final String MEMBER_ID_EXTRA = "member_id_extra";

	private SeekBar seekAmount, seekDuration;
	private String currencyChar, loanDurationUnit;
	private TextView amountFeed, durationFeed;
	private int[] loanAmounts, loanDurations;
	private long memberId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loan);
		
		// We're canceled unless the user clicks OK
		setResult(RESULT_CANCELED);
		
		// Grab the member's ID and get the name and pic
		String name = "";
		byte[] b = null;
		MembersDbAdapter db = new MembersDbAdapter(this);
		db.open();
		memberId = getIntent().getLongExtra(MEMBER_ID_EXTRA, -1);
		if (memberId != -1) {
			Cursor c = db.fetchMember(memberId);
			int nameC = c.getColumnIndex(MembersDbAdapter.COL_NAME);
			int picC = c.getColumnIndex(MembersDbAdapter.COL_PIC);
			
			name = c.getString(nameC);
			b = c.getBlob(picC);
			
			c.close();
		}
		db.close();
		
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
		
		// Feeds
		amountFeed = (TextView) findViewById(R.id.loan_seek_amount_feed);
		amountFeed.setText(currencyChar + loanAmounts[0]);
		
		durationFeed = (TextView) findViewById(R.id.loan_seek_duration_feed);
		durationFeed.setText(loanDurations[0] + " " + loanDurationUnit);
		
		// The SeekBars
		
		seekAmount = (SeekBar) findViewById(R.id.loan_seek_amount);
		seekAmount.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				amountFeed.setText(currencyChar + loanAmounts[progress]);
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
				durationFeed.setText(loanDurations[progress] + " " + loanDurationUnit);
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
				int amount = loanAmounts[seekAmount.getProgress()];
				int duration = loanDurations[seekDuration.getProgress()];
				
				MembersDbAdapter db = new MembersDbAdapter(LoanActivity.this);
				db.open();
				db.giveLoanToMember(memberId, amount, "reason", duration);
				db.close();
				
				setResult(RESULT_OK);
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
	 * Handle the up structure home button behavior
	 */
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
