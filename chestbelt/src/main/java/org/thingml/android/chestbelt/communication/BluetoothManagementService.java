package org.thingml.android.chestbelt.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import org.thingml.android.chestbelt.communication.ConnectionTask.ConnectionTaskReceiver;
import org.thingml.android.chestbelt.devices.Device;
import org.thingml.android.chestbelt.devices.DevicesListActivity;
import org.thingml.android.chestbelt.preferences.ChestBeltPrefFragment;
import org.thingml.android.chestbelt.sensapp.ChestBeltDatabaseLoger;
import org.thingml.android.chestbelt.sensapp.UpdateUriTask;
import org.thingml.android.chestbelt.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BluetoothManagementService extends Service implements ConnectionTaskReceiver {
	
	public static final String ACTION_DEVICE_DETECTED = BluetoothManagementService.class.getName() + ".ACTION_DEVICE_DETECTED";
	public static final String ACTION_DISCOVERY_STARTED = BluetoothManagementService.class.getName() + ".ACTION_DISCOVERY_STARTED";
	public static final String ACTION_DISCOVERY_ENDED = BluetoothManagementService.class.getName() + ".ACTION_DISCOVERY_ENDED";
	public static final String ACTION_CONNECTION_FAILURE = BluetoothManagementService.class.getName() + ".ACTION_CONNECTION_FAILURE";
	public static final String ACTION_CONNECTION_SUCCESS = BluetoothManagementService.class.getName() + ".ACTION_CONNECTION_SUCCESS";
	public static final String ACTION_DEVICE_DISCONNECTED = BluetoothManagementService.class.getName() + ".ACTION_DEVICE_DISCONNECTED";

	public static final String EXTRA_DEVICE_IS_CONNECTED = BluetoothManagementService.class.getName() + ".EXTRA_DEVICE_IS_CONNECTED";		
	public static final String EXTRA_DEVICE_IS_AVAILABLE = BluetoothManagementService.class.getName() + ".EXTRA_DEVICE_IS_AVAILABLE";		
	
	private BluetoothAdapter btAdapter;
	private Hashtable<String, ChestBeltDriver> runningSessions = new Hashtable<String, ChestBeltDriver>();
	private Hashtable<String, ConnectionTask> connectTasks = new Hashtable<String, ConnectionTask>();
	private Hashtable<String, ChestBeltDatabaseLoger> databaseLogers = new Hashtable<String, ChestBeltDatabaseLoger>();
	private Hashtable<String, ChestBeltGraphBufferizer> graphBufferizers = new Hashtable<String, ChestBeltGraphBufferizer>();
	private ArrayList<String> prefixFilter = new ArrayList<String>();
	private SharedPreferences prefs;
	private NotificationManager notificationManager;
	private Notification connectionNotification;
	private boolean isApplicationRunning = false;
	
	private final ChestBeltBinder chestBeltBinder = new ChestBeltBinder();
	
	private static final String TAG = BluetoothManagementService.class.getSimpleName();
	private static final int CONNECTION_NOTIFICATION_ID = 1001;
	
	@Override
	public void onCreate() {
		super.onCreate();
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(spChanged);
		setupNotification();
		registerReceivers();
		prefixFilter.add("ESUMS");
		prefixFilter.add("CORBYS");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if (action.equals(DevicesListActivity.ACTION_ASK_START_DISCOVERY)) {
			Log.d(TAG, "Start service: ACTION_ASK_START_DISCOVERY");
			startDiscovery();
		} else if (action.equals(DevicesListActivity.ACTION_ASK_END_DISCOVERY)) {
			Log.d(TAG, "Start service: ACTION_ASK_END_DISCOVERY");
			stopDiscovery();
		} else if (action.equals(DevicesListActivity.ACTION_ASK_BOUNDED_DEVICES)) {
			Log.d(TAG, "Start service: ACTION_ASK_BOUNDED_DEVICES");
			sendBondedDevices();
		} else if (action.equals(DevicesListActivity.ACTION_ASK_CONNECT)) {
			Log.d(TAG, "Start service: ACTION_ASK_CONNECT");
			Bundle extra = intent.getExtras(); 
			newConnection(extra.getString(Device.EXTRA_DEVICE_NAME), extra.getString(Device.EXTRA_DEVICE_ADDRESS), extra.getInt(DevicesListActivity.EXTRA_CONNECTION_MODE));
		} else if (action.equals(DevicesListActivity.ACTION_ASK_DISCONNECT)) {
			Log.d(TAG, "Start service: ACTION_ASK_DISCONNECT");
			Bundle extra = intent.getExtras();
			endConnection(extra.getString(Device.EXTRA_DEVICE_ADDRESS));
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
		return START_STICKY_COMPATIBILITY;
	}
	
	public class ChestBeltBinder extends Binder {
		public ChestBeltDriver getDriver(String address) {
			return runningSessions.get(address);
		}
        public ChestBeltGraphBufferizer getGraphBufferizers(String address) {
        	return graphBufferizers.get(address);
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return chestBeltBinder;
	}
	
	private void startDiscovery() {
		Log.d(TAG, "Starting dicovery...");
		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
		btAdapter.startDiscovery();
		sendBroadcast(new Intent(BluetoothManagementService.ACTION_DISCOVERY_STARTED));
	}
	
	private void stopDiscovery() {
		Log.d(TAG, "Stopping dicovery...");
		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
	}
	
	private boolean applyFilter(String name) {
		for (String prefix : prefixFilter) {
			if (name.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}
	
	private void sendBondedDevices() {
		for (BluetoothDevice device : btAdapter.getBondedDevices()) {
			if (applyFilter(device.getName())) {
				newDevice(device, false);
			}
		}
	}
	
	private void newConnection(String name, String address, int mode) {
		if (runningSessions.containsKey(address)) {
			Log.w(TAG, "Reconnect to " + name + " (" + address + ")");
			endConnection(address);
		}
		doConnection(address, mode);
	}

	private void endConnection(String address) {
		BluetoothDevice d = btAdapter.getRemoteDevice(address);
		String name = d.getName();
		closeExchange(address);
		connectTasks.remove(address);
		runningSessions.remove(address);
		if (runningSessions.isEmpty()) {
			stopForeground(true);
		}
		databaseLogers.remove(address);
		Toast.makeText(getApplicationContext(), "Disconnected from " + name, Toast.LENGTH_SHORT).show();
		Intent i = new Intent(ACTION_DEVICE_DISCONNECTED);
		i.putExtra(Device.EXTRA_DEVICE_NAME, name);
		i.putExtra(Device.EXTRA_DEVICE_ADDRESS, address);
		sendBroadcast(i);
	}

	private ChestBeltDriver initExchange(String address) {
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
		return new ChestBeltDriver(in, out);
	}

	private void closeExchange(String address) {
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

	private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String name = device.getName() != null ? device.getName() : "Unknown"; 
				Log.i(TAG, "Device detected - Name: " + name + " Adress: " + device.getAddress());
				if (device.getBondState() == BluetoothDevice.BOND_BONDED && applyFilter(name)) {
					newDevice(device, true);
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				Log.i(TAG, "...Discovery finished");
				sendBroadcast(new Intent(BluetoothManagementService.ACTION_DISCOVERY_ENDED));
			}
		}
	};

	private void newDevice(BluetoothDevice device, boolean available) {
			Intent i = new Intent(ACTION_DEVICE_DETECTED);
			i.putExtra(Device.EXTRA_DEVICE_NAME, device.getName());
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, device.getAddress());
			i.putExtra(BluetoothManagementService.EXTRA_DEVICE_IS_AVAILABLE, available);
			boolean isConnected = runningSessions.containsKey(device.getAddress()) ? true : false;
			i.putExtra(BluetoothManagementService.EXTRA_DEVICE_IS_CONNECTED, isConnected);
			sendBroadcast(i);
	}

	SharedPreferences.OnSharedPreferenceChangeListener spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			String datamodeKey = getString(R.string.pref_datamode_key);
			String storageKey = getString(R.string.pref_data_storage);
			String ecgStorageKey = getString(R.string.pref_ecg_storage);
			String serverKey = getString(R.string.pref_sensor_server);
			String portKey = getString(R.string.pref_sensor_port);
			if (key.equals(storageKey)) {
				refreshStorage();
			} else if (key.equals(serverKey) || key.equals(portKey)) {
				refreshUri();
			} else if (key.equals(datamodeKey)) {
				refreshDataMode();
			} else if (key.equals(ecgStorageKey)) {
				refreshECGStorage();
			}
		}
	};

	private void registerReceivers() {
		registerReceiver(btReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		registerReceiver(btReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
	}
	
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
	
	private void refreshUri() {
		String newUri = prefs.getString(getString(R.string.pref_sensor_server), "Invalid").trim() + ":" + prefs.getString(getString(R.string.pref_sensor_port), "0").trim();
		for (String prefix : prefixFilter) {
			new UpdateUriTask(this, prefix).execute(newUri);
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
		for (ChestBeltDriver session : runningSessions.values()) {
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
		unregisterReceiver(btReceiver);
		prefs.unregisterOnSharedPreferenceChangeListener(spChanged);
	}

	@Override
	public void onConnectionSuccess(String name, String address) {
		Toast.makeText(getApplicationContext(), "Connected to " + name, Toast.LENGTH_SHORT).show();
		ChestBeltDriver newSession = initExchange(address);
		if (newSession != null) {
			runningSessions.put(address, newSession);
			ChestBeltDatabaseLoger databaseLoger = new ChestBeltDatabaseLoger(this, name);
			databaseLogers.put(address, databaseLoger);
			ChestBeltGraphBufferizer bufferizer = new ChestBeltGraphBufferizer();
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
		} else {
			onConnectionFailure(name, address);
		}
	}
	
	@Override
	public void onConnectionFailure(String name, String address) {
		Toast.makeText(getApplicationContext(), "Unable to connect to " + name, Toast.LENGTH_LONG).show();
		Intent i = new Intent(ACTION_CONNECTION_FAILURE);
		i.putExtra(Device.EXTRA_DEVICE_NAME, name);
		i.putExtra(Device.EXTRA_DEVICE_ADDRESS, address);
		sendBroadcast(i);
	}
}
