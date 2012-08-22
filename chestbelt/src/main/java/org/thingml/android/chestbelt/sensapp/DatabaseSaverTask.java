package org.thingml.android.chestbelt.sensapp;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

public class DatabaseSaverTask extends AsyncTask<ContentValues, Void, Integer> {

	private Context context;
	private Uri uri;
	
	public DatabaseSaverTask(Context context, Uri uri) {
		this.context = context;
		this.uri = uri;
	}
	
	@Override
	protected Integer doInBackground(ContentValues... params) {
		int nbInsert = 0;
		for (ContentValues values : params) {
			context.getContentResolver().insert(uri, values);
			nbInsert ++;
		}
		return nbInsert;
	}
}
