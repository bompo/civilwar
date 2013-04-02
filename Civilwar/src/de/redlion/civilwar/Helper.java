package de.redlion.civilwar;

public class Helper {
	
	public static float map(float value, float fromLow, float fromHigh, float toLow, float toHigh) {
		return toLow + (toHigh - toLow) * ( ( value - fromLow)/ ( fromHigh - fromLow) );
	}		 
	
}
