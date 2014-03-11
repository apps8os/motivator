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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apps8os.motivator.utils.UtilityMethods;

import android.content.Context;

/**
 * Represents a day in the users history. The definition which belongs to a certain day is done
 * by the using class.
 * @author Toni Järvinen
 *
 */
public class DayInHistory {
	
	private int mMoodSum = 0;
	private int mMoodAmount = 0;
	private int mEnergySum = 0;
	private int mEnergyAmount = 0;
	private int mAlcoholDrinks = 0;
	private String mComment;
	private Calendar mDate;
	/**
	 * Create an instance. Here dayInMillis is the midnight of the day represented by this instance.
	 * @param dayInMillis
	 */
	public DayInHistory(long dayInMillis) {
		mDate = new GregorianCalendar();
		mDate.setTimeInMillis(dayInMillis);
		mDate = UtilityMethods.setToMidnight(mDate);
	}
	
	public void addMoodLevel(int moodLevel) {
		mMoodAmount += 1;
		mMoodSum += moodLevel;
	}
	
	public void addEnergyLevel(int moodLevel) {
		mEnergyAmount += 1;
		mEnergySum += moodLevel;
	}
	
	public void addAlcoholDrink(int amount) {
		mAlcoholDrinks += 1;
	}
	
	public void addComment(String comment) {
		mComment = comment;
	}

	/**
	 * @return the mAvgMoodLevel
	 */
	public int getAvgMoodLevel() {
		return mMoodSum / mMoodAmount;
	}

	/**
	 * @return the mAvgEnergyLevel
	 */
	public int getAvgEnergyLevel() {
		return mEnergySum / mEnergyAmount;
	}

	/**
	 * @return the mAlcoholDrinks
	 */
	public int getAlcoholDrinks() {
		return mAlcoholDrinks;
	}

	/**
	 * @return the mComment
	 */
	public String getComment() {
		return mComment;
	}

	/**
	 * @return the current date with a legible format
	 */
	public String getDateInString(Context context) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", context.getResources().getConfiguration().locale);
		return sdf.format(mDate.getTime());
	}
	
	/**
	 * Gets the date in milliseconds.
	 * @return
	 */
	public long getDateInMillis() {
		return mDate.getTimeInMillis();
	}
	
	public Calendar getDate() {
		return mDate;
	}
}
