package com.testPattern;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.example.testbubble.R;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.DialogInterface;
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

public class MainActivity extends Activity {
	String touchEventPath; // Path to the input stream of the touch device /dev/input/eventx
	String touchDescriptor = "4e2720e99bd2b59adae8529881343531fff7c98e"; // Descriptor obtained from the MotionEvent created by the touch device. 
	String getTouchParamCmd = "dumpsys input" + "\n";
	String getTouchEventCmd = "getevent -l " + touchEventPath + "| grep PO" + "\n";

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
			String tempDevice = new String();
			String tempClass = new String();
			String tempPath = new String();
			String tempDescriptor = new String();
			float xScale;
			float yScale;
			
	
			StringBuilder test = new StringBuilder();
			while ((tempString = paramInputStream.readLine()) != null) {
				// Search for given Descriptor:
				if (tempString.matches("    .:.*") ) {
//					test.append(tempString);
					tempDevice = tempString.substring(4);
//					paramInputStream.mark(8192); // 8192 is default BufferedReader buffer size.
					tempClass = getParam("Classes", paramInputStream);
					if (tempClass.equals("0x00000015") || tempClass.equals("0x00000014")) {
						tempPath = getParam ("Path", paramInputStream);
						tempDescriptor = getParam ("Descriptor", paramInputStream);
						if (tempDescriptor.equals(touchDescriptor)) {
							// This is the device we are looking for, so we search for its parameters
							while ((tempString = paramInputStream.readLine()) != null) {
								if (tempString.contains(tempDevice)) {
									xScale = Float.valueOf(getParam("XScale", paramInputStream));
									yScale = Float.valueOf(getParam("YScale", paramInputStream));
									break;	
								}
							}
						}
					}
				}
				outputStream.close();
				paramInputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		} 
		
	
		
		
		
		
		
//		// Get touchscreen device parameters
//		try {
//			byte[] buffer = new byte[8];
//			Process touchWCProcess = Runtime.getRuntime().exec(getTouchParamCmd + "|wc -m");
//			
//			while (touchWCProcess.getInputStream().read(buffer) != -1);
//			Process touchParamProcess = Runtime.getRuntime().exec("echo 'hola");
//			BufferedReader paramInputStream = new BufferedReader (new InputStreamReader(touchParamProcess.getInputStream()));
//
//			String tempString = new String();
//	
//			StringBuilder test = new StringBuilder();
//			while ((tempString = paramInputStream.readLine()) != null) {
//				
//			}					
//		} catch (Exception e) {
//			e.printStackTrace();
//			// TODO: handle exception
//		} 
		
//		// Start raw touch screen coordinates input stream
//		new Thread (new Runnable() {
//			@Override
//			public void run() {
//				StringBuilder touchLog = new StringBuilder();
//				byte[] touchByte = new byte[7];
//				BufferedInputStream touchInputStream;
//				
//				try {
//					Process touchEventProcess = Runtime.getRuntime().exec(getTouchEventCmd);
//					touchInputStream = new BufferedInputStream (touchEventProcess.getInputStream());
//					while (touchInputStream.read(touchByte) != -1) {
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//				} 		
//			}
//		}).start();
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
}
