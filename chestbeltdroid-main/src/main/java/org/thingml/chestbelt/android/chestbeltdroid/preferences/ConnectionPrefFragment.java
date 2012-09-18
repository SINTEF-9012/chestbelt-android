package org.thingml.chestbelt.android.chestbeltdroid.preferences;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

public class ConnectionPrefFragment extends PreferenceFragment {
	
	private EditTextPreference duration;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.pref_connection_fragment);
		CheckBoxPreference autoReconnect = (CheckBoxPreference) findPreference(getString(R.string.pref_autoreconnect_key));
		duration = (EditTextPreference) findPreference(getString(R.string.pref_autoreconnect_duration_key));
		duration.setDependency(autoReconnect.getKey());
		duration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				duration.setSummary(newValue + " minutes");
				return true;
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();
		duration.setSummary(duration.getText() + " minutes");
	}
}
