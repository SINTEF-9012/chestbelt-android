package org.thingml.chestbelt.android.chestbeltdroid.preferences;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class ConnectionPrefFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.pref_connection_fragment);
	}
}
