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
import org.thingml.chestbelt.android.chestbeltdroid.devices.Device;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphBaseView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphDetailsView;
import org.thingml.chestbelt.android.chestbeltdroid.graph.GraphWrapper;
import org.thingml.chestbelt.android.chestbeltdroid.preferences.PreferencesActivity;
import org.thingml.chestbelt.driver.ChestBeltListener;
import org.thingml.chestbelt.driver.ChestBeltMode;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DashBoardActivity extends VisualizationActivity implements ChestBeltListener {
	
	private GraphWrapper wrapperECGData;

	private String deviceName;
	private String deviceAddress;
	
	private TextView battery;
	private TextView heartrate;
	private TextView temperature;
	private ImageView activity;
	private ImageView position;
	private ProgressBar gyroPitch;
	private ProgressBar gyroRoll;
	private ProgressBar gyroYaw;
	private ProgressBar accLateral;
	private ProgressBar accLongitudinal;
	private ProgressBar accVertical;

	public class ClickListenner implements OnClickListener {
		private Class<?> activity;
		public ClickListenner(Class<?> activity) {
			this.activity = activity;
		}
		
		@Override
		public void onClick(View v) {
			Intent i = new Intent(DashBoardActivity.this, activity);
			i.putExtra(Device.EXTRA_DEVICE_NAME, deviceName);
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, deviceAddress);
			startActivity(i);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		deviceName = getIntent().getExtras().getString(Device.EXTRA_DEVICE_NAME);
		deviceAddress = getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS);
		setTitle(deviceName + " Dashboard");
		setContentView(R.layout.dashboard);
		battery = (TextView) findViewById(R.id.tv_battery);
		battery.setOnClickListener(new ClickListenner(BatteryActivity.class));
		heartrate = (TextView) findViewById(R.id.tv_heartrate);
		heartrate.setOnClickListener(new ClickListenner(HeartRateActivity.class));
		temperature = (TextView) findViewById(R.id.tv_temperature);
		temperature.setOnClickListener(new ClickListenner(TemperatureActivity.class));
		activity = (ImageView) findViewById(R.id.iv_activity);
		activity.setOnClickListener(new ClickListenner(ActivityActivity.class));
		position = (ImageView) findViewById(R.id.iv_position);
		findViewById(R.id.gyro_layout).setOnClickListener(new ClickListenner(GyroActivity.class));
		gyroPitch = (ProgressBar) findViewById(R.id.pb_gyro_pitch);
		gyroRoll = (ProgressBar) findViewById(R.id.pb_gyro_roll);
		gyroYaw = (ProgressBar) findViewById(R.id.pb_gyro_yaw);
		findViewById(R.id.acc_layout).setOnClickListener(new ClickListenner(AccelerometerActivity.class));
		accLateral = (ProgressBar) findViewById(R.id.pb_acc_lateral);
		accLongitudinal = (ProgressBar) findViewById(R.id.pb_acc_longitudinal);
		accVertical = (ProgressBar) findViewById(R.id.pb_acc_vertical);
	}
	
	@Override
	protected void onResume() {
		((TextView) findViewById(R.id.tv_mode)).setText(ChestBeltMode.fromCode(Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_datamode_key), String.valueOf(ChestBeltMode.Extracted.getCode())))) + " mode");
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.dashboard_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			startActivity(new Intent(this, PreferencesActivity.class));
			return true;
		case R.id.graph_view:
			Intent i = new Intent(this, GraphListActivity.class);
			i.putExtra(Device.EXTRA_DEVICE_NAME, getIntent().getExtras().getString(Device.EXTRA_DEVICE_NAME));
			i.putExtra(Device.EXTRA_DEVICE_ADDRESS, getIntent().getExtras().getString(Device.EXTRA_DEVICE_ADDRESS));
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		removeChestBeltListenner(this);
	}
	
	@Override
	protected void onBindingReady() {
		wrapperECGData = new GraphWrapper(chestBeltConnection.getBufferizer().getBufferECG());
		wrapperECGData.setGraphOptions(Color.RED, 250, GraphBaseView.LINECHART, 0, 4096, "ECG");
		wrapperECGData.setPrinterParameters(true, false, false);
		GraphDetailsView graphECG = ((GraphDetailsView) findViewById(R.id.gv_ecg));
		graphECG.registerWrapper(wrapperECGData);
		graphECG.setOnClickListener(new ClickListenner(ECGActivity.class));
		if (chestBeltConnection.getBufferizer().getBufferBattery().isEmpty()) {
			battery.setText("-- %");
		} else {
			int value = chestBeltConnection.getBufferizer().getBufferBattery().getLastValue();
			battery.setText(String.valueOf(value) + " %");
			if (value <= 15) {
				battery.setTextColor(Color.RED);
			} else if (15 < value && value < 50) {
				battery.setTextColor(Color.YELLOW);
			} else {
				battery.setTextColor(Color.GREEN);
			}
		}
		if (chestBeltConnection.getBufferizer().getBufferTemperature().isEmpty()) {
			temperature.setText("-- °C");
		} else {
			temperature.setText(chestBeltConnection.getBufferizer().getBufferTemperature().getLastValue() + " °C");
		}
		chestBeltConnection.getDriver().addChestBeltListener(this);
	}

	private void updateActivity(int value) {
		switch (value) {
		case 0: activity.setImageResource(R.drawable.activity0); break;
		case 1: activity.setImageResource(R.drawable.activity1); break;
		case 2: activity.setImageResource(R.drawable.activity2); break;
		case 3: activity.setImageResource(R.drawable.activity3); break;
		default: break;
		}
	}
	
	@Override
	public void cUSerialNumber(long value, int timestamp) {
		// TODO Auto-generated method stub
	}

	@Override
	public void batteryStatus(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				battery.setText(value + " %");	
				if (value <= 15) {
					battery.setTextColor(Color.RED);
				} else if (15 < value && value < 50) {
					battery.setTextColor(Color.YELLOW);
				} else {
					battery.setTextColor(Color.GREEN);
				}
			}
		});
	}

	@Override
	public void indication(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (value >= 10 && value <= 13) {
					updateActivity(value-10);
				} else if (value >= 1 && value <= 6) {
					switch (value) {
					case 1: 
						position.setImageResource(R.drawable.pos_upright);
						break;
					case 2: 
						position.setImageResource(R.drawable.pos_prone);
						break;
					case 3: 
						position.setImageResource(R.drawable.pos_supine);
						break;
					case 4: 
						position.setImageResource(R.drawable.pos_side);
						break;
					case 5: 
						position.setImageResource(R.drawable.pos_inverted);
						break;
					case 6: 
						position.setImageResource(android.R.id.empty);
						break;
					default:
						break; 
					}
				} 
			}
		});
	}

	@Override
	public void status(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageOverrun(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void heartRate(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					heartrate.setText((float) value / 10 + "\nbpm");
				} else {
					heartrate.setText(String.valueOf((float) value / 10));
				}
			}
		});
	}

	@Override
	public void heartRateConfidence(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eCGData(int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eCGSignalQuality(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eCGRaw(int value, int timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gyroPitch(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gyroPitch.setProgress(value + 2500);
			}
		});
	}

	@Override
	public void gyroRoll(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gyroRoll.setProgress(value + 2500);
			}
		});
	}

	@Override
	public void gyroYaw(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gyroYaw.setProgress(value + 2500);
			}
		});
	}

	@Override
	public void accLateral(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				accLateral.setProgress(value + 500);
			}
		});
	}

	@Override
	public void accLongitudinal(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				accLongitudinal.setProgress(value + 500);
			}
		});
	}

	@Override
	public void accVertical(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				accVertical.setProgress(value + 500);
			}
		});
	}

	@Override
	public void rawActivityLevel(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateActivity(value-10);
			}
		});
	}

	@Override
	public void combinedIMU(final int ax, final int ay, final int az, final int gx, final int gy, final int gz, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				gyroPitch.setProgress(gy + 2500);
				gyroRoll.setProgress(gx + 2500);
				gyroYaw.setProgress(gz + 2500);
				accLateral.setProgress(ay + 500);
				accLongitudinal.setProgress(az + 500);
				accVertical.setProgress(ax + 500);
			}
		});
	}

	@Override
	public void skinTemperature(final int value, int timestamp) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				temperature.setText((float) value / 10 + " °C");	
			}
		});
	}

	@Override
	public void cUFWRevision(String arg0, int arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void fullClockTimeSync(long arg0, boolean arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void referenceClockTime(long arg0, boolean arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void connectionLost() {
		// TODO Auto-generated method stub
	}
}