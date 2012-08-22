package org.thingml.android.chestbelt.communication;

import java.util.ArrayList;

public class ChestBeltBufferizer {

	private ArrayList<Integer> values = new ArrayList<Integer>();
	private StringBuilder string = new StringBuilder();
	private long startTime;
	private long interval;
	private boolean ready = false;
	
	public ChestBeltBufferizer() {
	}
	
	public ChestBeltBufferizer(long startTime, long interval) {
		this.startTime = startTime;
		this.interval = interval;
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public ArrayList<Integer> getValues() {
		return values;
	}
	
	public void reset(long startTime, long interval) {
		this.startTime = startTime;
		this.interval = interval;
		ready = false;
		values.clear();
		string = new StringBuilder();
	}
	
	public void addMeasure(int value, long time) {
		values.add(value);
		string.append(value + ";");
		if (time >= startTime + interval) {
			ready = true;
		}
	}
	
	@Override
	public String toString() {
		return new String(string);
	}
}
