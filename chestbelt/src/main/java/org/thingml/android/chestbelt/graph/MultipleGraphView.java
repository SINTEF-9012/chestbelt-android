package org.thingml.android.chestbelt.graph;

import java.util.ArrayList;
import java.util.Hashtable;

import org.thingml.android.chestbelt.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

//public class MultipleGraphView extends View {
//
//	public static final int BARCHART = 0;
//	public static final int LINECHART = 1;
//
//	private Hashtable<String, GraphWrapper> wrappers = new Hashtable<String, GraphWrapper>();
//	private int bottomOffset = 0;
//	private int topOffset = 0;
//
//	private Paint paint = new Paint();
//	private ArrayList<PaintManager> paintManager = new ArrayList<MultipleGraphView.PaintManager>();
//	private Hashtable<String, int[]> graphValues = new Hashtable<String, int[]>(); 
//	
//	public MultipleGraphView(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		paintManager.start();
//	}
//
//	public void registerWrapper(GraphWrapper wrapper) {
//		this.wrapper = wrapper;
//	}
//	
//	public GraphWrapper getWrapper() {
//		return wrapper;
//	}
//	
//	@Override
//	protected void onMeasure(int h, int w){
//		super.onMeasure(w, h);
//		setMeasuredDimension(h, w);
//	}
//	
//	@Override
//	protected void onDraw(Canvas canvas) {
//		if (graphValues != null) {
//			paint.setColor(wrapper.getColor());
//			//paint.setAntiAlias(true);
//			switch (wrapper.getDrawGraphType()) {
//			case BARCHART:
//				drawBarGraph(canvas);
//				break;
//			case LINECHART:
//				drawLineGraph(canvas);
//				break;
//			}
//		}
//	}
//	
//	private void drawBarGraph(Canvas c) {
//		paint.setStrokeWidth((float) computeX(1) + 1);
//		for (int i = 0 ; i < graphValues.length ; i++) {
//			if (graphValues[i] == wrapper.getBuffer().getInvalidNumber()) {
//				break;
//			} else {
//				c.drawLine(computeX(i), computeY(0), computeX(i), computeY(graphValues[i]), paint);
//			}
//		}
//	}
//	
//	private void drawLineGraph(Canvas c) {
//		int lastY = 0;
//		paint.setStrokeWidth(2);
//		for (int i = 0 ; i < graphValues.length ; i ++) {
//			if (graphValues[i] == wrapper.getBuffer().getInvalidNumber()) {
//				break;
//			} else {
//				int y = computeY(graphValues[i]);
//				if (i == 0) {
//					c.drawLine(computeX(i), y, computeX(i), y, paint);
//				} else {
//					c.drawLine(computeX(i-1), lastY, computeX(i), y, paint);
//				}
//				lastY =  y;
//			}
//		}
//	}
//	
//	protected int computeX(int value) {
//        return value * getWidth() / graphValues.length;
//    }
//
//    protected int computeY(int value) {
//       return getHeight() - bottomOffset - map(value, wrapper.getLowestVisible(), wrapper.getHighestVisible(), bottomOffset, getHeight() - topOffset);
//    }
//
//	protected int findHighestValue() {
//		if (graphValues == null) {
//			return 0;
//		}
//		int max = Integer.MIN_VALUE;
//		for (int i = 0 ; i < graphValues.length ; i++){
//			if(graphValues[i] > max && graphValues[i] != wrapper.getBuffer().getInvalidNumber()){
//				max = graphValues[i];
//			}
//		}
//		return max;
//	}
//
//	protected int findLowestValue() {
//		if (graphValues == null) {
//			return 0;
//		}
//		int min = Integer.MAX_VALUE;
//		for (int i = 0; i < graphValues.length; i++){
//			if(graphValues[i] < min && graphValues[i] != wrapper.getBuffer().getInvalidNumber()){
//				min = graphValues[i];
//			}
//		}
//		return min;
//	}
//
//	private int map(int x, int in_min, int in_max, int out_min, int out_max) {
//		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
//	}
//	
//	private class PaintManager extends Thread {
//		
//		private String name;
//		
//		public PaintManager(String name) {
//			this.name = name;
//		}
//		
//		public void run() {
//			while (true) {
//				if (wrappers.get(name).getBuffer() != null) {
//					graphValues.get(name) = wrappers.get(name).getBuffer().getGraphData();
//					postInvalidate();
//				}
//				try {
//					sleep(wrapper.getSleepTime());
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//}