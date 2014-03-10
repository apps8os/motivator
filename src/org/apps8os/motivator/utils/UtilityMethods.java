/*******************************************************************************
 * Copyright (c) 2014 Helsingin Diakonissalaitos and the authors
 * 
 * The MIT License (MIT)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO 
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS 
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.apps8os.motivator.utils;

import java.util.Calendar;


/**
 * Holds a set of static methods used in different classes. Not meant to be instanced.
 * @author Toni Järvinen
 *
 */
public final class UtilityMethods {
	
	/**
	 * Private empty constructor to make sure instancing this class induces an error.
	 */
	private UtilityMethods() {
	}
	
	/**
	 * Sets the calendar to midnight.
	 * @param calendar
	 * @return
	 */
	public static Calendar setToMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
	
	/**
	 * Returns the day boundaries on a 2 long millisecond values. Effectively the times 00:00:00 and 23:59:59
	 * Returns array with earlier boundary as first value and later second. NOTE: This also sets the original
	 * calendar instance to 1 23:59:59!
	 * @param calendar
	 * @return
	 */
	public static long[] getDayInMillis(Calendar calendar) {
		UtilityMethods.setToMidnight(calendar);
		long earlierBoundary = calendar.getTimeInMillis();
    	calendar.add(Calendar.DATE, 1);
    	long laterBoundary = calendar.getTimeInMillis();
    	long result[] = {earlierBoundary, laterBoundary};
    	return result;
	}

}
