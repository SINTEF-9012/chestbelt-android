	package org.thingml.chestbelt.android.chestbeltdroid.viewer;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.communication.BluetoothManagementService;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltServiceConnection;
import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltServiceConnection.ChestBeltServiceConnectionCallback;
import org.thingml.chestbelt.android.chestbeltdroid.devices.Device;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView.GraphListenner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class BatteryActivity extends Activity implements ChestBeltServiceConnectionCallback, GraphListenner {
	
	private TextView tvSensorName;
	//private ImageView ivSensorIcon;
	private TextView tvSensorValue;
	private GraphDetailsView graph;
	private ChestBeltServiceConnection chestBeltConnection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		//ivSensorIcon = (ImageView) findViewById(R.id.iv_sensor_icon);
		tvSensorValue = (TextView) findViewById(R.id.tv_sensor_value);
		tvSensorName.setText("Battery");
		//ivSensorIcon.setImageResource(R.drawable.ic_heartrate);
		graph =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph);
		chestBeltConnection = new ChestBeltServiceConnection(this, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
	}

	@Override
	protected void onStart() {
		super.onStart();
		graph.registerListenner(this);
		Intent intent = new Intent(this, BluetoothManagementService.class);
		bindService(intent, chestBeltConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		graph.unregisterListenner(this);
		if (chestBeltConnection.isBound()) {
			unbindService(chestBeltConnection);
			chestBeltConnection.setBound(false);
		}
	}

	@Override
	public void serviceBound() {
		GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferBattery());
		wrapper.setGraphOptions(Color.GREEN, 3000, GraphBaseView.BARCHART, 0, 100, "Battery");
		wrapper.setPrinterParameters(false, false, true);
		graph.registerWrapper(wrapper);
	}

	@Override
	public void lastValueChanged(final int value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tvSensorValue.setText(String.valueOf(value));	
			}
		});
	}
}
