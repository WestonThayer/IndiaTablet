package org.vt.indiatab;

import org.vt.indiatab.data.MembersDbAdapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MembersAdapter extends CursorAdapter {

	private LayoutInflater inflater;
	
	public MembersAdapter(Context context, Cursor c) {
		super(context, c, 0);
		inflater = LayoutInflater.from(context);
	}
	
	private class Holder {
		public ProgressBar progress;
		public ImageView image;
		public TextView name;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Holder holder = (Holder) view.getTag();
		
		// update the progress
		int prog = cursor.getInt(cursor.getColumnIndex(MembersDbAdapter.COL_LOAN_PROG));
		int duration = cursor.getInt(cursor.getColumnIndex(MembersDbAdapter.COL_LOAN_DURATION));
		if (duration != MembersDbAdapter.NO_LOAN) {
			holder.progress.setVisibility(View.VISIBLE);
			holder.progress.setMax(duration);
			holder.progress.setProgress(prog);
			
			if (prog == duration) {
				view.setBackgroundResource(R.color.loan_due);
			}
			else {
				view.setBackgroundColor(0);
			}
		}
		else { // they DON'T have a loan out
			holder.progress.setVisibility(View.INVISIBLE);
			view.setBackgroundColor(0);
		}
		
		// update the name
		int nameC = cursor.getColumnIndex(MembersDbAdapter.COL_NAME);
		String name = cursor.getString(nameC);
		holder.name.setText(name);
		
		// update the image
		int imgC = cursor.getColumnIndex(MembersDbAdapter.COL_PIC);
		holder.image.setImageResource(R.drawable.missing_photo);
		if (!cursor.isNull(imgC)) {
			byte[] img = cursor.getBlob(imgC);
			holder.image.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.length));
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
}
