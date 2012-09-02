package org.thingml.chestbelt.android.chestbeltdroid.sensapp;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltBufferizer;
import org.thingml.chestbelt.driver.ChestBeltListener;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class ChestBeltDatabaseLoger implements ChestBeltListener {

	public static final String ACTION_MESSAGE_RECEIVED = ChestBeltDatabaseLoger.class.getName() + ".ACTION_MESSAGE_RECEIVED";
	
	public static final String EXTRA_MESSAGE_CODE = ChestBeltDatabaseLoger.class.getName() + ".EXTRA_MESSAGE_CODE";
	public static final String EXTRA_MESSAGE_VALUE = ChestBeltDatabaseLoger.class.getName() + ".EXTRA_MESSAGE_VALUE";
	public static final String EXTRA_MESSAGE_TIMESTAMP = ChestBeltDatabaseLoger.class.getName() + ".EXTRA_MESSAGE_TIMESTAMP";
	
	private final static String TAG = ChestBeltDatabaseLoger.class.getSimpleName();
	
	private static final class SensorNames {
		public static final String BATTERY_STATUS = "_Battery"; 
		public static final String HEART_RATE = "_Heartrate";
		public static final String SKIN_TEMPERATURE = "_Temperature";
		public static final String ACTIVITY = "_Activity";
		public static final String POSITION = "_Position";
		public static final String ECGDATA = "_ECGData";
		public static final String GYROPITCH = "_GyroPitch";
		public static final String GYROROLL = "_GyroRoll";
		public static final String GYROYAW = "_GyroYaw";
		public static final String ACCLATERAL = "_AccLateral";
		public static final String ACCLONGITUDINAL = "_AccLongitudinal";
		public static final String ACCVERTICAL = "_AccVertical";
	}
	
	private Context context;
	private String prefix;
	private boolean storage = false;
	private boolean ecgStorage = false;
	
	private ChestBeltBufferizer bufECG = new ChestBeltBufferizer(System.currentTimeMillis(), 2000);
	private ChestBeltBufferizer bufAccLateral = new ChestBeltBufferizer(System.currentTimeMillis(), 2000);
	private ChestBeltBufferizer bufAccLongitudinal = new ChestBeltBufferizer(System.currentTimeMillis(), 2000);
	private ChestBeltBufferizer bufAccVertical = new ChestBeltBufferizer(System.currentTimeMillis(), 2000);
	private ChestBeltBufferizer bufGyroPitch = new ChestBeltBufferizer(System.currentTimeMillis(), 2000);
	private ChestBeltBufferizer bufGyroRoll = new ChestBeltBufferizer(System.currentTimeMillis(), 2000);
	private ChestBeltBufferizer bufGyroYaw = new ChestBeltBufferizer(System.currentTimeMillis(), 2000);
	
	public ChestBeltDatabaseLoger(Context context, String prefix) {
		this.context = context;
		this.prefix = prefix;
		setStorage(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_data_storage), false));
		setECGStorage(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_ecg_storage), false));
	}
	
	public void setStorage(boolean storage) {
		this.storage = storage;
		if (storage) {
			checkAndRegisterSensors();
		}
	}
	public void setECGStorage(boolean ecgStorage) {
		this.ecgStorage = ecgStorage;
	}
	
	private ContentValues prepareContentValues(String sensor, long time, long basetime) {
		ContentValues values = new ContentValues();
		values.put(SensAppCPContract.Measure.SENSOR, sensor);
		values.put(SensAppCPContract.Measure.TIME, time);
		values.put(SensAppCPContract.Measure.BASETIME, basetime);
		values.put(SensAppCPContract.Measure.UPLOADED, 0);
		return values;
	}
	
	private void insertInDatabase(ContentValues values) {
		new DatabaseSaverTask(context, SensAppCPContract.Measure.CONTENT_URI).execute(values);
	}
	
	private void savetoDatabase(String sensor, int value, long time, long basetime) {
		ContentValues values = prepareContentValues(sensor, time, basetime);
		values.put(SensAppCPContract.Measure.VALUE, String.valueOf(value));
		insertInDatabase(values);
	}
	
	private void savetoDatabase(String sensor, float value, long time, long basetime) {
		ContentValues values = prepareContentValues(sensor, time, basetime);
		values.put(SensAppCPContract.Measure.VALUE, String.valueOf(value));
		insertInDatabase(values);
	}
	
	private void savetoDatabase(String sensor, String value, long time, long basetime) {
		ContentValues values = prepareContentValues(sensor, time, basetime);
		values.put(SensAppCPContract.Measure.VALUE, value);
		insertInDatabase(values);
	}
	
	private void checkAndRegisterSensors() {
			Log.w(TAG, "Sensor register check");
			boolean sensorsRegisterSuccess = registerSensor(prefix + SensorNames.BATTERY_STATUS, "%", "Numerical", "Battery level") 
					& registerSensor(prefix + SensorNames.HEART_RATE, "count", "Numerical", "Heart rate")
					& registerSensor(prefix + SensorNames.SKIN_TEMPERATURE, "degC", "Numerical", "Skin temperature")
					& registerSensor(prefix + SensorNames.ACTIVITY, "count", "Numerical", "Activity")
					& registerSensor(prefix + SensorNames.POSITION, "count", "Numerical", "Position")
					& registerSensor(prefix + SensorNames.ECGDATA, "count", "String", "ECG Data")
					& registerSensor(prefix + SensorNames.GYROPITCH, "count", "String", "Gyroscope pitch")
					& registerSensor(prefix + SensorNames.GYROROLL, "count", "String", "Gyroscope roll")
					& registerSensor(prefix + SensorNames.GYROYAW, "count", "String", "Gyroscope yaw")
					& registerSensor(prefix + SensorNames.ACCLATERAL, "count", "String", "Lateral accelerometer")
					& registerSensor(prefix + SensorNames.ACCLONGITUDINAL, "count", "String", "Longitudinal accelerometer")
					& registerSensor(prefix + SensorNames.ACCVERTICAL, "count", "String", "Vertical accelerometer");
			if (!sensorsRegisterSuccess) {
				Log.e(TAG, "ERROR REGISTER SENSOR");
				storage = false;
		}
	}
	
	private boolean registerSensor(String name, String unit, String template, String description) {
		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppCPContract.Sensor.CONTENT_URI + "/" + name), new String[]{SensAppCPContract.Sensor.NAME}, null, null, null);
		if (cursor != null) {
			boolean exists = cursor.getCount() > 0;
			cursor.close();
			if (!exists) {
				ContentValues values = new ContentValues();
				values.put(SensAppCPContract.Sensor.NAME, name);
				values.put(SensAppCPContract.Sensor.DESCRIPTION, description);
				values.put(SensAppCPContract.Sensor.BACKEND, "raw");
				values.put(SensAppCPContract.Sensor.TEMPLATE, template);
				values.put(SensAppCPContract.Sensor.UNIT, unit);
				values.put(SensAppCPContract.Sensor.UPLOADED, 0);
				context.getContentResolver().insert(SensAppCPContract.Sensor.CONTENT_URI, values);
				Log.i(TAG, "Sensor registered - name: " + name);
				return true;
			} else {
				Log.w(TAG, "Name already exits: " + name);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void cUSerialNumber(long value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void batteryStatus(int value, int timestamp) {
		if (storage) {
			savetoDatabase(prefix + SensorNames.BATTERY_STATUS, value, System.currentTimeMillis()/1000, 0);
		}
	}

	@Override
	public void indication(int value, int timestamp) {
		if (storage) {
			if (value >= 1 && value <= 6) { 
				savetoDatabase(prefix + SensorNames.POSITION, value, System.currentTimeMillis()/1000, 0);
			} else if (value >= 10 && value <= 13) {
				savetoDatabase(prefix + SensorNames.ACTIVITY, value - 10, System.currentTimeMillis()/1000, 0);
			}
		}
	}

	@Override
	public void status(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageOverrun(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void heartRate(int value, int timestamp) {
		if (storage) {
			savetoDatabase(prefix + SensorNames.HEART_RATE, (float) value / 10, System.currentTimeMillis()/1000, 0);
		}
	}

	@Override
	public void heartRateConfidence(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void eCGData(int value) {
		if (storage && ecgStorage) {
			bufECG.addMeasure(value, System.currentTimeMillis());
			if (bufECG.isReady()) {
				savetoDatabase(prefix + SensorNames.ECGDATA, bufECG.toString(), bufECG.getStartTime()/1000, 0);
				bufECG.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void eCGSignalQuality(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eCGRaw(int value, int timestamp) {
		if (storage && ecgStorage) {
			bufECG.addMeasure(value, System.currentTimeMillis());
			if (bufECG.isReady()) {
				savetoDatabase(prefix + SensorNames.ECGDATA, bufECG.toString(), bufECG.getStartTime()/1000, 0);
				bufECG.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void gyroPitch(int value, int timestamp) {
		if (storage) {
			bufGyroPitch.addMeasure(value, System.currentTimeMillis());
			if (bufGyroPitch.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROPITCH, bufGyroPitch.toString(), bufGyroPitch.getStartTime()/1000, 0);
				bufGyroPitch.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void gyroRoll(int value, int timestamp) {
		if (storage) {
			bufGyroRoll.addMeasure(value, System.currentTimeMillis());
			if (bufGyroRoll.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROROLL, bufGyroRoll.toString(), bufGyroRoll.getStartTime()/1000, 0);
				bufGyroRoll.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void gyroYaw(int value, int timestamp) {
		if (storage) {
			bufGyroYaw.addMeasure(value, System.currentTimeMillis());
			if (bufGyroYaw.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROYAW, bufGyroYaw.toString(), bufGyroYaw.getStartTime()/1000, 0);
				bufGyroYaw.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void accLateral(int value, int timestamp) {
		if (storage) {
			bufAccLateral.addMeasure(value, System.currentTimeMillis());
			if (bufAccLateral.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCLATERAL, bufAccLateral.toString(), bufAccLateral.getStartTime()/1000, 0);
				bufAccLateral.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void accLongitudinal(int value, int timestamp) {
		if (storage) {
			bufAccLongitudinal.addMeasure(value, System.currentTimeMillis());
			if (bufAccLongitudinal.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCLONGITUDINAL, bufAccLongitudinal.toString(), bufAccLongitudinal.getStartTime()/1000, 0);
				bufAccLongitudinal.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void accVertical(int value, int timestamp) {
		if (storage) {
			bufAccVertical.addMeasure(value, System.currentTimeMillis());
			if (bufAccVertical.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCVERTICAL, bufAccVertical.toString(), bufAccVertical.getStartTime()/1000, 0);
				bufAccVertical.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void rawActivityLevel(int value, int timestamp) {
//		if (storage) {
//			savetoDatabase(prefix + SensorNames.ACTIVITY, value - 10, System.currentTimeMillis()/1000, 0);
//		}
	}

	@Override
	public void combinedIMU(int ax, int ay, int az, int gx, int gy, int gz, int timestamp) {
		if (storage) {
			bufAccLateral.addMeasure(ay, System.currentTimeMillis());
			if (bufAccLateral.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCLATERAL, bufAccLateral.toString(), bufAccLateral.getStartTime()/1000, 0);
				bufAccLateral.reset(System.currentTimeMillis(), 2000);
			}
			bufAccLongitudinal.addMeasure(az, System.currentTimeMillis());
			if (bufAccLongitudinal.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCLONGITUDINAL, bufAccLongitudinal.toString(), bufAccLongitudinal.getStartTime()/1000, 0);
				bufAccLongitudinal.reset(System.currentTimeMillis(), 2000);
			}
			bufAccVertical.addMeasure(ax, System.currentTimeMillis());
			if (bufAccVertical.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCVERTICAL, bufAccVertical.toString(), bufAccVertical.getStartTime()/1000, 0);
				bufAccVertical.reset(System.currentTimeMillis(), 2000);
			}
			bufGyroPitch.addMeasure(gy, System.currentTimeMillis());
			if (bufGyroPitch.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROPITCH, bufGyroPitch.toString(), bufGyroPitch.getStartTime()/1000, 0);
				bufGyroPitch.reset(System.currentTimeMillis(), 2000);
			}
			bufGyroRoll.addMeasure(gx, System.currentTimeMillis());
			if (bufGyroRoll.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROROLL, bufGyroRoll.toString(), bufGyroRoll.getStartTime()/1000, 0);
				bufGyroRoll.reset(System.currentTimeMillis(), 2000);
			}
			bufGyroYaw.addMeasure(gz, System.currentTimeMillis());
			if (bufGyroYaw.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROYAW, bufGyroYaw.toString(), bufGyroYaw.getStartTime()/1000, 0);
				bufGyroYaw.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void skinTemperature(int value, int timestamp) {
		if (storage) {
			savetoDatabase(prefix + SensorNames.SKIN_TEMPERATURE, (float) value / 10, System.currentTimeMillis()/1000, 0);
		}
	}

	@Override
	public void cUFWRevision(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fullClockTimeSync(long arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void referenceClockTime(long arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}
}
