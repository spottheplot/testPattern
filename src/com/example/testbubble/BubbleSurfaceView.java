package com.example.testbubble;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Rect;

public class BubbleSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private int tolerance = 5;
	private int tolerance_2 = tolerance * tolerance;
	private int lastCircleX;
	private int lastCircleY;
	private int circleX;
	private int circleY;
	
	private Canvas testCanvas = null;
	private int canvasWidth = 0;
	private int canvasHeight = 0;
	private Bitmap canvasBitmap = null;
	private Matrix identityMatrix;
	private boolean process = false;
	
	private SurfaceHolder sh;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	Context ctx;

	public BubbleSurfaceView(Context context) {
		super(context);
		sh = getHolder();
		sh.addCallback(this);
		paint.setColor(Color.BLUE);
		paint.setStyle(Style.FILL);
		paint.setStrokeWidth(tolerance);
		ctx = context;
		canvasWidth = getWidth();
		canvasHeight = getHeight();
		setFocusable(true); // make sure we get key events
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		circleX = (int)event.getX();
		circleY = (int)event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (processTolerance(circleX, circleY)) {
				doDrawCircle(testCanvas, circleX, circleY);
				lastCircleX = circleX;
				lastCircleY = circleY;
				Log.d("Coordinate", "Cooldinates are" + circleX + " , " + circleY);
			}
			break;
		
		case MotionEvent.ACTION_MOVE:
			if (processTolerance(circleX, circleY)) {
				doDrawLine(testCanvas, circleX, circleY);
				lastCircleX = circleX;
				lastCircleY = circleY;
				Log.d("Coordinate", "Cooldinates are" + circleX + " , " + circleY);	
			}
			break;

		case MotionEvent.ACTION_UP:
			if (processTolerance(circleX, circleY)) {
				doDrawCircle(testCanvas, circleX, circleY);
				lastCircleX = circleX;
				lastCircleY = circleY;
				Log.d("Coordinate", "Cooldinates are" + circleX + " , " + circleY);
			}
			break;
		}
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		canvasWidth = Math.abs(holder.getSurfaceFrame().width());
		canvasHeight = Math.abs(holder.getSurfaceFrame().height());
	    canvasBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888);
	    testCanvas = new Canvas(canvasBitmap);
	    testCanvas.setBitmap(canvasBitmap);
	    identityMatrix = new Matrix();
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
//		canvasBitmap = null;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {      
	    setSurfaceSize(width, height);
//	    canvasWidth = width;
//		canvasHeight = height; 
	  }
	
	private boolean processTolerance(int x, int y) {
		return ((lastCircleX - x) * (lastCircleX - x) + 
				(lastCircleY - y) * (lastCircleY - y) > tolerance_2);
	}
		
	public void setSurfaceSize(int width, int height) {
		synchronized (sh) {
			canvasWidth = width;
			canvasHeight = height;
		}
	}
	
	private void doDrawLine(Canvas canvas, int circleX, int circleY) {
		canvas.drawLine(lastCircleX, lastCircleY, circleX, circleY, paint);
		Canvas c = sh.lockCanvas(null);
		c.drawBitmap(canvasBitmap, identityMatrix, null);
		sh.unlockCanvasAndPost(c);
		Log.d("Circle draw", " Drawing circle in " + circleY + " , " + circleX);
		
//		process = false;
	}
	
	private void doDrawCircle(Canvas canvas, int circleX, int circleY) {
		canvas.drawCircle(circleX, circleY, tolerance / 2, paint);
		Canvas c = sh.lockCanvas(null);
		c.drawBitmap(canvasBitmap, identityMatrix, null);
		sh.unlockCanvasAndPost(c);
		Log.d("Circle draw", " Drawing circle in " + circleY + " , " + circleX);
		
//		process = false;
	}
	
	private void setProcessing (boolean b) {
		process = b;
	}

}

