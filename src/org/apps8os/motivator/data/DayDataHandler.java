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
package org.apps8os.motivator.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apps8os.motivator.utils.UtilityMethods;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * Handles the access for the "Day" data. Mainly focuses on reading the mood data and
 * creating DayInHistory objects.
 * @author Toni Järvinen
 *
 */
public class DayDataHandler extends MotivatorDatabaseHelper {
	
	public static final String NO_COMMENT = "";
	
	private static final String TABLE_NAME_MOOD = TABLE_NAME_MOOD_LEVELS;
	private Context mContext;
	
	public DayDataHandler(Context context) {
		super(context);
		mContext = context;
	}
	
    /**
     * Inserting a mood to the database
     * @param energyLevel
     * @param moodLevel
     */
    public void insertMood(int energyLevel, int moodLevel) {
    	open();
    	ContentValues values = new ContentValues();
    	values.put(KEY_ENERGYLEVEL, energyLevel);
    	values.put(KEY_MOODLEVEL, moodLevel);
    	values.put(KEY_TIMESTAMP, System.currentTimeMillis());
    	mDb.insert(TABLE_NAME_MOOD, null, values);
    	close();
    }
    
    /**
     * Inserting a mood to the database with a comment
     * @param energyLevel
     * @param moodLevel
     */
    public void insertMood(int energyLevel, int moodLevel, String comment) {
    	open();
    	ContentValues values = new ContentValues();
    	values.put(KEY_ENERGYLEVEL, energyLevel);
    	values.put(KEY_MOODLEVEL, moodLevel);
    	values.put(KEY_CONTENT, comment);
    	values.put(KEY_TIMESTAMP, System.currentTimeMillis());
    	mDb.insert(TABLE_NAME_MOOD, null, values);
    	close();
    }
    
    /**
     * Used to get a DayInHistory representing the day given as milliseconds.
     * @param dayInMillis
     * @return
     */
    public DayInHistory getDayInHistory(long dayInMillis) {
    	open();
    	Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dayInMillis);
    	Cursor query = null;
    	long[] boundaries = UtilityMethods.getDayInMillis(calendar);
    	
    	String selection = KEY_TIMESTAMP + " < " + boundaries[1] + " AND " + KEY_TIMESTAMP +  " >= " + boundaries[0];
    	String columns[] = {KEY_MOODLEVEL, KEY_ENERGYLEVEL, KEY_TIMESTAMP, KEY_CONTENT};
    	query = mDb.query(TABLE_NAME_MOOD, columns, selection, null, null, null, null);
    	
