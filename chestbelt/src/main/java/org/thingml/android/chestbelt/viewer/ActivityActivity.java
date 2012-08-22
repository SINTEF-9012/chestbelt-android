	package org.thingml.android.chestbelt.viewer;

import org.thingml.android.chestbelt.communication.BluetoothManagementService;
import org.thingml.android.chestbelt.communication.ChestBeltServiceConnection;
import org.thingml.android.chestbelt.communication.ChestBeltServiceConnection.ChestBeltServiceConnectionCallback;
import org.thingml.android.chestbelt.devices.Device;
import org.thingml.android.chestbelt.graph.GraphBaseView;
import org.thingml.android.chestbelt.graph.GraphDetailsView;
import org.thingml.android.chestbelt.graph.GraphWrapper;
import org.thingml.android.chestbelt.graph.GraphBaseView.GraphListenner;
import org.thingml.android.chestbelt.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityActivity extends Activity implements ChestBeltServiceConnectionCallback, GraphListenner {
	
	private TextView tvSensorName;
	private ImageView ivSensorIcon;
	private GraphDetailsView graph;
	private ChestBeltServiceConnection chestBeltConnection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		tvSensorName.setText("Activity");
		ivSensorIcon = (ImageView) findViewById(R.id.iv_sensor_icon);
		ivSensorIcon.setImageResource(R.drawable.activity0);
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
		GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferActivityLevel());
		wrapper.setGraphOptions(Color.GRAY, 500, GraphBaseView.BARCHART, 0, 3, "Activity");
		wrapper.setPrinterParameters(false, false, true);
		wrapper.setLineNumber(3);
		graph.registerWrapper(wrapper);
	}

	@Override
	public void lastValueChanged(int value) {
		switch (value) {
		case 0:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ivSensorIcon.setImageResource(R.drawable.activity0);
				}
			});
			break;
		case 1:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ivSensorIcon.setImageResource(R.drawable.activity1);
				}
			});
			break;
		case 2:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ivSensorIcon.setImageResource(R.drawable.activity2);
				}
			});
			break;
		case 3:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ivSensorIcon.setImageResource(R.drawable.activity3);
				}
			});
			break;
		default:
			break;
		}
	}
}
