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
package org.thingml.chestbelt.android.chestbeltdroid.preferences;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.widget.Toast;

public class SensAppPrefFragment extends PreferenceFragment {
	
	private CheckBoxPreference dataStorage;
	private CheckBoxPreference storeECG;
	
	private OnPreferenceChangeListener warnHugeData = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if ((Boolean) newValue) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage("Warning: due to the huge amount of data, this option leads to a significant charge on the database.");
				builder.setCancelable(false);
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						storeECG.setChecked(true);
					}
				});
				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
				builder.create().show();
				return false;
			} else {
				return true;	
			}
		}
	};
	
	private OnPreferenceChangeListener checkSensAppInstall = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			if ((Boolean) newValue) {
				try{
					getActivity().getPackageManager().getApplicationInfo("org.sensapp.android.sensappdroid", 0);
				} catch (PackageManager.NameNotFoundException e ){
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage("SensApp application is required to enable this feature. Would you like install it now?");
					builder.setCancelable(false);
					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Uri uri = Uri.parse("market://details?id=" + "org.sensapp.android.sensappdroid");
							try {
								startActivity(new Intent(Intent.ACTION_VIEW, uri));
							} catch (ActivityNotFoundException e1) {
								Toast.makeText(getActivity(), "Error: Not able to launch the PlayStore", Toast.LENGTH_LONG).show();
							}
						}
					});
					builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					builder.create().show();
					return false;
				}
			}
			return true;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_sensapp_fragment);
		dataStorage = (CheckBoxPreference) findPreference(getString(R.string.pref_data_storage));
		dataStorage.setOnPreferenceChangeListener(checkSensAppInstall);
		storeECG = (CheckBoxPreference) findPreference(getString(R.string.pref_ecg_storage));
		storeECG.setOnPreferenceChangeListener(warnHugeData);
		storeECG.setDependency(dataStorage.getKey());
		CheckBoxPreference storeIMU = (CheckBoxPreference) findPreference(getString(R.string.pref_imu_storage));
		storeIMU.setDependency(dataStorage.getKey());
	}
}