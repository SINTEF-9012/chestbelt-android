package org.thingml.chestbelt.android.chestbeltdroid.communication;

import org.thingml.chestbelt.android.chestbeltdroid.communication.ChestBeltBinder.ChestBeltBinderCallback;
import org.thingml.chestbelt.driver.ChestBelt;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class ChestBeltServiceConnection implements ServiceConnection, ChestBeltBinderCallback {
	
	public interface ChestBeltServiceConnectionCallback {
		public void serviceBound();
		public void deviceConnected();
		public void deviceDisconnected();
	}
	
	ChestBeltBinder binder;
	private ChestBeltServiceConnectionCallback activity;
	private String address;
	private boolean bound = false;
	
	public ChestBeltServiceConnection(ChestBeltServiceConnectionCallback activity, String address) {
		Log.e("DEBUG", "new instance");
		this.activity = activity;
		this.address = address;
	}
	
	public ChestBeltGraphBufferizer getBufferizer() {
		return binder.getGraphBufferizers(address);
	}
	
	public ChestBelt getDriver() {
		return binder.getDriver(address);
	}
	
	public boolean isBound() {
		return bound;
	}
	
	public boolean isConnected() {
		if (binder != null) {
			return binder.isConnected(address);
		}
		return false;
	}
	
	public void setBound(boolean bound) {
		this.bound = bound;
	}
	
	public void stopListenning() {
		if (binder != null) {
			binder.removeListenner(this);
		}
	}
	
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        binder = (ChestBeltBinder) service;
        binder.addListenner(this);
        bound = true;
        activity.serviceBound();
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        bound = false;
    }

	@Override
	public void deviceConnected(String address) {
		if (this.address.equals(address)) {
			activity.deviceConnected();
		}
	}

	@Override
	public void deviceDisconnected(String address) {
		if (this.address.equals(address)) {
			activity.deviceDisconnected();
		}
	}
};