/**
 * Copyright (C) 2012 SINTEF <fabien@fleurey.com>
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
	package org.thingml.chestbelt.android.chestbeltdroid.viewer;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView.GraphListenner;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class BatteryActivity extends VisualizationActivity implements GraphListenner {
	
	private TextView tvSensorName;
	private TextView tvSensorValue;
	private GraphDetailsView graph;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		tvSensorValue = (TextView) findViewById(R.id.tv_sensor_value);
		tvSensorName.setText("Battery");
		graph =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph);
	}

	@Override
	protected void onResume() {
		super.onStart();
		graph.registerListenner(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		graph.unregisterListenner(this);
	}
	
	@Override
	public void onBindingReady() {
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
