package org.thingml.android.chestbelt.graph;

import java.util.List;

import org.thingml.android.chestbelt.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class GraphAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private List<GraphWrapper> wrappers;
	
	private class ViewHolder {
		GraphDetailsView graph;
		//TextView tv;
	}
	
	public GraphAdapter(Context context, List<GraphWrapper> wrappers) {
		this.inflater = LayoutInflater.from(context);
		this.wrappers = wrappers;
	}
	
	@Override
	public int getCount() {
		return wrappers.size();
	}

	@Override
	public Object getItem(int position) {
		return wrappers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.graph_row, parent, false);
			//holder.tv = (TextView) convertView.findViewById(R.id.tv_graphrow_name);
			holder.graph = (GraphDetailsView) convertView.findViewById(R.id.gv_graphrow_graph);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		//holder.tv.setText(wrappers.get(position).getName());
		holder.graph.registerWrapper(wrappers.get(position));
		return convertView;
	}

}
