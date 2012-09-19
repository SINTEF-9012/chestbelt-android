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
import android.util.Log;
import android.widget.TextView;

public class AccelerometerActivity extends VisualizationActivity {

	private final static String TAG = AccelerometerActivity.class.getSimpleName();
	
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
		tvSensorName.setText("Accelerometers");
		graph1 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph1);
		graph2 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph2);
		graph3 =  (GraphDetailsView) findViewById(R.id.gv_sensor_graph3);
	}

	@Override
	protected void onBindingReady() {
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
