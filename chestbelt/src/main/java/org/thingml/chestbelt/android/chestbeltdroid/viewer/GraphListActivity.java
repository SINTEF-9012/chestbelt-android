	package org.thingml.chestbelt.android.chestbeltdroid.viewer;

import java.util.ArrayList;
import java.util.List;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.communication.BluetoothManagementService;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltServiceConnection;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltServiceConnection.ChestBeltServiceConnectionCallback;
import org.thingml.chestbelt.android.chestbeltdroid.devices.Device;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphAdapter;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;
import org.thingml.chestbelt.android.chestbeltdroid.preferences.PreferencesActivity;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class GraphListActivity extends ListActivity implements ChestBeltServiceConnectionCallback {
	
	private GraphAdapter adapter;
	private GraphWrapper wrapperHeartRate;
	private GraphWrapper wrapperTemperature;
	private GraphWrapper wrapperBattery;
	private GraphWrapper wrapperActivity;
	private GraphWrapper wrapperECGData;
	private GraphWrapper wrapperGyroPitch;
	private GraphWrapper wrapperGyroRoll;
	private GraphWrapper wrapperGyroYaw;
	private GraphWrapper wrapperAccLateral;
	private GraphWrapper wrapperAccLongitudinal;
	private GraphWrapper wrapperAccVertical;
	private List<GraphWrapper> wrappers = new ArrayList<GraphWrapper>();
	private ChestBeltServiceConnection chestBeltConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.graph_list);
		adapter = new GraphAdapter(this, wrappers);
		setListAdapter(adapter);
		getListView().setDividerHeight(5);
		chestBeltConnection = new ChestBeltServiceConnection(this, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = new Intent(this, BluetoothManagementService.class);
		bindService(intent, chestBeltConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (chestBeltConnection.isBound()) {
			unbindService(chestBeltConnection);
			wrappers.clear();
			chestBeltConnection.setBound(false);
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if ("Heart rate".equals(wrappers.get(position).getName())) {
			Intent i = new Intent(getApplicationContext(), HeartRateActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
		} else if ("Temperature".equals(wrappers.get(position).getName())) {
			Intent i = new Intent(getApplicationContext(), TemperatureActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
		} else if ("Battery".equals(wrappers.get(position).getName())) {
			Intent i = new Intent(getApplicationContext(), BatteryActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
		} else if ("Activity".equals(wrappers.get(position).getName())) {
			Intent i = new Intent(getApplicationContext(), ActivityActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
		} else if ("ECG".equals(wrappers.get(position).getName())) {
			Intent i = new Intent(getApplicationContext(), ECGActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
		} else if (wrappers.get(position).getName().startsWith("Gyro")) {
			Intent i = new Intent(getApplicationContext(), GyroActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
		} else if (wrappers.get(position).getName().startsWith("Acc")) {
			Intent i = new Intent(getApplicationContext(), AccelerometerActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.graphview_menu, menu);
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
	public void serviceBound() {
		wrapperHeartRate = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferHeartrate());
		wrapperHeartRate.setGraphOptions(Color.RED, 1000, GraphBaseView.BARCHART, 20, 180, "Heart rate");
		wrapperHeartRate.setPrinterParameters(true, true, true);
		
		wrapperBattery = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferBattery());
		wrapperBattery.setGraphOptions(Color.GREEN, 2000, GraphBaseView.BARCHART, 0, 100, "Battery");
		wrapperBattery.setPrinterParameters(true, true, true);
		
		wrapperTemperature = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferTemperature());
		wrapperTemperature.setGraphOptions(Color.BLUE, 2000, GraphBaseView.BARCHART, 20, 40, "Temperature");
		wrapperTemperature.setPrinterParameters(true, true, true);
		
		wrapperActivity = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferActivityLevel());
		wrapperActivity.setGraphOptions(Color.GRAY, 1000, GraphBaseView.BARCHART, 0, 3, "Activity");
		wrapperActivity.setPrinterParameters(true, false, true);
		wrapperActivity.setLineNumber(3);
		
		wrapperECGData = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferECG());
		wrapperECGData.setGraphOptions(Color.RED, 250, GraphBaseView.LINECHART, 0, 4096, "ECG");
		wrapperECGData.setPrinterParameters(true, false, false);
		
		wrapperGyroPitch = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferGyroPitch());
		wrapperGyroPitch.setGraphOptions(Color.WHITE, 500, GraphBaseView.LINECHART, -1024, 1024, "Gyro pitch");
		wrapperGyroPitch.setPrinterParameters(true, false, false);
		wrapperGyroRoll = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferGyroRoll());
		wrapperGyroRoll.setGraphOptions(Color.WHITE, 500, GraphBaseView.LINECHART, -1024, 1024, "Gyro roll");
		wrapperGyroRoll.setPrinterParameters(true, false, false);
		wrapperGyroYaw = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferGyroYaw());
		wrapperGyroYaw.setGraphOptions(Color.WHITE, 500, GraphBaseView.LINECHART, -1024, 1024, "Gyro yaw");
		wrapperGyroYaw.setPrinterParameters(true, false, false);
		
		wrapperAccLateral = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferAccLateral());
		wrapperAccLateral.setGraphOptions(Color.YELLOW, 500, GraphBaseView.LINECHART, -300, 300, "Acc lateral");
		wrapperAccLateral.setPrinterParameters(true, false, false);
		wrapperAccLongitudinal = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferAccLongitudinal());
		wrapperAccLongitudinal.setGraphOptions(Color.YELLOW, 500, GraphBaseView.LINECHART, -300, 300, "Acc longitudinal");
		wrapperAccLongitudinal.setPrinterParameters(true, false, false);
		wrapperAccVertical = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferAccVertical());
		wrapperAccVertical.setGraphOptions(Color.YELLOW, 500, GraphBaseView.LINECHART, 0, 600, "Acc vertical");
		wrapperAccVertical.setPrinterParameters(true, false, false);
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				wrappers.add(wrapperHeartRate);
				wrappers.add(wrapperBattery);
				wrappers.add(wrapperTemperature);
				wrappers.add(wrapperActivity);
				wrappers.add(wrapperECGData);
				wrappers.add(wrapperGyroPitch);
				wrappers.add(wrapperGyroRoll);
				wrappers.add(wrapperGyroYaw);
				wrappers.add(wrapperAccLateral);
				wrappers.add(wrapperAccLongitudinal);
				wrappers.add(wrapperAccVertical);
			}
		});
	}
}