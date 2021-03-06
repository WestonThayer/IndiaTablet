package org.vt.indiatab;

import java.io.ByteArrayOutputStream;

import org.vt.indiatab.data.MembersDbAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

/**
 * A view that allows for the creation of a new member. The user can add notes,
 * a name, and a picture chosen from the Android Gallery.
 * 
 * @author Weston Thayer
 *
 */
public class AddMemberActivity extends FragmentActivity {
	
	private ImageButton image;
	private EditText name, notes;
	private byte[] pic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_ACTION_BAR_ITEM_TEXT);
		setContentView(R.layout.add_member);
		
		// We're canceled unless the user clicks OK
		setResult(RESULT_CANCELED);
		
		ActionBar actionBar = getSupportActionBar();
		
		// TODO: string.xml again
		actionBar.setTitle("Add a Member");
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		image = (ImageButton) findViewById(R.id.add_member_image);
		name = (EditText) findViewById(R.id.add_member_name);
		notes = (EditText) findViewById(R.id.add_member_notes);
		
		// Restore the image if possible
		if (savedInstanceState != null) {
			pic = savedInstanceState.getByteArray("pic");
			if (pic != null) {
				image.setImageBitmap(BitmapFactory.decodeByteArray(pic, 0, pic.length));
			}
		}
		
		// The image launches the gallery, and gets the results back
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(Intent.createChooser(intent, "Select Picture"), PIC_REQUEST);
			}
		});
		
		// OK & Cancel buttons
		
		Button ok = (Button) findViewById(R.id.add_member_ok);
		ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String n = name.getText().toString();
				long group = getIntent().getLongExtra(MembersFragment.GROUP_EXTRA, -1);
				
				if (group != -1) {
					MembersDbAdapter membersDb = new MembersDbAdapter(AddMemberActivity.this);
					membersDb.open();
					membersDb.createMember(n, group, notes.getText().toString(), pic);
					membersDb.close();
					
					setResult(RESULT_OK);
				}
				finish();
			}
		});
		
		Button cancel = (Button) findViewById(R.id.add_member_cancel);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putByteArray("pic", pic);
		super.onSaveInstanceState(outState);
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
	
	/*
	 * Activity result code (for camera)
	 */
	
	public static final int PIC_REQUEST = 9;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == PIC_REQUEST && resultCode == RESULT_OK) {
			Uri uri = data.getData();
			
			Cursor cursor = getContentResolver().query(uri,
					new String[] {
						android.provider.MediaStore.Images.ImageColumns.DATA
					}, null, null, null);
            cursor.moveToFirst();
            
            //Link to the image
            String imageFilePath = cursor.getString(0);
            cursor.close();
            
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 4;
            opts.outHeight = 100;
            opts.outWidth = 100;
            Bitmap bmp = BitmapFactory.decodeFile(imageFilePath, opts);
            image.setImageBitmap(bmp);
            
            // Ready it for storage
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bmp.compress(CompressFormat.JPEG, 90, bos); 
            pic = bos.toByteArray();
		}
	}
}
