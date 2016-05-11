/*
* class TouchListener is intended to recognize some base gestures 
* method onGesture(View v, boolean gestureEnd, Set<Gesture> setGestures) should be overridden
* in order to receive gesture information 
*/

package com.kivsw.dialog;

import android.content.Context;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.util.EnumSet;
import java.util.Set;

//import android.graphics.Point;
//import android.widget.TextView;
//import java.util.Collection;
//import java.util.HashMap;

public class TouchListener implements  android.view.View.OnTouchListener
{

	Context context;
	
	class Pointer // contains initial and final position of the finger
	{
		float X0,Y0,    // coordinates where the touch has started
		      Xcur,Ycur;// coordinates where the touch has ended
		long t0, tcur;  // the time of the first and the last touches.
		Pointer(float X, float Y, long t){X0=X; Y0=Y;t0=t;};
	};
	
	protected SparseArray<Pointer> pointers; // active events of touching
	
	boolean isGestureEnd=true; // true if the current gesture is finished
	public enum Gesture {gRotation, gPinch, gHSwipe, gVSwipe, gTap, gLongTap, gDoubleTap}; // all recognized gestures
	EnumSet<Gesture> gesturesSet =null; // set holds the recognized gestures
	
	long previousTapTime=0;            // hold the time of previous Tap event
	View previousTapedView=null;       // hold view that has been taped
	
	// the thresholds that means the event start
	double rotateThreshold=15.0/180*Math.PI,  // 15 degree for rotation
		   zoomingThreshold = 0.10,           // 10% for zooming
		   swipeThreshold = 0.15;             // 0.15 inch for sweep
	
	DisplayMetrics diaplayMetrics = null;     // the display information
	
