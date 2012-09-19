package org.thingml.chestbelt.android.chestbeltdroid.devices;

import java.util.ArrayList;

/**
 * Define the object model for one bluetooth device.
 * @author Fabien Fleurey
 */

public class Device {
	
	public static final String EXTRA_DEVICE_NAME = Device.class.getName() + ".EXTRA_DEVICE_NAME";
	public static final String EXTRA_DEVICE_ADDRESS = Device.class.getName() + ".EXTRA_DEVICE_ADDRESS";
		
	private String name = null;
	private String address = null;
	private boolean connected = false;
	private boolean available = false;
	
	public Device(String name, String adress) {
		this.name = new String(name);
		this.address = new String(adress);
	}
	
	public Device(String name, String address, boolean connected, boolean available) {
		this.name = new String(name);
		this.address = new String(address);
		this.connected = connected;
		this.available = available;
	}
	
	public String getName() {
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setConnected(boolean status) {
		connected = status;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	@Override
	public String toString() {
		return "name: " + name + " (" + address + ") - Connected: " + connected + " - Available: " + available; 
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Device) {
			return address.equals(((Device) o).getAddress()); 
		}
		return false;
	}
	
	public static boolean contain(ArrayList<Device> devices, String address) {
		for (Device d : devices) {
			if (d.getAddress().equals(address)) {
				return true;
			}
		}
		return false;
	}
	
	public static Device getFromAddress(ArrayList<Device> devices, String address) {
		for (Device d : devices) {
			if (d.getAddress().equals(address)) {
				return d;
			}
		}
		return null;
	}
}
