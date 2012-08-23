package org.thingml.chestbelt.android.chestbeltdroid.communication;

import org.thingml.chestbelt.android.chestbeltdroid.communication.BluetoothManagementService.ChestBeltBinder;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ChestBeltServiceConnection implements ServiceConnection {
	
	public interface ChestBeltServiceConnectionCallback {
		public void serviceBound();
	}
	
	private ChestBeltServiceConnectionCallback activity;
	private String address;
	private ChestBeltGraphBufferizer bufferizer;
	private ChestBeltDriver driver;
	private boolean bound = false;
	
	public ChestBeltServiceConnection(ChestBeltServiceConnectionCallback activity, String address) {
		this.activity = activity;
		this.address = address;
	}
	
	public ChestBeltGraphBufferizer getBufferizer() {
		return bufferizer;
	}
	
	public ChestBeltDriver getDriver() {
		return driver;
	}
	
	public boolean isBound() {
		return bound;
	}
	
	public void setBound(boolean bound) {
		this.bound = bound;
	}
	
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        ChestBeltBinder binder = (ChestBeltBinder) service;
        bufferizer = binder.getGraphBufferizers(address);
        driver = binder.getDriver(address);
        activity.serviceBound();
        bound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        bound = false;
    }
};