package org.thingml.chestbelt.android.chestbeltdroid.graph;


public class GraphBuffer implements GraphBufferInterface {

	private int[] graphData;
	private int size;
	private boolean empty = true;
	private int lastValue = 0;
	private int notValidNumber = Integer.MIN_VALUE;
	private int counter = 0;
	
	public GraphBuffer(){
		size = 100;
		graphData = new int[size];
		initializeArray(notValidNumber);
	}
	
	public GraphBuffer(int customSize) {
		size = customSize;
		graphData = new int[size];
		initializeArray(notValidNumber);
	}
	
	public GraphBuffer(int customSize, int inValidNumber) {
		size = customSize;
		graphData = new int[size];
		initializeArray(inValidNumber);
	}

	public int getLastValue() {
		return lastValue;
	}
	
	private void initializeArray(int inValidNumber) {
		for (int i = 0 ; i < graphData.length ; i++) {
			graphData[i] = inValidNumber;
		}
		empty = true;
	}
	
	@Override
	public synchronized int[] getGraphData() {
		int[] result = graphData.clone();
		return result;
	}

	@Override
	public synchronized boolean insertData(int data) {
		if (data == notValidNumber) {
			return false;
		}
		lastValue = data;
		empty = false;
		if (counter >= size) {
			for (int i = 1 ; i < graphData.length ; i++) {
				graphData[i-1] = graphData[i];
			}
			graphData[size-1] = data;
			counter++;
			return true;
		} else {
			for (int i = 0 ; i < graphData.length ; i++){
				if (graphData[i] == notValidNumber) {
					graphData[i] = data;
					counter ++;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int getInvalidNumber() {
		return notValidNumber;
	}

	public boolean isEmpty() {
		return empty;
	}
	
	public void setArray(int[] intArray) {
		graphData = intArray;
		size = intArray.length;
	}
	
	@Override
	public void resetBuffer(){
		graphData = new int[size];
		initializeArray(notValidNumber);
		lastValue = 0;
	}
	
}
