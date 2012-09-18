	package org.thingml.chestbelt.android.chestbeltdroid.viewer;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView.GraphListenner;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ActivityActivity extends VisualizationActivity implements GraphListenner {
	
	private TextView tvSensorName;
	private ImageView ivSensorIcon;
	private GraphDetailsView graph;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		tvSensorName.setText("Activity");
		ivSensorIcon = (ImageView) findViewById(R.id.iv_sensor_icon);
		ivSensorIcon.setImageResource(R.drawable.activity0);
		graph =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph);
	}

	@Override
	protected void onBindingReady() {
		GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferActivityLevel());
		wrapper.setGraphOptions(Color.GRAY, 500, GraphBaseView.BARCHART, 0, 3, "Activity");
		wrapper.setPrinterParameters(false, false, true);
		wrapper.setLineNumber(3);
		graph.registerWrapper(wrapper);		
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
	public void lastValueChanged(final int value) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				switch (value) {
				case 0: ivSensorIcon.setImageResource(R.drawable.activity0); break;
				case 1: ivSensorIcon.setImageResource(R.drawable.activity1); break;
				case 2: ivSensorIcon.setImageResource(R.drawable.activity2); break;
				case 3: ivSensorIcon.setImageResource(R.drawable.activity3); break;
				default: break;
				}
			}
		});
	}
}