    	query.moveToFirst();
    	// Initialize the DayInHistory object with the first timestamp on the constructor.
	    DayInHistory result = new DayInHistory(dayInMillis, mContext);
	    // Add all moods to the DayInHistory object.
	    int queryCount = query.getCount();
	    while (queryCount > 0 && !query.isClosed()) {
	    	Mood mood = new Mood(query.getInt(0), query.getInt(1), query.getLong(2), query.getString(3));
	    	result.addMood(mood);
	    	if (query.isLast()) {
	    		query.close();
	    	} else {
	    		query.moveToNext();
	    	}
	    }
	    if (!query.isClosed()) {
	    	query.close();
	    }
	    columns = new String[1];
	    columns[0] = KEY_TIMESTAMP;
	    query = mDb.query(TABLE_NAME_DRINKS, columns, selection, null, null, null, null);
	    result.setAlcoholDrinks(query.getCount());
	    close();
	    query.close();
	    return result;
    }
    
    /**
     * Used to get a day in history that has recorded moods.
     * @param dayInMillis
     * @return DayInHistory object, if the day has no recorded moods, returns null
     */
    public DayInHistory getDayInHistoryWithMoods(long dayInMillis) {
    	open();
    	Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dayInMillis);
    	Cursor query = null;
    	long[] boundaries = UtilityMethods.getDayInMillis(calendar);
    	
    	String selection = KEY_TIMESTAMP + " < " + boundaries[1] + " AND " + KEY_TIMESTAMP +  " >= " + boundaries[0];
    	String columns[] = {KEY_MOODLEVEL, KEY_ENERGYLEVEL, KEY_TIMESTAMP, KEY_CONTENT};
    	query = mDb.query(TABLE_NAME_MOOD, columns, selection, null, null, null, null);
    	
    	query.moveToFirst();
    	// Initialize the DayInHistory object with the first timestamp on the constructor.
	    DayInHistory result = new DayInHistory(dayInMillis, mContext);
	    // Add all moods to the DayInHistory object.
	    int queryCount = query.getCount();
	    while (queryCount > 0 && !query.isClosed()) {
	    	Mood mood = new Mood(query.getInt(0), query.getInt(1), query.getLong(2), query.getString(3));
	    	result.addMood(mood);
	    	if (query.isLast()) {
	    		query.close();
	    	} else {
	    		query.moveToNext();
	    	}
	    }
	    if (!query.isClosed()) {
	    	query.close();
	    }
	    columns = new String[1];
	    columns[0] = KEY_TIMESTAMP;
	    query = mDb.query(TABLE_NAME_DRINKS, columns, selection, null, null, null, null);
	    result.setAlcoholDrinks(query.getCount());
	    close();
	    query.close();
	    if (result.getMoods().size() == 0) {
	    	return null;
	    }
	    return result;
    }
    
    
    /**
     * Gets the amount of clicked drinks for the day given in millis from the drink table.
     * @param dayInMillis
     * @return
     */
    public int getClickedDrinksForDay(long dayInMillis) {
    	open();
    	Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dayInMillis);
    	Cursor query = null;
    	long[] boundaries = UtilityMethods.getDayInMillis(calendar);
    	
    	String selection = KEY_TIMESTAMP + " < " + boundaries[1] + " AND " + KEY_TIMESTAMP +  " >= " + boundaries[0];
    	String columns[] = {KEY_TIMESTAMP};
    	query = mDb.query(TABLE_NAME_DRINKS, columns, selection, null, null, null, null);
    	int result = query.getCount();
    	close();
 	    return result;
    }
    
    /**
     * Gets the timestamps for the drinks between a given timeframe
     * @param fromMillis
     * @param toMillis
     * @return
     */
    public long[] getTimestampsForDrinksBetween(long fromMillis, long toMillis) {
    	open();
    	String selection = KEY_TIMESTAMP + " >= " + fromMillis + " AND " + KEY_TIMESTAMP + " < " + toMillis;
    	String columns[] = {KEY_TIMESTAMP};
    	Cursor query = mDb.query(TABLE_NAME_DRINKS, columns, selection, null, null, null, KEY_TIMESTAMP);
    	int count = query.getCount();
    	long[] result = new long[count];
    	if (query.moveToFirst()) {
    		for (int i = 0; i < count; i++) {
    			result[i] = query.getLong(0);
    			query.moveToNext();
    		}
    	}
    	query.close();
    	close();
    	return result;
    }
    
    /**
     * Used to get an array of days from given timestamp.
     * @param fromInMillis from timestamp
     * @param amountOfDays how many days
     * @return
     */
    public DayInHistory[] getDaysAfter(long fromInMillis, int amountOfDays) {
    	DayInHistory[] days = new DayInHistory[amountOfDays];
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(fromInMillis);
    	for (int i = 0; i < amountOfDays; i++) {
    		days[i] = getDayInHistory(calendar.getTimeInMillis());
    		calendar.add(Calendar.DATE, 1);
    	}
    	return days;
    }
    
    /**
     * Gets the given amount of days from the given timestamp.
     * @param fromInMillis
     * @param amountOfDays
     * @return
     */
    public ArrayList<DayInHistory> getDaysWithMoodsAfter(long fromInMillis, int amountOfDays) {
    	ArrayList<DayInHistory> days = new ArrayList<DayInHistory>();
    	Calendar calendar = Calendar.getInstance();
    	DayInHistory day;
    	calendar.setTimeInMillis(fromInMillis);
    	for (int i = 0; i < amountOfDays; i++) {
    		day = getDayInHistoryWithMoods(calendar.getTimeInMillis());
    		if (day != null) {
    			days.add(day);
    		}
    		calendar.add(Calendar.DATE, 1);
    	}
    	return days;
    }
    
	/**
	 * Adds a drink with the current time.
	 * @param answerId
	 */
	public void addDrink() {
		open();
		ContentValues values = new ContentValues();
		values.put(KEY_TIMESTAMP, System.currentTimeMillis());
		mDb.insert(TABLE_NAME_DRINKS, null, values);
		close();
	}
	
	/**
	 * Adds a drink with the given timestamp.
	 * @param answerId
	 */
	public void addDrink(long timestamp) {
		open();
		ContentValues values = new ContentValues();
		values.put(KEY_TIMESTAMP, timestamp);
		mDb.insert(TABLE_NAME_DRINKS, null, values);
		close();
	}
	
	/**
	 * Sets the daily total drinks. This is meant to be used when checking events.
	 * CAUTION: There are two different places
	 * for the drinks, the added drinks with the clicker and the checked. This gets the checked total drinks.
	 * @param amount
	 * @param timestamp
	 */
	public void insertDailyDrinkAmount(int amount, long timestamp) {
		open();
		ContentValues values = new ContentValues();
		values.put(KEY_TIMESTAMP, timestamp);
		values.put(KEY_TOTAL_DAILY_DRINKS, amount);
		mDb.insert(TABLE_NAME_TOTAL_DRINKS, null, values);
		close();
	}
	
	/**
	 * Getting the checked drinks from the daily drinks database. CAUTION: There are two different places
	 * for the drinks, the added drinks with the clicker and the checked. This gets the checked total drinks.
	 * @param timestamp
	 * @return
	 */
	public int getDailyDrinkAmount(long timestamp) {
		open();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		long dayBoundaries[] = UtilityMethods.getDayInMillis(calendar);
		String selection = KEY_TIMESTAMP + " > " + dayBoundaries[0] + " AND " + KEY_TIMESTAMP + " <= " + dayBoundaries[1];
		String columns[] = {KEY_TOTAL_DAILY_DRINKS};
		Cursor query = mDb.query(TABLE_NAME_TOTAL_DRINKS, columns, selection, null, null, null, null);
		int result = -1;
		if (query.moveToFirst()) {
			result = query.getInt(0);
		}
		query.close();
		close();
		return result;
	}
	
	/**
	 * Adds a drink to the event with given id.
	 * @param answerId
	 */
	public void removeDrink() {
		open();
		
		super.deleteLastRow(TABLE_NAME_DRINKS);
		close();
	}
	
}
