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
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class ECGActivity extends VisualizationActivity {
	
	private TextView tvSensorName;
	private GraphDetailsView graph;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_view);
		tvSensorName = (TextView) findViewById(R.id.tv_sensor_name);
		tvSensorName.setText("ECG");
		graph =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph);
	}

	@Override
	public void onBindingReady() {
		GraphWrapper wrapper = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferECG());
		wrapper.setGraphOptions(Color.RED, 100, GraphBaseView.LINECHART, 0, 4096, "ECG");
		wrapper.setLineNumber(0);
		graph.registerWrapper(wrapper);
	}
}
