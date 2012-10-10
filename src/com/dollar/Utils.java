package com.dollar;

//TODO: Most of the code here could be significantly optimized. This was a quick port from the C# version of the library

import java.util.Vector;
import java.util.Enumeration;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Utils
{	 
	public static Vector Resample(Vector points, int n)
	{		
		double I = PathLength(points) / (n - 1); // interval length
		double D = 0.0;
		
		Vector srcPts = new Vector(points.size());
		for (int i = 0; i < points.size(); i++)
			srcPts.addElement(points.elementAt(i));
		
		Vector dstPts = new Vector(n);
		dstPts.addElement(srcPts.elementAt(0));	//assumes that srcPts.size() > 0
		
		for (int i = 1; i < srcPts.size(); i++)
		{
			Vector2 pt1 = (Vector2) srcPts.elementAt(i - 1);
			Vector2 pt2 = (Vector2) srcPts.elementAt(i);

			double d = Distance(pt1, pt2);
			if ((D + d) >= I)
			{
				double qx = pt1.x + ((I - D) / d) * (pt2.x - pt1.x);
				double qy = pt1.y + ((I - D) / d) * (pt2.y - pt1.y);
				Vector2 q = new Vector2((float)qx, (float)qy);
				dstPts.addElement(q); // append new Vector2 'q'
				srcPts.insertElementAt(q, i); // insert 'q' at position i in points s.t. 'q' will be the next i
				D = 0.0;
			}
			else
			{
				D += d;
			}
		}
		// somtimes we fall a rounding-error short of adding the last Vector2, so add it if so
		if (dstPts.size() == n - 1)
		{
			dstPts.addElement(srcPts.elementAt(srcPts.size() - 1));
		}

		return dstPts;
	}

	
	public static Vector RotateToZero(Vector points)
	{	return RotateToZero(points, null, null);	}

	
	public static Vector RotateToZero(Vector points, Vector2 centroid, Rectangle boundingBox)
	{
		Vector2 c = Centroid(points);
		Vector2 first = (Vector2)points.elementAt(0);
		double theta =  Math.atan2(c.y - first.y, c.x - first.x);
		
		if (centroid != null)
			centroid.set(c.cpy());

		
		if (boundingBox != null)
			BoundingBox(points, boundingBox);
		
		return RotateBy(points, -theta);
	}		
	
	public static Vector RotateBy(Vector points, double theta)
	{
		return RotateByRadians(points, theta);
	}
	
	// rotate the points by the given radians about their centroid
	public static Vector RotateByRadians(Vector points, double radians)
	{
		Vector newPoints = new Vector(points.size());
		Vector2 c = Centroid(points);

		double _cos = Math.cos(radians);
		double _sin = Math.sin(radians);

		double cx = c.x;
		double cy = c.y;

		for (int i = 0; i < points.size(); i++)
		{
			Vector2 p = (Vector2) points.elementAt(i);

			double dx = p.x - cx;
			double dy = p.y - cy;

			newPoints.addElement(
				new Vector2((float)(dx * _cos - dy * _sin + cx), (float)(dx * _sin + dy * _cos + cy )));
		}
		return newPoints;
	}

	public static Vector ScaleToSquare(Vector points, double size)
	{
		return ScaleToSquare(points, size, null);
	}				

	public static Vector ScaleToSquare(Vector points, double size, Rectangle boundingBox)
	{
		Rectangle B = BoundingBox(points);
		Vector newpoints = new Vector(points.size());
		for (int i = 0; i < points.size(); i++)
		{
			Vector2 p = (Vector2)points.elementAt(i);
			double qx = p.x * (size / B.width);
			double qy = p.y * (size / B.height);
			newpoints.addElement(new Vector2((float)qx,(float) qy));
		}
		
		if (boundingBox != null) //this will probably not be used as we are more interested in the pre-rotated bounding box -> see RotateToZero
			boundingBox.set(B);
		
		return newpoints;
	}			
	
	public static Vector TranslateToOrigin(Vector points)
	{
		Vector2 c = Centroid(points);
		Vector newpoints = new Vector(points.size());
		for (int i = 0; i < points.size(); i++)
		{
			Vector2 p = (Vector2)points.elementAt(i);
			double qx = p.x - c.x;
			double qy = p.y - c.y;
			newpoints.addElement(new Vector2((float)qx, (float)qy));
		}
		return newpoints;
	}			
	
	public static double DistanceAtBestAngle(Vector points, Template T, double a, double b, double threshold)
	{
		double Phi = Recognizer.Phi;
	
		double x1 = Phi * a + (1.0 - Phi) * b;
		double f1 = DistanceAtAngle(points, T, x1);
		double x2 = (1.0 - Phi) * a + Phi * b;
		double f2 = DistanceAtAngle(points, T, x2);
		
		while (Math.abs(b - a) > threshold)
		{
			if (f1 < f2)
			{
				b = x2;
				x2 = x1;
				f2 = f1;
				x1 = Phi * a + (1.0 - Phi) * b;
				f1 = DistanceAtAngle(points, T, x1);
			}
			else
			{
				a = x1;
				x1 = x2;
				f1 = f2;
				x2 = (1.0 - Phi) * a + Phi * b;
				f2 = DistanceAtAngle(points, T, x2);
			}
		}
		return Math.min(f1, f2);
	}			

	public static double DistanceAtAngle(Vector points, Template T, double theta)
	{
		Vector newpoints = RotateBy(points, theta);
		return PathDistance(newpoints, T.Points);
	}		

//	#region Lengths and Rects	
	
	public static Rectangle BoundingBox(Vector points)
	{
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
	
		Enumeration e = points.elements();
		
//		foreach (Vector2 p in points)
		while (e.hasMoreElements())
		{
			Vector2 p = (Vector2)e.nextElement();
		
			if (p.x < minX)
				minX = p.x;
			if (p.x > maxX)
				maxX = p.x;
		
			if (p.y < minY)
				minY = p.y;
			if (p.y > maxY)
				maxY = p.y;
		}
	
		return new Rectangle((float)minX, (float)minY, (float)(maxX - minX), (float)(maxY - minY));
	}

	public static void BoundingBox(Vector points, Rectangle dst)
	{
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
	
		Enumeration e = points.elements();
		
//		foreach (Vector2 p in points)
		while (e.hasMoreElements())
		{
			Vector2 p = (Vector2)e.nextElement();
		
			if (p.x < minX)
				minX = p.x;
			if (p.x > maxX)
				maxX = p.x;
		
			if (p.y < minY)
				minY = p.y;
			if (p.y > maxY)
				maxY = p.y;
		}
	
		dst.x = (float) minX;
		dst.x = (float) minY;
		dst.width = (float) (maxX - minX);
		dst.height = (float) (maxY - minY);
	}	
	
	public static double Distance(Vector2 p1, Vector2 p2)
	{
		double dx = p2.x - p1.x;
		double dy = p2.y - p1.y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	// compute the centroid of the points given
	public static Vector2 Centroid(Vector points)
	{
		double xsum = 0.0;
		double ysum = 0.0;
		
		Enumeration e = points.elements();
		
//		foreach (Vector2 p in points)
		while (e.hasMoreElements())
		{
			Vector2 p = (Vector2)e.nextElement();
			xsum += p.x;
			ysum += p.y;
		}
		return new Vector2((float)(xsum / points.size()),(float)( ysum / points.size()));
	}

	public static double PathLength(Vector points)
	{
		double length = 0;
		for (int i = 1; i < points.size(); i++)
		{
			//length += Distance((Vector2) points[i - 1], (Vector2) points[i]);
			length += Distance((Vector2) points.elementAt(i - 1), (Vector2) points.elementAt(i));
		}
		return length;
	}

	// computes the 'distance' between two Vector2 paths by summing their corresponding Vector2 distances.
	// assumes that each path has been resampled to the same number of points at the same distance apart.
	public static double PathDistance(Vector path1, Vector path2)
	{            
		double distance = 0;
		for (int i = 0; i < path1.size(); i++)
		{
			distance += Distance((Vector2) path1.elementAt(i), (Vector2) path2.elementAt(i));
		}
		return distance / path1.size();
	}

	


}
