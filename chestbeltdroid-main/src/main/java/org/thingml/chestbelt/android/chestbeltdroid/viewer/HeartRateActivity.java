	package org.thingml.chestbelt.android.chestbeltdroid.viewer;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView.GraphListenner;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class HeartRateActivity extends VisualizationActivity implements GraphListenner {

	private final static String TAG = HeartRateActivity.class.getSimpleName();
	
	private TextView tvSensorName;
	private ImageView ivSensorIcon;
	private TextView tvSensorValue;
	private GraphDetailsView graph;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "__ON_CREATE__");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		ivSensorIcon = (ImageView) findViewById(R.id.iv_sensor_icon);
		tvSensorValue = (TextView) findViewById(R.id.tv_sensor_value);
		tvSensorName.setText("Heart rate");
		ivSensorIcon.setImageResource(R.drawable.ic_heartrate);
		graph =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph);
	}

	@Override
	protected void onResume() {
		super.onResume();
		graph.registerListenner(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		graph.unregisterListenner(this);
	}

	@Override
	public void onBindingReady() {
		GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferHeartrate());
		wrapper.setGraphOptions(Color.RED, 1000, GraphBaseView.BARCHART, 30, 160, "Heart rate");
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
