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
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a day in the users history. Implements parcelable for transfering the instance
 * between activities/fragments.
 * @author Toni Järvinen
 *
 */
public class DayInHistory implements Parcelable{
	
	private int mMoodSum = 0;
	private int mMoodAmount = 0;
	private int mEnergySum = 0;
	private int mEnergyAmount = 0;
	private int mAlcoholDrinks = 0;
	private String mComment;
	private long mDateInMillis;
	/**
	 * Create an instance. Set the date in millis to midnight of the day.
	 * @param dayInMillis
	 */
	public DayInHistory(long dayInMillis) {
		Calendar mDate = new GregorianCalendar();
		mDate.setTimeInMillis(dayInMillis);
		mDate = UtilityMethods.setToMidnight(mDate);
		mDateInMillis = mDate.getTimeInMillis();
	}
	
	/**
	 * Reconstructs the instance from the parcel.
	 * @param source
	 */
	private DayInHistory(Parcel source) {
		mMoodSum = source.readInt();
		mMoodAmount = source.readInt();
		mEnergyAmount = source.readInt();
		mEnergySum = source.readInt();
		mAlcoholDrinks = source.readInt();
		mComment = source.readString();
		mDateInMillis = source.readLong();
	}
	
	/**
	 * Creator instance for the parcelable interface.
	 */
	public static final Parcelable.Creator<DayInHistory> CREATOR = new Parcelable.Creator<DayInHistory>() {
		@Override
		public DayInHistory createFromParcel(Parcel source) {
			return new DayInHistory(source);
		}

		@Override
		public DayInHistory[] newArray(int size) {
			return new DayInHistory[size];
		}
	};
	
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
		if (mMoodAmount > 0) {
			return mMoodSum / mMoodAmount;
		} else {
			return 0;
		}
		
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
		Calendar date = new GregorianCalendar();
		date.setTimeInMillis(mDateInMillis);
		return sdf.format(date.getTime());
	}
	
	/**
	 * Gets the date in milliseconds.
	 * @return
	 */
	public long getDateInMillis() {
		return mDateInMillis;
	}
	

	@Override
	public int describeContents() {
		return hashCode();
	}

	/**
	 * Writing to the parcel.
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mMoodSum);
		dest.writeInt(mMoodAmount);
		dest.writeInt(mEnergyAmount);
		dest.writeInt(mEnergySum);
		dest.writeInt(mAlcoholDrinks);
		dest.writeString(mComment);
		dest.writeLong(mDateInMillis);
	}
}
