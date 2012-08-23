package org.thingml.chestbelt.android.chestbeltdroid.devices;

import java.util.ArrayList;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.communication.BluetoothManagementService;
import org.thingml.chestbelt.android.chestbeltdroid.preferences.PreferencesActivity;
import org.thingml.chestbelt.android.chestbeltdroid.viewer.DashBoardActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which enable to connect devices and monitoring their status.
 * @author Fabien Fleurey
 */

public class DevicesListActivity extends ListActivity {
	
	public static final String ACTION_ASK_START_DISCOVERY = DevicesListActivity.class.getName() + ".ACTION_ASK_START_DISCOVERY";
	public static final String ACTION_ASK_END_DISCOVERY = DevicesListActivity.class.getName() + ".ACTION_ASK_END_DISCOVERY";
	public static final String ACTION_ASK_BOUNDED_DEVICES = DevicesListActivity.class.getName() + ".ACTION_ASK_BOUNDED_DEVICES";
	public static final String ACTION_ASK_CONNECT = DevicesListActivity.class.getName() + ".ACTION_ASK_CONNECT";
	public static final String ACTION_ASK_DISCONNECT = DevicesListActivity.class.getName() + ".ACTION_ASK_DISCONNECT";
	public static final String ACTION_OPENED = DevicesListActivity.class.getName() + ".ACTION_OPENED";
	public static final String ACTION_CLOSED = DevicesListActivity.class.getName() + ".ACTION_CLOSED";

	public static final String EXTRA_CONNECTION_MODE = DevicesListActivity.class.getName() + ".EXTRA_CONNECTION_MODE";
	
	private static final String TAG = DevicesListActivity.class.getSimpleName();
	private static final int REQUEST_ENABLE_BT = 10;
	private static final int DIALOG_DISCOVERY_ID = 20;
	private static final int DIALOG_CONNECTION_ID = 30;
	private static final int DIALOG_CONNECTION_CONFIRMATION = 40;
	private static final int ITEM_CONNECT_ID = Menu.FIRST + 1;
	private static final int ITEM_DISCONNECT_ID = Menu.FIRST + 2;
	
