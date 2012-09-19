package org.thingml.chestbelt.android.chestbeltdroid.graph;

import java.util.List;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class GraphAdapter extends BaseAdapter {

	private LayoutInflater inflater = null;
	private List<GraphWrapper> wrappers;
	
	private static class ViewHolder {
		GraphDetailsView graph;
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
		View view = convertView;
		if(view == null) {
			holder = new ViewHolder();
			view = inflater.inflate(R.layout.graph_row, parent, false);
			holder.graph = (GraphDetailsView) view.findViewById(R.id.gv_graphrow_graph);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.graph.registerWrapper(wrappers.get(position));
		return view;
	}

}
