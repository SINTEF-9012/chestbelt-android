package org.thingml.chestbelt.android.chestbeltdroid.sensapp;

import java.util.ArrayList;

import org.sensapp.android.sensappdroid.contract.SensAppContract;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class UpdateUriTask extends AsyncTask<String, Void, Integer> {

	private static final String TAG = UpdateUriTask.class.getSimpleName();
	
	private Context context;
	private String prefix;
	
	public UpdateUriTask(Context context, String prefix) {
		this.context = context;
		this.prefix = prefix;
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		String uri = params[0];
		ArrayList<String> namesToUpdate = new ArrayList<String>();
		Cursor cursor = context.getContentResolver().query(SensAppContract.Sensor.CONTENT_URI, new String[]{SensAppContract.Sensor.NAME}, null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndex(SensAppContract.Sensor.NAME));
				if (name.startsWith(prefix)) {
					namesToUpdate.add(name);
				}
			}
			cursor.close();
		}
		ContentValues values = new ContentValues();
		values.put(SensAppContract.Sensor.URI, uri);
		values.put(SensAppContract.Sensor.UPLOADED, 0);
		int rowUpdated = 0;
		for (String name : namesToUpdate) {
			rowUpdated += context.getContentResolver().update(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + name), values, null, null);
		}
		return rowUpdated;
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		Log.i(TAG, "Updated for prefix " + prefix + ":" + result);
	}
}
