package org.thingml.chestbelt.android.chestbeltdroid.preferences;

import org.thingml.chestbelt.android.chestbeltdroid.R;
import org.thingml.chestbelt.driver.ChestBeltMode;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

public class ChestBeltPrefFragment extends PreferenceFragment {
	
	private ListPreference dataMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.pref_chestbelt_fragment);
		dataMode = (ListPreference) findPreference(getString(R.string.pref_datamode_key));
		
		// Can't set integers as preference list values so use strings. Careful to the order!
		String[] modeValues = new String[]{
				String.valueOf(ChestBeltMode.Extracted.getCode()),
				String.valueOf(ChestBeltMode.FullECG.getCode()),
				String.valueOf(ChestBeltMode.Raw.getCode()),
				String.valueOf(ChestBeltMode.RawAccelerometer.getCode()),
				String.valueOf(ChestBeltMode.RawGyroMode.getCode()),
				String.valueOf(ChestBeltMode.Test.getCode())};
		dataMode.setEntryValues(modeValues);
		dataMode.setDefaultValue(String.valueOf(ChestBeltMode.Extracted.getCode()));
	}
}
