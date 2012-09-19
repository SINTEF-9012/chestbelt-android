package org.thingml.chestbelt.android.chestbeltdroid.graph;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GraphBaseView extends View {

	public interface GraphListenner {
		public void lastValueChanged(int value);
	}
	
	public static final int BARCHART = 0;
	public static final int LINECHART = 1;

	private GraphListenner listenner;
	private GraphWrapper wrapper = new GraphWrapper();
	private int lastValue;
	private int bottomOffset = 0;
	private int topOffset = 0;

	private Paint paint = new Paint();
	private PaintManager paintManager = new PaintManager();
	private int[] graphValues; 
	
	public GraphBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.graph_base);
		checkAttributes(customAttrs);
		paintManager.start();
	}

	private void checkAttributes(TypedArray customAttrs) {
		wrapper.setColor(customAttrs.getInteger(R.styleable.graph_base_color, Color.RED));
		wrapper.setLowestVisible(customAttrs.getInteger(R.styleable.graph_base_minimum, 0));
		wrapper.setHighestVisible(customAttrs.getInteger(R.styleable.graph_base_maximum, 500));
		wrapper.setSleepTime(customAttrs.getInteger(R.styleable.graph_base_refresh, 1000));
		wrapper.setDrawGraphType(customAttrs.getInteger(R.styleable.graph_base_style, LINECHART));
	}

	public void registerListenner(GraphListenner listenner) {
		this.listenner = listenner;
	}
	
	public void unregisterListenner(GraphListenner listenner) {
		if (this.listenner == listenner) {
			this.listenner = null;
		}
	}
	
	public void registerWrapper(GraphWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	public GraphWrapper getWrapper() {
		return wrapper;
	}
	
	public int getLastValue() {
		return lastValue;
	}
	
	@Override
	protected void onMeasure(int h, int w){
		super.onMeasure(w, h);
		setMeasuredDimension(h, w);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (graphValues != null) {
			paint.setColor(wrapper.getColor());
			//paint.setAntiAlias(true);
			switch (wrapper.getDrawGraphType()) {
			case BARCHART:
				drawBarGraph(canvas);
				break;
			case LINECHART:
				drawLineGraph(canvas);
				break;
			default:
				break;
			}
		}
	}
	
	private void drawBarGraph(Canvas c) {
		paint.setStrokeWidth((float) computeX(1) + 1);
		for (int i = 0 ; i < graphValues.length ; i++) {
			if (graphValues[i] == wrapper.getBuffer().getInvalidNumber()) {
				break;
			} else {
				c.drawLine(computeX(i), computeY(0), computeX(i), computeY(graphValues[i]), paint);
			}
		}
	}
	
	private void drawLineGraph(Canvas c) {
		int lastY = 0;
		paint.setStrokeWidth(2);
		for (int i = 0 ; i < graphValues.length ; i ++) {
			if (graphValues[i] == wrapper.getBuffer().getInvalidNumber()) {
				break;
			} else {
				int y = computeY(graphValues[i]);
				if (i == 0) {
					c.drawLine(computeX(i), y, computeX(i), y, paint);
				} else {
					c.drawLine(computeX(i-1), lastY, computeX(i), y, paint);
				}
				lastY =  y;
			}
		}
	}
	
	protected int computeX(int value) {
        return value * getWidth() / graphValues.length;
    }

    protected int computeY(int value) {
       return getHeight() - bottomOffset - map(value, wrapper.getLowestVisible(), wrapper.getHighestVisible(), bottomOffset, getHeight() - topOffset);
    }

	protected int findHighestValue() {
		if (graphValues == null) {
			return 0;
		}
		int max = Integer.MIN_VALUE;
		for (int i = 0 ; i < graphValues.length ; i++){
			if(graphValues[i] > max && graphValues[i] != wrapper.getBuffer().getInvalidNumber()){
				max = graphValues[i];
			}
		}
		if (max == Integer.MIN_VALUE) {
			return 0;
		}
		return max;
	}

	protected int findLowestValue() {
		if (graphValues == null) {
			return 0;
		}
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < graphValues.length; i++){
			if(graphValues[i] < min && graphValues[i] != wrapper.getBuffer().getInvalidNumber()){
				min = graphValues[i];
			}
		}
		if (min == Integer.MAX_VALUE) {
			return 0;
		}
		return min;
	}

	private int map(int x, int in_min, int in_max, int out_min, int out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}
	
	private class PaintManager extends Thread {
		public void run() {
			while (true) {
				if (wrapper.getBuffer() != null) {
					graphValues = wrapper.getBuffer().getGraphData();
					lastValue = wrapper.getBuffer().getLastValue();
					if (listenner != null) {
						listenner.lastValueChanged(lastValue);
					}
					postInvalidate();
				}
				try {
					sleep(wrapper.getSleepTime());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}