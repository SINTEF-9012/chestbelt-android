package org.thingml.chestbelt.android.chestbeltdroid.preferences;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SensAppPrefFragment extends PreferenceFragment {
	
	private Preference server;
	private Preference port;
	private Preference dataStorage;
	private CheckBoxPreference storeECG;
	private SharedPreferences prefs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_sensapp_fragment);
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		server = findPreference(getString(R.string.pref_sensor_server));
		port = findPreference(getString(R.string.pref_sensor_port));
		dataStorage = findPreference(getString(R.string.pref_data_storage));
		storeECG = (CheckBoxPreference) findPreference(getString(R.string.pref_ecg_storage));
		storeECG.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				if ((Boolean) newValue) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage("Warning: due to the huge amount of ECG data, this option leads to a significant charge on the database, it is discouraged to let this mode permanently enabled.");
					builder.setCancelable(false);
					builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					builder.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							prefs.edit().putBoolean(storeECG.getKey(), false).commit();
							storeECG.setChecked(false);
						}
					});
					builder.create().show();
				}
				return true;
			}
		});
		server.setDependency(dataStorage.getKey());
		port.setDependency(dataStorage.getKey());
		storeECG.setDependency(dataStorage.getKey());
	}

	SharedPreferences.OnSharedPreferenceChangeListener spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {	
			if (key.equals(server.getKey())) {
				String value = sharedPreferences.getString(getString(R.string.pref_sensor_server), "Set the server address");
				server.setSummary(value);
			} else if (key.equals(port.getKey())) {
				String value = sharedPreferences.getString(getString(R.string.pref_sensor_port), "Set the server port");
				port.setSummary(value);
			} else if (key.equals(dataStorage.getKey())) {
				if (prefs.getBoolean(key, false)) {
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
								  Toast.makeText(getActivity(), "Couldn't launch the market.", Toast.LENGTH_LONG).show();
								}
							}
						});
						builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
						builder.create().show();
						sharedPreferences.edit().putBoolean(dataStorage.getKey(), false).commit();
						((CheckBoxPreference) dataStorage).setChecked(false);
					}
				}
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		String value = prefs.getString(getString(R.string.pref_sensor_server), "Set the server address");
		server.setSummary(value);
		value = prefs.getString(getString(R.string.pref_sensor_port), "Set the server port");
		port.setSummary(value);
		prefs.registerOnSharedPreferenceChangeListener(spChanged);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(spChanged);
	}
}