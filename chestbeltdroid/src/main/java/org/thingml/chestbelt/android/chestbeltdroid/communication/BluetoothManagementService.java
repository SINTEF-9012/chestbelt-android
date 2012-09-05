package org.thingml.chestbelt.android.chestbeltdroid.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltGraphBufferizer.ChestBeltCallback;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ConnectionTask.ConnectionTaskReceiver;
import org.thingml.chestbelt.android.chestbeltdroid.devices.Device;
import org.thingml.chestbelt.android.chestbeltdroid.devices.DevicesListActivity;
import org.thingml.chestbelt.android.chestbeltdroid.preferences.ChestBeltPrefFragment;
import org.thingml.chestbelt.android.chestbeltdroid.sensapp.ChestBeltDatabaseLoger;
import org.thingml.chestbelt.driver.ChestBelt;
import org.thingml.chestbelt.driver.ChestBeltMode;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BluetoothManagementService extends Service implements ConnectionTaskReceiver, ChestBeltCallback {
	
	public static final String ACTION_CONNECTION_FAILURE = BluetoothManagementService.class.getName() + ".ACTION_CONNECTION_FAILURE";
	public static final String ACTION_CONNECTION_SUCCESS = BluetoothManagementService.class.getName() + ".ACTION_CONNECTION_SUCCESS";
	public static final String ACTION_DEVICE_DISCONNECTED = BluetoothManagementService.class.getName() + ".ACTION_DEVICE_DISCONNECTED";
	public static final String ACTION_CONNECTED_DEVICES = BluetoothManagementService.class.getName() + ".ACTION_CONNECTED_DEVICES";
			
	public static final String EXTRA_CONNECTED_DEVICE_ADDRESSES = BluetoothManagementService.class.getName() + ".EXTRA_CONNECTED_DEVICE_ADDRESSES";
	
	private BluetoothAdapter btAdapter;
	private Hashtable<String, ChestBelt> runningSessions = new Hashtable<String, ChestBelt>();
	private Hashtable<String, ConnectionTask> connectTasks = new Hashtable<String, ConnectionTask>();
	private Hashtable<String, ChestBeltDatabaseLoger> databaseLogers = new Hashtable<String, ChestBeltDatabaseLoger>();
	private Hashtable<String, ChestBeltGraphBufferizer> graphBufferizers = new Hashtable<String, ChestBeltGraphBufferizer>();
	private SharedPreferences prefs;
	private NotificationManager notificationManager;
	private Notification connectionNotification;
	private boolean isApplicationRunning = false;
	
	private final ChestBeltBinder chestBeltBinder = new ChestBeltBinder(runningSessions, graphBufferizers);
	
	private static final String TAG = BluetoothManagementService.class.getSimpleName();
	private static final int CONNECTION_NOTIFICATION_ID = 1001;
	
	@Override
	public void onCreate() {
		super.onCreate();
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(spChanged);
		setupNotification();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action.equals(DevicesListActivity.ACTION_ASK_CONNECT)) {
			Log.d(TAG, "Start service: ACTION_ASK_CONNECT");
			newConnection(intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS));
		} else if (action.equals(DevicesListActivity.ACTION_ASK_DISCONNECT)) {
			Log.d(TAG, "Start service: ACTION_ASK_DISCONNECT");
			Bundle extra = intent.getExtras();
			endConnection(extra.getString(Device.EXTRA_DEVICE_ADDRESS));
		} else if (action.equals(DevicesListActivity.ACTION_ASK_CONNECTED_DEVICES)) {
			Log.d(TAG, "Start service: ACTION_ASK_CONNECTED_DEVICES");
			Intent i = new Intent(ACTION_CONNECTED_DEVICES);
			i.putExtra(EXTRA_CONNECTED_DEVICE_ADDRESSES, runningSessions.keySet().toArray(new String[runningSessions.size()]));
			sendBroadcast(i);
		} else if (action.equals(DevicesListActivity.ACTION_OPENED)) {
			Log.d(TAG, "Start service: ACTION_OPENED");
			isApplicationRunning = true;
		} else if (action.equals(DevicesListActivity.ACTION_CLOSED)) {
			Log.d(TAG, "Start service: ACTION_CLOSED");
			isApplicationRunning = false;
			Handler stopService = new Handler();
			stopService.postDelayed(new Runnable() {
				public void run() {
					Log.d(TAG, "Post Handler - A session is running: " + !runningSessions.isEmpty());
					if (!isApplicationRunning && runningSessions.isEmpty()) {
						Log.d(TAG, "Stopping service");
						stopSelf();
					}
				}
			}, 5000);
		} 
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return chestBeltBinder;
	}
	
	private void newConnection(String address) {
		String connectMode = prefs.getString("Connection_list_preference", "40");
		int mode = Integer.parseInt(connectMode);
		if (runningSessions.containsKey(address)) {
			Log.e(TAG, "Device " + address + " already connected");
			return;
		}
		doConnection(address, mode);
	}

	private void endConnection(String address) {
		BluetoothDevice d = btAdapter.getRemoteDevice(address);
		chestBeltBinder.deviceDisconnected(address);
		closeExchange(address);
		connectTasks.remove(address);
		runningSessions.remove(address);
		if (runningSessions.isEmpty()) {
			stopForeground(true);
		}
		graphBufferizers.remove(address);
		databaseLogers.remove(address);
		Toast.makeText(getApplicationContext(), "Disconnected from " + d.getName(), Toast.LENGTH_SHORT).show();
		Intent i = new Intent(ACTION_DEVICE_DISCONNECTED);
		i.putExtra(Device.EXTRA_DEVICE_ADDRESS, address);
		sendBroadcast(i);
	}

	private ChestBelt initExchange(String address) {
		BluetoothSocket socket = connectTasks.get(address).getSocket();
		InputStream in;
		OutputStream out;
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new ChestBelt(in, out);
	}

	private void closeExchange(String address) {
		runningSessions.get(address).close();
		BluetoothSocket socket = connectTasks.get(address).getSocket();
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setupNotification() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		connectionNotification = new Notification(R.drawable.ic_launcher, "Connected", System.currentTimeMillis());
		connectionNotification.flags |= Notification.FLAG_ONGOING_EVENT; 
		Intent notificationIntent = new Intent(this, DevicesListActivity.class);
		PendingIntent pNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		connectionNotification.setLatestEventInfo(getApplicationContext(), getString(R.string.app_name), "Connected", pNotificationIntent);
	}

	private void doConnection(String btDeviceAddress, int mode) {
		BluetoothDevice device = btAdapter.getRemoteDevice(btDeviceAddress);
		btAdapter.cancelDiscovery();
		connectTasks.put(btDeviceAddress, (ConnectionTask) new ConnectionTask(this, device).execute(mode));
	}

	SharedPreferences.OnSharedPreferenceChangeListener spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			String datamodeKey = getString(R.string.pref_datamode_key);
			String storageKey = getString(R.string.pref_data_storage);
			String ecgStorageKey = getString(R.string.pref_ecg_storage);
			if (key.equals(storageKey)) {
				refreshStorage();
			} else if (key.equals(datamodeKey)) {
				refreshDataMode();
			} else if (key.equals(ecgStorageKey)) {
				refreshECGStorage();
			}
		}
	};
	
	private void refreshStorage() {
		for (ChestBeltDatabaseLoger loger : databaseLogers.values()) {
			loger.setStorage(prefs.getBoolean(getString(R.string.pref_data_storage), false));
		}
	}
	
	private void refreshECGStorage() {
		for (ChestBeltDatabaseLoger loger : databaseLogers.values()) {
			loger.setECGStorage(prefs.getBoolean(getString(R.string.pref_ecg_storage), false));
		}
	}
	
	private void refreshDataMode() {
		switch (Integer.valueOf(prefs.getString(getString(R.string.pref_datamode_key), String.valueOf(ChestBeltPrefFragment.DATAMODE_EXTRACTED)))) {
		case ChestBeltPrefFragment.DATAMODE_EXTRACTED:
			setDataMode(ChestBeltMode.Extracted);
			break;
		case ChestBeltPrefFragment.DATAMODE_FULLECG:
			setDataMode(ChestBeltMode.FullECG);
			break;
		case ChestBeltPrefFragment.DATAMODE_RAW:
			setDataMode(ChestBeltMode.Raw);
			break;
		case ChestBeltPrefFragment.DATAMODE_RAWACCELEROMETER:
			setDataMode(ChestBeltMode.RawAccelerometer);
			break;
		case ChestBeltPrefFragment.DATAMODE_RAWGYROMODE:
			setDataMode(ChestBeltMode.RawGyroMode);
			break;
		case ChestBeltPrefFragment.DATAMODE_TEST:
			setDataMode(ChestBeltMode.Test);
			break;
		}
	}
	
	private void setDataMode(ChestBeltMode mode) {
		for (ChestBelt session : runningSessions.values()) {
			session.setDataMode(mode);
		}
		Log.i(TAG, "Data mode set to: " + mode);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		for (String address : runningSessions.keySet()) {
			closeExchange(address);
		}
		notificationManager.cancel(CONNECTION_NOTIFICATION_ID);
		prefs.unregisterOnSharedPreferenceChangeListener(spChanged);
	}

	@Override
	public void onConnectionSuccess(String name, String address) {
		Toast.makeText(getApplicationContext(), "Connected to " + name, Toast.LENGTH_SHORT).show();
		ChestBelt newSession = initExchange(address);
		if (newSession != null) {
			runningSessions.put(address, newSession);
			ChestBeltDatabaseLoger databaseLoger = new ChestBeltDatabaseLoger(this, name);
			databaseLogers.put(address, databaseLoger);
			ChestBeltGraphBufferizer bufferizer = new ChestBeltGraphBufferizer(this, address);
			graphBufferizers.put(address, bufferizer);
			newSession.addChestBeltListener(databaseLoger);
			newSession.addChestBeltListener(bufferizer);
			newSession.setLiveDataMode();
			newSession.connectionRestored();
			newSession.setBTUpdateInterval(1);
			refreshDataMode();
			startForeground(CONNECTION_NOTIFICATION_ID, connectionNotification);
			Intent i = new Intent(ACTION_CONNECTION_SUCCESS);
			i.putExtra(Device.EXTRA_DEVICE_NAME, name);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, address);
			sendBroadcast(i);
			chestBeltBinder.deviceConnected(address);
			if (autoReconnect) {
				autoReconnectTimer.cancel();
				autoReconnect = false;
				Log.e(TAG, "Auto reconnect canceled");
			}
		} else {
			onConnectionFailure(name, address, null);
		}
	}
	
	@Override
	public void onConnectionFailure(String name, String address, String errorMessage) {
		if (errorMessage != null) {
			Toast.makeText(getApplicationContext(), "Unable to connect to " + name + ":\n" + errorMessage, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), "Unable to connect to " + name, Toast.LENGTH_LONG).show();
		}
		Intent i = new Intent(ACTION_CONNECTION_FAILURE);
		i.putExtra(Device.EXTRA_DEVICE_NAME, name);
		i.putExtra(Device.EXTRA_DEVICE_ADDRESS, address);
		sendBroadcast(i);
	}

	private Timer autoReconnectTimer;
	private boolean autoReconnect = false;
	private int autoReconnectDelay;
	
	@Override
	public void connectionLost(final String address) {
		// Use handler because called by a worker thread
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				endConnection(address);	
			}
		});
		autoReconnectTimer = new Timer();
		autoReconnectTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				newConnection(address);
			}
		}, 10000, 10000);
		autoReconnect = true;
		Log.e(TAG, "Auto reconnect started");
	}
}
