package org.thingml.android.chestbelt.preferences;

import org.thingml.android.chestbelt.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class ChestBeltPrefFragment extends PreferenceFragment {
	
	public static final int DATAMODE_EXTRACTED = 10;
	public static final int DATAMODE_FULLECG = 20;
	public static final int DATAMODE_RAW = 30;
	public static final int DATAMODE_RAWACCELEROMETER = 40;
	public static final int DATAMODE_RAWGYROMODE = 50;
	public static final int DATAMODE_TEST = 60;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); 
		addPreferencesFromResource(R.xml.pref_chestbelt_fragment);
	}
	
	public static String resolveMode(int mode) {
		switch (mode) {
		case DATAMODE_EXTRACTED: return "Extracted";
		case DATAMODE_FULLECG: return "FullECG";
		case DATAMODE_RAW: return "Raw";
		case DATAMODE_RAWACCELEROMETER: return "RawAccelerometer";
		case DATAMODE_RAWGYROMODE: return "RawGyroscope";
		case DATAMODE_TEST: return "Test";
		default: return "Unknown";
		}
	}
}
