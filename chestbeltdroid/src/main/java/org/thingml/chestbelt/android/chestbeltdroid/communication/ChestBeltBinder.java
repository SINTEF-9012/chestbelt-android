package org.thingml.chestbelt.android.chestbeltdroid.communication;

import java.util.ArrayList;
import java.util.Hashtable;

import org.thingml.chestbelt.driver.ChestBelt;

import android.os.Binder;

public class ChestBeltBinder extends Binder {
	
	public interface ChestBeltBinderCallback {
		public void deviceConnected(String address);
		public void deviceDisconnected(String address);
	}
	
	private Hashtable<String, ChestBelt> runningSessions;
	private Hashtable<String, ChestBeltGraphBufferizer> graphBufferizers;
	private ArrayList<ChestBeltBinderCallback> listenners = new ArrayList<ChestBeltBinder.ChestBeltBinderCallback>();
	
	public ChestBeltBinder(Hashtable<String, ChestBelt> runningSessions, Hashtable<String, ChestBeltGraphBufferizer> graphBufferizers) {
		this.runningSessions = runningSessions;
		this.graphBufferizers = graphBufferizers;
	}
	
	public boolean isConnected(String address) {
		return runningSessions.containsKey(address);
	}
	public ChestBelt getDriver(String address) {
		return runningSessions.get(address);
	}
    public ChestBeltGraphBufferizer getGraphBufferizers(String address) {
    	return graphBufferizers.get(address);
    }
    
    public void addListenner(ChestBeltBinderCallback listenner) {
    	listenners.add(listenner);
    }
    public void removeListenner(ChestBeltBinderCallback listenner) {
    	listenners.remove(listenner);
    }
    
    public void deviceConnected(String address) {
    	for (ChestBeltBinderCallback listenner : listenners) {
    		listenner.deviceConnected(address);
    	}
    }
    public void deviceDisconnected(String address) {
    	for (ChestBeltBinderCallback listenner : listenners) {
    		listenner.deviceDisconnected(address);
    	}
    }
}
