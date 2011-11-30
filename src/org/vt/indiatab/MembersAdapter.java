package org.vt.indiatab;

import java.util.HashMap;

import org.vt.indiatab.data.MembersDbAdapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MembersAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	private HashMap<String, ImageView> hash;
	
	public MembersAdapter(Context context, Cursor c) {
		super(context, c, 0);
		inflater = LayoutInflater.from(context);
		hash = new HashMap<String, ImageView>();
	}
	
	private class Holder {
		public ProgressBar progress;
		public ImageView image;
		public TextView name;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag();
		
		// update the hash
		hash.put(holder.image.toString(), holder.image);
		
		// update the progress
		int progC = cursor.getColumnIndex(MembersDbAdapter.COL_LOAN_PROG);
		int durationC = cursor.getColumnIndex(MembersDbAdapter.COL_LOAN_DURATION);
		int duration = cursor.getInt(durationC);
		if (duration != -1) { // they DO have a loan out
			holder.progress.setVisibility(View.VISIBLE);
			holder.progress.setMax(cursor.getInt(durationC));
			holder.progress.setProgress(cursor.getInt(progC));
		}
		else { // they DON'T have a loan out
			holder.progress.setVisibility(View.INVISIBLE);
		}
		
		// update the name
		int nameC = cursor.getColumnIndex(MembersDbAdapter.COL_NAME);
		String name = cursor.getString(nameC);
		holder.name.setText(name);
		
		// update the image
		int imgC = cursor.getColumnIndex(MembersDbAdapter.COL_PIC_PATH);
		String path = cursor.getString(imgC);
		holder.image.setImageResource(R.drawable.missing_photo);
		if (path != null) {
			// Creating the bitmaps should be done in a new thread
			Bundle b = new Bundle();
			b.putString("id", holder.image.toString());
			b.putString("path", path);
			
			new LoadImage().execute(b);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View root = inflater.inflate(R.layout.member_grid_item, parent, false);
		Holder holder = new Holder();
		holder.progress = (ProgressBar) root.findViewById(R.id.member_grid_item_progress);
		holder.image = (ImageView) root.findViewById(R.id.member_grid_item_image);
		holder.name = (TextView) root.findViewById(R.id.member_grid_item_name);
		root.setTag(holder);
		
		return root;
	}
	
	private class LoadImage extends AsyncTask<Bundle, Void, Bundle> {

		@Override
		protected Bundle doInBackground(Bundle... b) {
			String id = b[0].getString("id");
			String path = b[0].getString("path");
			
			BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = 8;
            opts.outHeight = 100;
            opts.outWidth = 100;
            Bitmap bmp = BitmapFactory.decodeFile(path, opts);
			
			Bundle bundle = new Bundle();
			bundle.putString("id", id);
			bundle.putParcelable("bmp", bmp);
			
			return bundle;
		}
		
		@Override
		protected void onPostExecute(Bundle result) {
			String id = result.getString("id");
			Bitmap bmp = result.getParcelable("bmp");
			
			ImageView img = hash.get(id);
			img.setImageBitmap(bmp);
		}
	}
}
