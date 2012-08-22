package org.thingml.android.chestbelt.graph;

public interface GraphBufferInterface {

	int[] getGraphData();
	boolean insertData(int data);
	int getInvalidNumber();
	void resetBuffer();
}
