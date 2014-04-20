package com.testPattern;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Juan Herrero Macias
 * @mail juan.herrero@bq.com
 *
 */

public class PatternParameters {
    private Context ctx;
    private final int default_density = 3;
    private float xdpi;
    private float ydpi;
    private int heightPixels;
    private int widthPixels;
    
   //Patterns provided by default 
    public enum Patterns {
        DEFAULT_TEST,
//        BORDER_TEST;
    }
    
    public PatternParameters(Context context) {
    	// Get the screen dimensions so the patterns fit the screen correctly
    	DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    	xdpi = metrics.xdpi;
    	ydpi = metrics.ydpi;
    	heightPixels = metrics.heightPixels;
    	widthPixels = metrics.widthPixels;
    }
    
    /**
     * 
     * @param tPatterns One of the predefined patterns provided
     * @param density Density of the pattern that will be drawn over the screen
     * @return The array of points needed to draw the pattern in the correct order.
     */
    public Point[] getPattern(Patterns tPattern , int density){
        Point[] pattern = null;
        
        switch (tPattern) {
            case DEFAULT_TEST:
                pattern = getDefaultPattern(density);
                break;
            //TODO: Add more pattern tests
            default:
                break;
        }
        return pattern;
    }

    /**
     * Default pattern constructor
     * @param density
     * @return
     */
    private Point[] getDefaultPattern(int density) {
        // Variable initialization
        ArrayList<Point> pointList = new ArrayList<Point>();
        int[] x = new int[density + 2];
        int [] y = new int[density + 2];
        
        // Getting pattern coordinates
        for (int i = 0; i < density + 2; i++) {
            x[i] = (int) (widthPixels * i / (density + 1));
            y[i] = (int) (heightPixels * i / (density + 1));
        }
        
        // Generating pattern points
        pointList.add(0, new Point(0, 0));
        for (int j = 1; j < density + 1; j++) {
            pointList.add(new Point(x[j], 0));
            pointList.add(new Point(x[density + 1], y[density + 1 - j]));
            pointList.add(new Point(x[density + 1 -j], y[density + 1]));
            pointList.add(new Point(0, y[j]));
            pointList.add(new Point(x[j], 0));
        } 
        pointList.add(new Point(x[density + 1], 0));
        pointList.add(new Point(0, y[density + 1]));
        pointList.add(new Point(x[density + 1], y[density + 1]));
        pointList.add(new Point(0, 0));
        pointList.add(new Point(0, y[density + 1]));

        return pointList.toArray(new Point[pointList.size()]);
    }
    
    private Point [] getDefaultPattern() {
    	return getDefaultPattern(default_density);
    }
}
