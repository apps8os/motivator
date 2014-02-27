package org.apps8os.motivator.utils;

import java.util.Calendar;


/**
 * Holds a set of static methods used in different classes.
 * @author Toni Järvinen
 *
 */
public final class UtilityMethods {
	
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

}
