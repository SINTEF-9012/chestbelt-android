package org.thingml.chestbelt.android.chestbeltdroid.sensapp;

import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.contract.SensAppContract;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

public class DatabaseSaverTask extends AsyncTask<ContentValues, Void, Void> {

	private Context context;
	private Uri uri;
	
	public DatabaseSaverTask(Context context, Uri uri) {
		this.context = context;
		this.uri = uri;
	}
	
	@Override
	protected Void doInBackground(ContentValues... params) {
		String sensor = params[0].getAsString(SensAppContract.Measure.SENSOR);
		String value = params[0].getAsString(SensAppContract.Measure.VALUE);
		long basetime = params[0].getAsLong(SensAppContract.Measure.BASETIME);
		long time = params[0].getAsLong(SensAppContract.Measure.TIME);
		SensAppHelper.insertMeasure(context, sensor, value, basetime, time);
		return null;
	}
}
