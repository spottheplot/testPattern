package com.testPattern;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import com.example.testbubble.R;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Point;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.os.Build;


//TODO: Remove the activity part from this class and let it be as an static method class to get the device parameters and touch events
/**
 * This class provides methods to obtain the devices parameters
 * @author Juan Herrero Macias
 *
 */
public class MainActivity extends Activity {
	String touchEventPath = new String(); // Path to the input stream of the touch device /dev/input/eventx
	String touchDescriptor = "4e2720e99bd2b59adae8529881343531fff7c98e"; // Descriptor obtained from the MotionEvent created by the touch device. 
	String getTouchParamCmd = "dumpsys input" + "\n";
	String getTouchEventCmd = "getevent -l " + touchEventPath + "| grep PO" + "\n";
	
	String deviceName = new String();
	String deviceClass = new String();
	String devicePath = new String();
	String deviceDescriptor = new String();
	float xScale;
	float yScale;
	
	// getevent output labels
//	final String ABS_MT_TRACKING_ID = "0039";
//	final String ABS_MT_POSITION_X = "0035";
//	final String ABS_MT_POSITION_Y = "0036";
//	final String ABS_MT_PRESSURE = "003a";
//	final String SYN_REPORT = "0000";
//	final String ABS_MT_ORIENTATION  = "0034";
//	final String ABS_SLOT = "002f";
	
	// getevent output byte labels
	final byte ABS_MT_TRACKING_ID = 0x39;
	final byte ABS_MT_POSITION_X = 0x35;
	final byte ABS_MT_POSITION_Y = 0x36;
	final byte ABS_MT_PRESSURE = 0x3a;
	final byte SYN_REPORT = 0x00;
	
	// ABS_MT_TRACKING_ID value when a touch event ends
//	final String TOUCH_END = "ffffffff";
	final byte TOUCH_END = (byte)0xff;
	
	// Touch event Point variables
	ArrayList<Point> pointList = new ArrayList<Point>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
		PaintView pView = new PaintView(this);
		
//		// Create a button that obtains needed parameters and starts the test
//		Button startButton = (Button) findViewById(R.id.buttonStart);
//		
//		startButton.setOnTouchListener(new OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				InputDevice x = event.getDevice();
//				touchDescriptor = x.getDescriptor();
////				setContentView(pView);
//				return false;
//			}
//		});
//		
//		setContentView(R.layout.activity_main);
		
		setContentView(pView);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		try{
		    Process su = Runtime.getRuntime().exec("su");
		    DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
		    BufferedReader paramInputStream = new BufferedReader (new InputStreamReader(su.getInputStream()));
		    outputStream.writeBytes("dumpsys input\n");
		    outputStream.flush();

		    String tempString = new String();
	
//			StringBuilder test = new StringBuilder();
			while ((tempString = paramInputStream.readLine()) != null) {
				// Search for given Descriptor:
				if (tempString.matches("    .:.*") ) {
//					test.append(tempString);
					deviceName = tempString.substring(4);
					deviceClass = getParam("Classes", paramInputStream);
					if (deviceClass.equals("0x00000015") || deviceClass.equals("0x00000014")) {
						devicePath = getParam ("Path", paramInputStream);
						deviceDescriptor = getParam ("Descriptor", paramInputStream);
						// This is the device we are looking for, so we search for the relationship between the touch sensor resolution and the pixel resolution
						if (deviceDescriptor.equals(touchDescriptor)) {
							touchEventPath = devicePath;
							while ((tempString = paramInputStream.readLine()) != null) {
								if (tempString.contains(deviceName)) {
									// Touch sensor report * x/yScale = Pixel scale touch location.
									xScale = Float.valueOf(getParam("XScale", paramInputStream));
									yScale = Float.valueOf(getParam("YScale", paramInputStream));
									outputStream.close();
									paramInputStream.close();
									break;	
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
			//TODO: Implement an alternative when device parameters can't be parsed from dumpsys input
		} 
		
		/**
		 * This thread will parse the output bytes of the input devices directly from linux kernel		
		 */
		// Start raw touch screen coordinates input stream
		new Thread (new Runnable() {
			@Override
			public void run() {
				try {
					Process su = Runtime.getRuntime().exec("su");
					DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
					BufferedInputStream touchInputStream = new BufferedInputStream(su.getInputStream());
					outputStream.writeBytes("cat " + devicePath + "\n");
					outputStream.flush();

//					String tempString = new String();
					int xTouch = 0;
					int yTouch = 0;
					boolean isNewPoint = false;
//					int pressure = 0; 
					
					byte[] bBuffer = new byte[4];
					while (touchInputStream.skip(10) != 0 
							&& touchInputStream.read(bBuffer) != -1
							&& touchInputStream.skip(2) != 0) {
						if (bBuffer[0] == ABS_MT_TRACKING_ID
								&& bBuffer[2] != TOUCH_END
								&& bBuffer[3] != TOUCH_END) {
							while (touchInputStream.skip(10) != 0 
									&& touchInputStream.read(bBuffer) != -1
									&& touchInputStream.skip(2) != 0
									&& bBuffer[2] != TOUCH_END
									&& bBuffer[3] != TOUCH_END) {
								switch (bBuffer[0]) {
								case ABS_MT_POSITION_X:
									// First or last 8 bits are masked to avoid values over 0x80 being casted to negative int
									xTouch = (bBuffer[3] << 8 & 0xFF00 | bBuffer[2] & 0xFF);
									isNewPoint = true;
									break;
									
								case ABS_MT_POSITION_Y:
									yTouch = bBuffer[3] << 8 | bBuffer[2] & 0xFF;
									isNewPoint = true;
									break;
									
								case SYN_REPORT:
									if (isNewPoint) {
										pointList.add(new Point(xTouch, yTouch));
										isNewPoint = false;
									}
									break;

								default:
									break;
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					// TODO: handle exception
				} 	
			}
		}).start();
	}
	
	/**
	 * Gets 
	 * @return First Point in the touch Points array list
	 */
	public Point getNextTouch() {
		Point returnPoint = null;
		if (pointList.size() > 0) { 
			returnPoint = pointList.get(0);
			pointList.remove(0);
			//TODO: If pointList gets too big, we should consider trimming it
			// pointList.trimToSize();
		}
		return returnPoint;
	}
	
	public Point[] getRemainingTouchPoints() {
		Point[] tempPoints =  null;
		int size;
		if ((size = pointList.size()) > 0) {
			tempPoints = pointList.toArray(new Point[size]);
			for (int i = 0; i < size; i++) {
				pointList.remove(0);
			}
			//TODO: If pointList gets too big, we should consider trimming it
			//	pointList.trimToSize();
		}
		return tempPoints;
	}
			
	/**
	 * Finds the next line in the provided inputStream beginning with parameter value and returns its content.
	 * This method is written for parsing the output of $dumpsys input command
	 * @param parameter
	 * @param inputStream Input stream associated with the output of $dumpsys input
	 * @return
	 */
	private String getParam(String parameter, BufferedReader inputStream) {
		String tempString;
		String returnString = new String();
		try {
			while ((tempString = inputStream.readLine().trim()) != null) {
				if (tempString.startsWith(parameter) == true) {
					returnString = tempString.substring((parameter + ": ").length()); 
					break;
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
		}
		return returnString;
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		// TODO Auto-generated method stub
//		InputDevice x = event.getDevice();
//		String Descriptor = x.getDescriptor();
//		return super.onTouchEvent(event);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
