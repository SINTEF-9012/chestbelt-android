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

public class AccelerometerActivity extends Activity implements ChestBeltServiceConnectionCallback {

	private final static String TAG = AccelerometerActivity.class.getSimpleName();
	
	private TextView tvSensorName;
	private GraphDetailsView graph1;
	private GraphDetailsView graph2;
	private GraphDetailsView graph3;
	private ChestBeltServiceConnection chestBeltConnection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "__ON_CREATE__");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_accgyro_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		tvSensorName.setText("Accelerometers");
		graph1 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph1);
		graph2 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph2);
		graph3 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph3);
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
		GraphWrapper wrapper1 = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferAccLateral());
		wrapper1.setGraphOptions(Color.YELLOW, 300, GraphBaseView.LINECHART, -300, 300, "Lateral");
		wrapper1.setPrinterParameters(true, false, false);
		graph1.registerWrapper(wrapper1);
		
		GraphWrapper wrapper2 = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferAccLongitudinal());
		wrapper2.setGraphOptions(Color.YELLOW, 300, GraphBaseView.LINECHART, -300, 300, "Longitudinal");
		wrapper2.setPrinterParameters(true, false, false);
		graph2.registerWrapper(wrapper2);
		
		GraphWrapper wrapper3 = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferAccVertical());
		wrapper3.setGraphOptions(Color.YELLOW, 300, GraphBaseView.LINECHART, 0, 600, "Vertical");
		wrapper3.setPrinterParameters(true, false, false);
		graph3.registerWrapper(wrapper3);
	}
}