	//-----------------------------------------------------------------------------
	public TouchListener(Context context)
	{
		this.context = context;
		pointers = new SparseArray<Pointer>();
		diaplayMetrics = new DisplayMetrics();
		gesturesSet =EnumSet.noneOf(Gesture.class);
		

		
	}
	//-----------------------------------------------------------------------------
	@Override
	public boolean onTouch(View v, MotionEvent event) 
	{
		int pointerCount=event.getPointerCount(); // amount of touches
//		int historySize = event.getHistorySize();
		int action=event.getAction();
		
		if(isGestureEnd ) // if previous gesture has stopped
		{ // clear old data
			pointers.clear(); 
			isGestureEnd = false;
			gesturesSet.clear(); // clear flags of the recognizable gestures
			Display display = 
						((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            display.getMetrics (diaplayMetrics);
		}
		
		switch(action)
		{
		case MotionEvent.ACTION_DOWN: // the first touch

		case MotionEvent.ACTION_POINTER_DOWN : // the second and following touch
		case MotionEvent.ACTION_MOVE: // moving
		     {
				for (int p = 0; p < pointerCount; p++) 
				{
					int id=event.getPointerId(p);
					Pointer P = pointers.get(id);
					float X=event.getX(p),Y=event.getY(p);
					if(P==null)
						pointers.put(id, P=new Pointer(X,Y,SystemClock.elapsedRealtime()));
					P.Xcur=X; P.Ycur=Y;P.tcur=SystemClock.elapsedRealtime();
				}
			 };

			 break;	
			
		case MotionEvent.ACTION_POINTER_UP: // when a finger goes up, it means
		case MotionEvent.ACTION_UP:         // the end of a gesture
		case MotionEvent.ACTION_CANCEL:
			isGestureEnd = true;
			break;	
				   
		};
		
		// if the user remove one of touched fingers 
		if(pointerCount<pointers.size()) 
			isGestureEnd = true;
		
		handler(v);
		

		return true; // 
	};
	//-----------------------------------------------------------------------------
	// calculate the rotation angle
	public double getRotationAngleD() // degree
	{return getRotationAngle()*180/Math.PI; } ;
	public double getRotationAngle()  // radian
	{
		if(pointers.size()!=2) return 0;
		
		//Pointer values[] = pointers.values().toArray(new Pointer[2]);
		
		double a0=Math.atan2(pointers.valueAt(1).Y0  -pointers.valueAt(0).Y0, 
				             pointers.valueAt(1).X0  -pointers.valueAt(0).X0); // +-PI
		double a1=Math.atan2(pointers.valueAt(1).Ycur-pointers.valueAt(0).Ycur, 
				             pointers.valueAt(1).Xcur-pointers.valueAt(0).Xcur); // +-PI
		
		// decide where the rotation is directed
		double d=(a1-a0);
		if(d<-Math.PI)d+=  2*Math.PI;
		if(d>Math.PI) d-=  2*Math.PI;
		
		return d;
	}
	//-----------------------------------------------------------------------------
	// calculate the scale of zooming
	// when the user is pinching
	public double getZoom()
	{
		if(pointers.size()!=2) return 1;
		
		// the distance between the touches
		double d0 = Math.hypot((pointers.valueAt(0).Y0-pointers.valueAt(1).Y0),    // at the gesture start
				               (pointers.valueAt(0).X0-pointers.valueAt(1).X0));
		double d1 = Math.hypot((pointers.valueAt(0).Ycur-pointers.valueAt(1).Ycur),// at the gesture finish
				               (pointers.valueAt(0).Xcur-pointers.valueAt(1).Xcur));
		
		if(d0<1) return 0;
		
		return d1/d0;
	}
	//-----------------------------------------------------------------------------
	// calculate horizontal distance of the sweep (inch)
	public double getHSwipe()
	{
		if(pointers.size()!=1) return 0;
		double v=(pointers.valueAt(0).Xcur - pointers.valueAt(0).X0)/diaplayMetrics.xdpi;
		return v;
	}
	//-----------------------------------------------------------------------------
	// calculate vertical distance of the sweep (inch)
	public double getVSwipe()
	{
		if(pointers.size()!=1) return 0;
		double v=(pointers.valueAt(0).Ycur - pointers.valueAt(0).Y0)/diaplayMetrics.ydpi;
		return v;
	}
	//-----------------------------------------------------------------------------
	
	private void handler(View v)
	{
		
		// check the start of rotation
		if(!gesturesSet.contains(Gesture.gRotation))
		{
			double angle = getRotationAngle();
			if(angle<-rotateThreshold || angle>rotateThreshold)
				gesturesSet.add(Gesture.gRotation);
		}
		// check the start of zooming
		if(!gesturesSet.contains(Gesture.gPinch))
		{
			double zoom = getZoom();
			if(zoom<1-zoomingThreshold || zoom>1+zoomingThreshold)
				gesturesSet.add(Gesture.gPinch);
		}
		
		// check the start of sweeping up/down
		if(!gesturesSet.contains(Gesture.gVSwipe))
		{
			double sweep = getVSwipe();
			if(sweep>swipeThreshold || sweep < -swipeThreshold)
				gesturesSet.add(Gesture.gVSwipe);
		}
		//check the start of sweeping left/right
		if(!gesturesSet.contains(Gesture.gHSwipe))
		{
			double sweep = getHSwipe();
			if(sweep>swipeThreshold || sweep < -swipeThreshold)
				gesturesSet.add(Gesture.gHSwipe);
		}
		// recognize gTap, gLongTap and gDoubleTap
		if(isGestureEnd && gesturesSet.isEmpty()
				&& pointers.size()==1) // if the gesture ended and there is not another gesture
		{
			
			// choose one of the taps
			if( ((pointers.valueAt(0).t0-previousTapTime) <= ViewConfiguration.getDoubleTapTimeout())&& // check the time between the end of the previous tab and the start of the current tap
			    (v==previousTapedView))
			{
				gesturesSet.add(Gesture.gDoubleTap);
			}

		    if((pointers.valueAt(0).tcur-pointers.valueAt(0).t0)>=ViewConfiguration.getLongPressTimeout())
		    {
		    	gesturesSet.add(Gesture.gLongTap);
		    }

		    if(gesturesSet.isEmpty())
	    	    gesturesSet.add(Gesture.gTap);
	    	
			previousTapTime = pointers.valueAt(0).tcur;
			previousTapedView = v;
		}
		
		onGesture(v,isGestureEnd, gesturesSet);
	};
	//-----------------------------------------------------------------------------
	// this method should be overridden
	protected void onGesture(View v, boolean gestureEnd, Set<Gesture> setGestures)
	{
		
		
	};
	

}
