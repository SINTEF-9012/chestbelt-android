package org.thingml.chestbelt.android.chestbeltdroid.graph;

import android.graphics.Color;

public class GraphWrapper {
	
	private GraphBuffer graphBuffer;
	private int color = Color.RED;
	private long sleepTime = 1000;
	private int drawGraphType = GraphBaseView.LINECHART;
	private int lowestVisible = 0;
	private int highestVisible = 1023;
	private String name = new String();
	private boolean printName = false;
	private boolean printValue = false;
	private boolean printScale = false;
	private int lineNumber = 0;
	
	public GraphWrapper() {
		this.graphBuffer = new GraphBuffer();
	}
	
	public GraphWrapper(GraphBuffer graphBuffer) {
		this.graphBuffer = graphBuffer;
	}
	
	public GraphBuffer getBuffer() {
		return graphBuffer;
	}
	
	public void setBuffer(GraphBuffer gb) {
		this.graphBuffer = gb;
	}

	public void setGraphOptions(int color, long sleepTime, int drawGraphType, int lowestVisible, int highestVisible, String name) {
		this.color = color;
		this.sleepTime = sleepTime;
		this.drawGraphType = drawGraphType;
		this.lowestVisible = lowestVisible;
		this.highestVisible = highestVisible;
		this.name = name;
	}

	public void setPrinterParameters(boolean printName, boolean printValue, boolean printScale) {
		this.printName = printName;
		this.printValue = printValue;
		this.printScale = printScale;
	}
	
	public boolean printName() {
		return printName;
	}
	
	public boolean printScale() {
		return printScale;
	}
	
	public boolean printValue() {
		return printValue;
	}
	
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	public int getLineNumber() {
		return lineNumber;
	}
	
	public void setColor(int color) {
		this.color = color;
	}


	public int getColor() {
		return color;
	}


	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}


	public long getSleepTime() {
		return sleepTime;
	}


	public void setDrawGraphType(int drawGraphType) {
		this.drawGraphType = drawGraphType;
	}


	public int getDrawGraphType() {
		return drawGraphType;
	}


	public void setLowestVisible(int lowestVisible) {
		this.lowestVisible = lowestVisible;
	}


	public int getLowestVisible() {
		return lowestVisible;
	}


	public void setHighestVisible(int highestVisible) {
		this.highestVisible = highestVisible;
	}


	public int getHighestVisible() {
		return highestVisible;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String n){
		name = n;
	}
}
