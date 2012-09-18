package org.thingml.chestbelt.android.chestbeltdroid.graph;

public interface GraphBufferInterface {

	int[] getGraphData();
	boolean insertData(int data);
	int getInvalidNumber();
	void resetBuffer();
}