	private ArrayList<Device> devices = new ArrayList<Device>();
	private DevicesAdapter deviceAdapter;
	private SharedPreferences preferences;
	private boolean changeActivity = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "__ON_CREATE__");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		deviceAdapter = new DevicesAdapter(this, devices);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		setListViewProperties();
		if (BluetoothAdapter.getDefaultAdapter() == null) {
        	Log.e(TAG, "Bluetooth is not supported.");
        	Toast.makeText(this, "Bluetooth unsuported", Toast.LENGTH_LONG).show();
        	finish();
		} else if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
		} else {
			Intent i = new Intent(this, BluetoothManagementService.class);
			i.setAction(ACTION_ASK_BOUNDED_DEVICES);
			startService(i);
		}
		registerReceivers();
		registerForContextMenu(getListView());
		Intent i = new Intent(this, BluetoothManagementService.class);
		i.setAction(ACTION_OPENED);
		startService(i);
	}

	private void setListViewProperties() {
		TextView header = (TextView) getLayoutInflater().inflate(R.layout.device_list_header, null);
		getListView().addHeaderView(header, null, false);
		TextView footer = (TextView) getLayoutInflater().inflate(R.layout.device_list_footer, null);
		getListView().addFooterView(footer);
		setListAdapter(deviceAdapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (v.getId() == R.id.list_footer) {
			for (Device d : devices) {
				d.setAvailable(d.isConnected());
			}
			deviceAdapter.notifyDataSetChanged();
			Intent i = new Intent(this, BluetoothManagementService.class);
			i.setAction(ACTION_ASK_START_DISCOVERY);
			startService(i);
		} else {
			final Device d = ((Device) l.getItemAtPosition(position));
			if (d.isConnected()) {
				Log.i(TAG, "device already connected");
				Intent i = new Intent(getApplicationContext(), DashBoardActivity.class);
				i.putExtra(Device.EXTRA_DEVICE_NAME, d.getName());
				i.putExtra(Device.EXTRA_DEVICE_ADDRESS, d.getAddress());
				startActivity(i);
			} else {
				changeActivity = true;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("This device is currently not connected. Would you like connect it now?");
				builder.setCancelable(false);
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						connect(d);
					}
				});
				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				builder.create().show();
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_DISCOVERY_ID) {
			Log.d(TAG, "__CREATE_DIALOG__: " + id);
			ProgressDialog dialog = new ProgressDialog(this); 
			dialog.setMessage("scanning...");
			dialog.setIndeterminate(true);
			dialog.setButton(Dialog.BUTTON_NEGATIVE, "Abort", new OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface id) {
					Log.i(TAG, "User interrupt discovery");
					Intent i = new Intent(getApplicationContext(), BluetoothManagementService.class);
					i.setAction(ACTION_ASK_END_DISCOVERY);
					startService(i);
				}
			});
			return dialog;
		} else if (id == DIALOG_CONNECTION_ID) {
			Log.d(TAG, "__CREATE_DIALOG__: " + id);
			ProgressDialog dialog = new ProgressDialog(this); 
			dialog.setMessage("Connection...");
			dialog.setIndeterminate(true);
			return dialog;
		} else if (id == DIALOG_CONNECTION_CONFIRMATION) {
			Log.d(TAG, "__CREATE_DIALOG__: " + id);
			
		} 
		return null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (((AdapterContextMenuInfo) menuInfo).targetView.getId() != R.id.list_footer) {
			Device d = (Device) getListView().getItemAtPosition(((AdapterContextMenuInfo) menuInfo).position);
			Log.d(TAG, "Contextual menu - name: " + d.getName() + " address: " + d.getAddress());
			if (d.isConnected()) {
				menu.add(0, ITEM_DISCONNECT_ID, 0, "Disconnect");
			} else {
				menu.add(0, ITEM_CONNECT_ID, 0, "Connect");
			}
		}
	}
	
	private void connect(Device device) {
		showDialog(DIALOG_CONNECTION_ID, null);
		Intent i = new Intent(this, BluetoothManagementService.class);
		i.setAction(ACTION_ASK_CONNECT);
		i.putExtra(Device.EXTRA_DEVICE_NAME, device.getName());
		i.putExtra(Device.EXTRA_DEVICE_ADDRESS, device.getAddress());
		String connectMode = preferences.getString("Connection_list_preference", "10");
		int mode = Integer.parseInt(connectMode);
		i.putExtra(EXTRA_CONNECTION_MODE, mode);
		startService(i);
	}
	
	private void disconnect(Device device) {
		Log.i(TAG, "Asking for disconnect " + device.getName() + " (" + device.getAddress() + ")");
		Intent i = new Intent(this, BluetoothManagementService.class);
		i.setAction(ACTION_ASK_DISCONNECT);
		i.putExtra(Device.EXTRA_DEVICE_NAME, device.getName());
		i.putExtra(Device.EXTRA_DEVICE_ADDRESS, device.getAddress());
		startService(i);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Device device = ((Device) getListView().getItemAtPosition(info.position));
		switch (item.getItemId()) {
		case ITEM_CONNECT_ID:
			changeActivity = false;
			connect(device);
			return true;
		case ITEM_DISCONNECT_ID:
			disconnect(device);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.device_chooser_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BT:
				if (resultCode == RESULT_OK) { 
					Log.i(TAG, "Bluetooth succesfuly activated.");
					Intent i = new Intent(this, BluetoothManagementService.class);
					i.setAction(ACTION_ASK_BOUNDED_DEVICES);
					startService(i);
				} else if (resultCode == RESULT_CANCELED) { 
					Log.e(TAG, "Faillure during bluetooth activation.");
					Toast.makeText(getApplicationContext(), "Unable to start bluetooth", Toast.LENGTH_LONG).show();
					finish();
				}
				break;	
		}
	}
	
	@Override
	protected void onDestroy() {
		Log.d(TAG, "__ON_DESTROY__");
		Intent i = new Intent(this, BluetoothManagementService.class);
		i.setAction(ACTION_CLOSED);
		startService(i);
		unregisterReceiver(btManagerReceiver);
		super.onDestroy();
	}
	
	private final BroadcastReceiver btManagerReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothManagementService.ACTION_DISCOVERY_STARTED.equals(action)) {
				Log.d(TAG, "Receiver: ACTION_DISCOVERY_STARTED");
				showDialog(DIALOG_DISCOVERY_ID);
			} else if (BluetoothManagementService.ACTION_DISCOVERY_ENDED.equals(action)) {
				Log.d(TAG, "Receiver: ACTION_DISCOVERY_ENDED");
				dismissDialog(DIALOG_DISCOVERY_ID);
			} else if (BluetoothManagementService.ACTION_DEVICE_DETECTED.equals(action)) {
				Log.d(TAG, "Receiver: ACTION_DEVICE_DETECTED");
				deviceDetected(intent);
			} else if (BluetoothManagementService.ACTION_CONNECTION_SUCCESS.equals(action)) {
				Log.d(TAG, "Receiver: ACTION_CONNECTION_SUCCESS");
				dismissDialog(DIALOG_CONNECTION_ID);
				String address = intent.getExtras().getString(Device.EXTRA_DEVICE_ADDRESS);
				Device d = Device.getFromAddress(devices, address);
				deviceConnected(d);
			} else if (action.equals(BluetoothManagementService.ACTION_CONNECTION_FAILURE)) {
				Log.d(TAG, "Receiver: ACTION_CONNECTION_FAILURE");
				dismissDialog(DIALOG_CONNECTION_ID);
			} else if (BluetoothManagementService.ACTION_DEVICE_DISCONNECTED.equals(action)) {
				Log.d(TAG, "Receiver: ACTION_DEVICE_DISCONNECTED");
				String address = intent.getExtras().getString(Device.EXTRA_DEVICE_ADDRESS);
				Device d = Device.getFromAddress(devices, address);
				deviceDisconnected(d);
			}
		}
	};
	
	private void deviceDetected(Intent intent) {
		String name = intent.getExtras().getString(Device.EXTRA_DEVICE_NAME);
		String address = intent.getExtras().getString(Device.EXTRA_DEVICE_ADDRESS);
		if (!Device.contain(devices, address)) { 
			Device device = new Device(name, address);
			Log.d(TAG, "New device added: " + name + " (" + address + ")");
			devices.add(device);
		} 
		Device d = Device.getFromAddress(devices, address);
		d.setConnected(intent.getExtras().getBoolean(BluetoothManagementService.EXTRA_DEVICE_IS_CONNECTED, false));
		d.setAvailable(d.isConnected() || intent.getExtras().getBoolean(BluetoothManagementService.EXTRA_DEVICE_IS_AVAILABLE));
		Log.i(TAG, "Device: " + d);
		deviceAdapter.notifyDataSetChanged();
	}
	
	private void deviceConnected(Device device) {
		Log.i(TAG, "Device connected: " + device.getName() + " (" + device.getAddress() + ")");
		device.setConnected(true);
		device.setAvailable(true);
		deviceAdapter.notifyDataSetChanged();
		if (changeActivity) {
			changeActivity = false;
			Intent i = new Intent(getApplicationContext(), DashBoardActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_NAME, device.getName());
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, device.getAddress());
			startActivity(i);
		}
	}
	
	private void deviceDisconnected(Device device) {
		device.setConnected(false);
		device.setAvailable(false);
		deviceAdapter.notifyDataSetChanged();
	}
	
	private void registerReceivers() {
		registerReceiver(btManagerReceiver, new IntentFilter(BluetoothManagementService.ACTION_DISCOVERY_STARTED));
		registerReceiver(btManagerReceiver, new IntentFilter(BluetoothManagementService.ACTION_DISCOVERY_ENDED));
		registerReceiver(btManagerReceiver, new IntentFilter(BluetoothManagementService.ACTION_DEVICE_DETECTED));
		registerReceiver(btManagerReceiver, new IntentFilter(BluetoothManagementService.ACTION_CONNECTION_SUCCESS));
		registerReceiver(btManagerReceiver, new IntentFilter(BluetoothManagementService.ACTION_CONNECTION_FAILURE));
		registerReceiver(btManagerReceiver, new IntentFilter(BluetoothManagementService.ACTION_DEVICE_DISCONNECTED));
	}
}