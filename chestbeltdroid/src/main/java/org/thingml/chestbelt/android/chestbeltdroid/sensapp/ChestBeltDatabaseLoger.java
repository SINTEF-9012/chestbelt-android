package org.thingml.chestbelt.android.chestbeltdroid.sensapp;

import org.sensapp.android.sensappdroid.api.SensAppBackend;
import org.sensapp.android.sensappdroid.api.SensAppHelper;
import org.sensapp.android.sensappdroid.api.SensAppTemplate;
import org.sensapp.android.sensappdroid.api.SensAppUnit;
import org.sensapp.android.sensappdroid.contract.SensAppContract;
import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltBufferizer;
import org.thingml.chestbelt.driver.ChestBeltListener;

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
	private boolean imuStorage = false;
	
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
	
	public void setIMUStorage(boolean imuStorage) {
		this.imuStorage = imuStorage;
	}
	
	private void savetoDatabase(String sensor, int value, long time, long basetime) {
		SensAppHelper.insertMeasure(context, sensor, value, basetime, time);
	}
	
	private void savetoDatabase(String sensor, float value, long time, long basetime) {
		SensAppHelper.insertMeasure(context, sensor, value, basetime, time);
	}
	
	private void savetoDatabase(String sensor, String value, long time, long basetime) {
		SensAppHelper.insertMeasure(context, sensor, value, basetime, time);
	}
	
	private void checkAndRegisterSensors() {
			Log.w(TAG, "Sensor register check");
			boolean sensorsRegisterSuccess = registerSensor(prefix + SensorNames.BATTERY_STATUS, "Battery level", SensAppUnit.BATTERY_LEVEL, SensAppTemplate.NUMERICAL) 
					& registerSensor(prefix + SensorNames.HEART_RATE, "Heart rate", SensAppUnit.BITS_SECOND, SensAppTemplate.NUMERICAL)
					& registerSensor(prefix + SensorNames.SKIN_TEMPERATURE, "Skin temperature", SensAppUnit.DEGREES_CELSIUS, SensAppTemplate.NUMERICAL)
					& registerSensor(prefix + SensorNames.ACTIVITY, "Activity", SensAppUnit.COUNTER_VALUE, SensAppTemplate.NUMERICAL)
					& registerSensor(prefix + SensorNames.POSITION, "Position", SensAppUnit.COUNTER_VALUE, SensAppTemplate.NUMERICAL)
					& registerSensor(prefix + SensorNames.ECGDATA, "ECG Data", SensAppUnit.VOLT, SensAppTemplate.STRING)
					& registerSensor(prefix + SensorNames.GYROPITCH, "Gyroscope pitch", SensAppUnit.RADIAN, SensAppTemplate.STRING)
					& registerSensor(prefix + SensorNames.GYROROLL, "Gyroscope roll", SensAppUnit.RADIAN, SensAppTemplate.STRING)
					& registerSensor(prefix + SensorNames.GYROYAW, "Gyroscope yaw", SensAppUnit.RADIAN, SensAppTemplate.STRING)
					& registerSensor(prefix + SensorNames.ACCLATERAL, "Lateral accelerometer", SensAppUnit.ACCELERATION, SensAppTemplate.STRING)
					& registerSensor(prefix + SensorNames.ACCLONGITUDINAL, "Longitudinal accelerometer", SensAppUnit.ACCELERATION, SensAppTemplate.STRING)
					& registerSensor(prefix + SensorNames.ACCVERTICAL, "Vertical accelerometer", SensAppUnit.ACCELERATION, SensAppTemplate.STRING);
			if (!sensorsRegisterSuccess) {
				Log.e(TAG, "ERROR REGISTER SENSOR");
				storage = false;
		}
	}
	
	private boolean registerSensor(String name, String description, SensAppUnit unit, SensAppTemplate template) {
		Cursor cursor = context.getContentResolver().query(Uri.parse(SensAppContract.Sensor.CONTENT_URI + "/" + name), new String[]{SensAppContract.Sensor.NAME}, null, null, null);
		if (cursor != null) {
			boolean exists = cursor.getCount() > 0;
			cursor.close();
			if (!exists) {
				SensAppHelper.registerSensor(context, name, description, unit, SensAppBackend.raw, template, R.drawable.ic_launcher);
				Log.i(TAG, "Sensor registered - name: " + name);
				return true;
			} else {
				Log.w(TAG, "Sensor already exits: " + name);
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
		if (storage && imuStorage) {
			bufGyroPitch.addMeasure(value, System.currentTimeMillis());
			if (bufGyroPitch.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROPITCH, bufGyroPitch.toString(), bufGyroPitch.getStartTime()/1000, 0);
				bufGyroPitch.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void gyroRoll(int value, int timestamp) {
		if (storage && imuStorage) {
			bufGyroRoll.addMeasure(value, System.currentTimeMillis());
			if (bufGyroRoll.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROROLL, bufGyroRoll.toString(), bufGyroRoll.getStartTime()/1000, 0);
				bufGyroRoll.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void gyroYaw(int value, int timestamp) {
		if (storage && imuStorage) {
			bufGyroYaw.addMeasure(value, System.currentTimeMillis());
			if (bufGyroYaw.isReady()) {
				savetoDatabase(prefix + SensorNames.GYROYAW, bufGyroYaw.toString(), bufGyroYaw.getStartTime()/1000, 0);
				bufGyroYaw.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void accLateral(int value, int timestamp) {
		if (storage && imuStorage) {
			bufAccLateral.addMeasure(value, System.currentTimeMillis());
			if (bufAccLateral.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCLATERAL, bufAccLateral.toString(), bufAccLateral.getStartTime()/1000, 0);
				bufAccLateral.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void accLongitudinal(int value, int timestamp) {
		if (storage && imuStorage) {
			bufAccLongitudinal.addMeasure(value, System.currentTimeMillis());
			if (bufAccLongitudinal.isReady()) {
				savetoDatabase(prefix + SensorNames.ACCLONGITUDINAL, bufAccLongitudinal.toString(), bufAccLongitudinal.getStartTime()/1000, 0);
				bufAccLongitudinal.reset(System.currentTimeMillis(), 2000);
			}
		}
	}

	@Override
	public void accVertical(int value, int timestamp) {
		if (storage && imuStorage) {
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
		if (storage && imuStorage) {
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

	@Override
	public void connectionLost() {
		// TODO Auto-generated method stub
		
	}
}
