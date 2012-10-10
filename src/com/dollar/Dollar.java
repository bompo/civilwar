package com.dollar;

/*
 *	Java / Java ME port of the $1 Gesture Recognizer by  
 *	Jacob O. Wobbrock, Andrew D. Wilson, Yang Li.
 * 
 *	A quick port that needs to be polished, documented and optimized...!
 *	Send me an e-mail (address can be found at olwal.com) if you'd like to get an update when the library is updated, 
 *  and feel free to send any updates or changes you make!  
 *
 *	@author Alex Olwal
 *
 *	@version 0.1
 *
 *	@see http://depts.washington.edu/aimgroup/proj/dollar/
 *
 */

import java.util.Vector;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Dollar
{
	protected int x, y;
	protected int state;
		
	protected int _key = -1;
	
	protected boolean gesture = true; 
	protected Vector points = new Vector(1000);
	
	protected Recognizer recognizer;
	protected Result result = new Result("no gesture", 0, -1);
	
	protected boolean active = false;
	
	protected DollarListener listener = null;

	public static final int GESTURES_DEFAULT = 1;
	public static final int GESTURES_SIMPLE = 2;
	public static final int GESTURES_CIRCLES = 3;
	public static final int GESTURES_CIVIL_WAR = 4;
	
	protected int gestureSet;
	
	public Dollar()
	{
		this(GESTURES_SIMPLE);
	}
	
	public Dollar(int gestureSet)
	{
		this.gestureSet = gestureSet;
		recognizer = new Recognizer(gestureSet);
	}
	
	public void setListener(DollarListener listener)
	{
		this.listener = listener;
	}
	
	public void addPoint(int x, int y)
	{
		if (!active)
			return;
		
		points.addElement(new Vector2(x, y));
//		System.out.println(x + " " + y + " " + points.size());
	}	
	
	public void recognize()
	{
		if (!active)
			return;
		
		if (points.size() == 0) //the recognizer will crash if we try to process an empty set of points...
			return;
		
		result = recognizer.Recognize(points);		
//		points.removeAllElements();
		
		if (listener != null)
			listener.dollarDetected(this);
	}

	public Rectangle getBoundingBox()
	{
		return recognizer.boundingBox;
	}
	
	public int[] getBounds()
	{
		return recognizer.bounds;
	}
	
	public Vector2 getPosition()
	{
		return recognizer.centroid;
	}
	
	public String getName()
	{		
		return result.Name;
	}
	
	public double getScore()
	{
		return result.Score;
	}

	public int getIndex()
	{
		return result.Index;
	}

	public void setActive(boolean state)
	{
		active = state;
	}
	
	public boolean getActive()
	{
		return active;
	}	
	
	public void pointerPressed(int x, int y)
	{
		clear();
	}
	
	public void pointerReleased(int x, int y)
	{
		recognize();
	}
	
	public void pointerDragged(int x, int y)
	{
		addPoint(x, y);
	}
	
	public void clear()
	{
		points.removeAllElements();
		result.Name = "";
		result.Score = 0;
		result.Index = -1;
	}
	
}


