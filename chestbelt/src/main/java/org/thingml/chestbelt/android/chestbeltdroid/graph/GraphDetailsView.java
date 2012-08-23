package org.thingml.chestbelt.android.chestbeltdroid.graph;

import org.thingml.chestbelt.android.chestbeltdroid.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

public class GraphDetailsView extends GraphBaseView {
	
	private Paint paint = new Paint();
	
	public GraphDetailsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.graph_details);
		checkAttributes(customAttrs);
	}

	private void checkAttributes(TypedArray customAttrs) {
		getWrapper().setName(customAttrs.getString(R.styleable.graph_details_name));
		getWrapper().setLineNumber(customAttrs.getInteger(R.styleable.graph_details_lines, 0));
	}
	
	@Override
	protected void onMeasure(int h, int w) {
		super.onMeasure(h, w);
		setMeasuredDimension(h, w);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawGrid(canvas, getWrapper().getLineNumber());
		if (getWrapper().printName()) {
			drawName(canvas);
		}
		if (getWrapper().printValue()) {
			drawValue(canvas);
		}
		if (getWrapper().printScale()) {
			drawScale(canvas);
		}
	}
	
	private void drawValue(Canvas canvas) {
		paint.setColor(Color.WHITE);
		paint.setTextSize(25);
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(String.valueOf(getLastValue()), getWidth() / 2, getHeight() / 2, paint);
	}

	private void drawName(Canvas canvas) {
		if (getWrapper().getName() != null) {
			paint.setColor(Color.WHITE);
			paint.setTextSize(30);
			paint.setTextAlign(Paint.Align.CENTER);
			canvas.drawText(getWrapper().getName(), getWidth() / 2, 30, paint);
		}
	}

	private void drawScale(Canvas canvas) {
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(20);
		paint.setTextAlign(Paint.Align.LEFT);
		canvas.drawText(String.valueOf(getWrapper().getHighestVisible()), 0, 20, paint);
		canvas.drawText(String.valueOf(getWrapper().getLowestVisible()), 0, getHeight(), paint);
		paint.setTextAlign(Paint.Align.RIGHT);
		if (!getWrapper().getBuffer().isEmpty()) {
			canvas.drawText(String.valueOf(findHighestValue()), getWidth(), 20, paint);
			canvas.drawText(String.valueOf(findLowestValue()), getWidth(), getHeight(), paint);
		}
	}

	private void drawGrid(Canvas canvas, int lines) {
		if (lines != 0) {
			int linePosition = getHeight() / lines;
			paint.setStrokeWidth(1);
			paint.setColor(Color.WHITE);
			for (int k = 0 ; k < lines - 1 ; k ++) {
				canvas.drawLine(0, linePosition, canvas.getWidth(), linePosition, paint);
				linePosition = linePosition + (getHeight() / lines);
			}
		}
	}
}
