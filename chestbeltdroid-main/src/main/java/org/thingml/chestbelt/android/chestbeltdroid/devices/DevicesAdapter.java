package org.thingml.chestbelt.android.chestbeltdroid.devices;

import java.util.ArrayList;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Define an adapter to match Device objects with a ListView.
 * @author Fabien Fleurey
 */

public class DevicesAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private ArrayList<Device> devices;
	
	private static class ViewHolder {
		TextView nameTv;
		ImageView availablePic;
		TextView addressTv;
		ImageView statusPic;
	}
	
	public DevicesAdapter(Context context, ArrayList<Device> devices) {
		inflater = LayoutInflater.from(context);
		this.devices = devices;
	}
	
	public int getCount() {
		return devices.size();
	}

	public Device getItem(int position) {
		return devices.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		View view = convertView;
		if(view == null) {
			holder = new ViewHolder();
			view = inflater.inflate(R.layout.device_row, null);
			holder.nameTv = (TextView) view.findViewById(R.id.list_device_name);
			holder.availablePic = (ImageView) view.findViewById(R.id.list_available_image);
			holder.addressTv = (TextView) view.findViewById(R.id.list_device_address);
			holder.statusPic = (ImageView) view.findViewById(R.id.list_status_image);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		holder.nameTv.setText(devices.get(position).getName());
		holder.addressTv.setText(devices.get(position).getAddress());
		if (devices.get(position).isConnected()) {
			holder.statusPic.setImageResource(R.drawable.ic_connected);
		} else {
			holder.statusPic.setImageResource(R.drawable.ic_disconnected);
		}
		if (devices.get(position).isAvailable()) {
			holder.availablePic.setImageResource(R.drawable.ic_available); 
		} else {
			holder.availablePic.setImageResource(android.R.color.transparent);
		}
		return view;
	}

}
