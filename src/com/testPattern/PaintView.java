package com.testPattern;

import com.testPattern.PatternParameters.Patterns;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PaintView extends SurfaceView implements SurfaceHolder.Callback {
    // Functions that modify tolerance value should also update tolerance_2
    private int tolerance = 5;
    private int tolerance_2 = tolerance * tolerance;
    
    private Point lastTouch = new Point();
    private Point touch = new Point();
  
    private Canvas testCanvas = null;
    private int canvasWidth = 0;
    private int canvasHeight = 0;
    private Bitmap canvasBitmap = null;
    private Matrix identityMatrix;
    
    private SurfaceHolder sh;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Context ctx;
    
    private PatternParameters testedPattern;

    /**
     * Constructor
     * @param context The UI Activity Context
     */
    public PaintView(Context context) {
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
        touch.x = (int)event.getX();
        touch.y = (int)event.getY();
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            if (processTolerance(touch, lastTouch)) {
                doDrawCircle(testCanvas, touch, tolerance / 2);
                lastTouch.x = touch.x;
                lastTouch.y = touch.y;
                Log.v("Coordinate", "Cooldinates are" + touch.x + " , " + touch.y);
            }
            break;
        
        case MotionEvent.ACTION_MOVE:
            if (processTolerance(touch, lastTouch)) {
                doDrawLine(testCanvas, touch, lastTouch);
                lastTouch.x = touch.x;
                lastTouch.y = touch.y;
                Log.v("Coordinate", "Cooldinates are" + touch.x + " , " + touch.y); 
            }
            break;

        case MotionEvent.ACTION_UP:
            if (processTolerance(touch, lastTouch)) {
                doDrawCircle(testCanvas, touch, tolerance / 2);
                lastTouch.x = touch.x;
                lastTouch.y = touch.y;
                Log.v("Coordinate", "Cooldinates are" + touch.x + " , " + touch.y);
            }
            break;
        }
        return false;
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
//      canvasBitmap = null;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {      
        setSurfaceSize(width, height);
      }
    
    /**
     * @param p Point 1
     * @param d Point 2
     * @return True if the distance between points is higher than tolerance
     */
    private boolean processTolerance(Point p, Point d) {
        return ((p.x - d.x) * (p.x - d.x) + 
                (p.y - d.y) * (p.y - d.y) > tolerance_2);
    }
        
    /**
     * Called when drawing surface changes its size
     * @param width
     * @param height
     */
    public void setSurfaceSize(int width, int height) {
        synchronized (sh) {
            canvasWidth = width;
            canvasHeight = height;
        }
    }
     
    /**
     * Draws a line between two points in the canvas provided
     * @param canvas
     * @param p Point 1
     * @param d Point 2
     */
    private void doDrawLine(Canvas canvas, Point p, Point d) {
        canvas.drawLine(d.x, d.y, p.x, p.y, paint);
        //TODO: Create a try catch structure to avoid possible errors if canvas is already locked
        synchronized (sh) {
        	Canvas c = sh.lockCanvas(null);
            c.drawBitmap(canvasBitmap, identityMatrix, null);
            sh.unlockCanvasAndPost(c);
		}
        Log.v("Line draw", " Drawing line between [" + p.x + ", " + p.y + "] and [" + d.x + ", " + d.y + "]");
    }
    
    /**
     * Draw a circle in canvas provided
     * @param canvas
     * @param p Centre of the circle
     * @param r Radius of the circle
     */
    private void doDrawCircle(Canvas canvas, Point p, float r) {
        canvas.drawCircle(p.x, p.y, r, paint);
      //TODO: Create a try catch structure to avoid possible errors if canvas is already locked
        synchronized (sh) {
        	Canvas c = sh.lockCanvas(null);
            c.drawBitmap(canvasBitmap, identityMatrix, null);
            sh.unlockCanvasAndPost(c);
		}
        Log.v("Circle draw", " Drawing circle in " + p.x + " , " + p.y);
    }
    /**
     * Draws a pattern provided by PatternParameters
     * @param pattern The pattern you want to be drawn
     * @param density The density of the pattern
     */
    public void drawPattern(Patterns pattern, Canvas canvas, int density){
        PatternParameters testedPattern = new PatternParameters(getContext());
        Point[] points = testedPattern.getPattern(pattern, density); 
        Paint patternPaint = new Paint();
        patternPaint.setColor(Color.YELLOW);
        patternPaint.setStyle(Style.STROKE);
        patternPaint.setStrokeWidth(tolerance * 2);
        for (int i = 0; i < points.length - 1; i++) {
            canvas.drawLine(points[i].x, points[i].y, points[i+1].x, points[i+1].y, patternPaint);
        }
        Canvas c = sh.lockCanvas(null);
        c.drawBitmap(canvasBitmap, identityMatrix, null);
        sh.unlockCanvasAndPost(c); 
    }
}

