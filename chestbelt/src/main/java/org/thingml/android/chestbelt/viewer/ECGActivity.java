	package org.thingml.android.chestbelt.viewer;

import org.thingml.android.chestbelt.communication.BluetoothManagementService;
import org.thingml.android.chestbelt.communication.ChestBeltServiceConnection;
import org.thingml.android.chestbelt.communication.ChestBeltServiceConnection.ChestBeltServiceConnectionCallback;
import org.thingml.android.chestbelt.devices.Device;
import org.thingml.android.chestbelt.graph.GraphBaseView;
import org.thingml.android.chestbelt.graph.GraphDetailsView;
import org.thingml.android.chestbelt.graph.GraphWrapper;
import org.thingml.android.chestbelt.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ECGActivity extends Activity implements ChestBeltServiceConnectionCallback {

	private final static String TAG = ECGActivity.class.getSimpleName();
	
	private TextView tvSensorName;
	//private ImageView ivSensorIcon;
	private GraphDetailsView graph;
	private ChestBeltServiceConnection chestBeltConnection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "__ON_CREATE__");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		//ivSensorIcon = (ImageView) findViewById(R.id.iv_sensor_icon);
		tvSensorName.setText("ECG");
		graph =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph);
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
			chestBeltConnection.setBound(false);
		}
	}

	@Override
	public void serviceBound() {
		GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferECG());
		wrapper.setGraphOptions(Color.RED, 100, GraphBaseView.LINECHART, 0, 4096, "ECG");
		wrapper.setLineNumber(0);
		graph.registerWrapper(wrapper);
	}
}
