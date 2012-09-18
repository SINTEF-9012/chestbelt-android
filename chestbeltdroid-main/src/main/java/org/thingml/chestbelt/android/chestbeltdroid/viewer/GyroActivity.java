	package org.thingml.chestbelt.android.chestbeltdroid.viewer;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class GyroActivity extends VisualizationActivity {

	private final static String TAG = GyroActivity.class.getSimpleName();
	
	private TextView tvSensorName;
	private GraphDetailsView graph1;
	private GraphDetailsView graph2;
	private GraphDetailsView graph3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "__ON_CREATE__");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_accgyro_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		tvSensorName.setText("Gyroscopes");
		graph1 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph1);
		graph2 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph2);
		graph3 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph3);
	}

	@Override
	public void onBindingReady() {
		GraphWrapper wrapper1 = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferGyroPitch());
		wrapper1.setGraphOptions(Color.WHITE, 300, GraphBaseView.LINECHART, -1500, 1500, "Pitch");
		wrapper1.setPrinterParameters(true, false, false);
		graph1.registerWrapper(wrapper1);
		
		GraphWrapper wrapper2 = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferGyroRoll());
		wrapper2.setGraphOptions(Color.WHITE, 300, GraphBaseView.LINECHART, -1500, 1500, "Roll");
		wrapper2.setPrinterParameters(true, false, false);
		graph2.registerWrapper(wrapper2);
		
		GraphWrapper wrapper3 = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferGyroYaw());
		wrapper3.setGraphOptions(Color.WHITE, 300, GraphBaseView.LINECHART, -1500, 1500, "Yaw");
		wrapper3.setPrinterParameters(true, false, false);
		graph3.registerWrapper(wrapper3);
	}
}
