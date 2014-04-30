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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

/**
 * Represents a day in the users history. Implements parcelable for transfering the instance
 * between activities/fragments.
 * @author Toni Järvinen
 *
 */
public class DayInHistory implements Parcelable{
	

	public static final String DAY_IN_HISTORY = "day_in_history";
	public static final String DAY_IN_HISTORY_ARRAY = "day_in_history_array";
	
	private int mAlcoholDrinks = 0;
	private long mDateInMillis;
	private ArrayList<MotivatorEvent> mEvents = new ArrayList<MotivatorEvent>();
	private ArrayList<Mood> mMoods = new ArrayList<Mood>();
	private Context mContext;
	
	public static final int AMOUNT_OF_DRINKS = 1;
	public static final int ALL = 2;
	
	/**
	 * Create an instance. Set the date in millis to midnight of the day.
	 * @param dayInMillis
	 */
	public DayInHistory(long dayInMillis, Context context) {
		Calendar mDate = new GregorianCalendar();
		mDate.setTimeInMillis(dayInMillis);
		mDate = UtilityMethods.setToDayStart(mDate);
		mDateInMillis = mDate.getTimeInMillis();
		mContext = context;
	}
	
	/**
	 * Reconstructs the instance from the parcel.
	 * @param source
	 */
	private DayInHistory(Parcel source) {
		mAlcoholDrinks = source.readInt();
		mDateInMillis = source.readLong();
		source.readTypedList(mEvents, MotivatorEvent.CREATOR);
		source.readTypedList(mMoods, Mood.CREATOR);
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
	
	public void addAlcoholDrink(int amount) {
		mAlcoholDrinks += 1;
	}
	
	public void addMood(Mood mood) {
		mMoods.add(mood);
	}

	/*
	 * @return the mAvgMoodLevel
	 */
	public int getAvgMoodLevel() {
		int avgMood = 0;
		for (int i = 0; i < mMoods.size(); i++) {
			avgMood += mMoods.get(i).getMood();
		}
		if (mMoods.size() > 0) {
			avgMood = avgMood / mMoods.size();
		}
		return avgMood;
	}

	/**
	 * @return the mAvgEnergyLevel
	 */
	public int getAvgEnergyLevel() {
		int avgEnergy = 0;
		for (int i = 0; i < mMoods.size(); i++) {
			avgEnergy += mMoods.get(i).getEnergy();
		}
		if (mMoods.size() > 0) {
			avgEnergy = avgEnergy / mMoods.size();
		}
		return avgEnergy;
	}
	
	public Mood getFirstMoodOfTheDay() {
		if (mMoods.size() > 0) {
			return mMoods.get(0);
		} else {
			return new Mood(0,0, System.currentTimeMillis(), DayDataHandler.NO_COMMENT);
		}
	}


	/**
	 * @return the mAlcoholDrinks
	 */
	public int getAlcoholDrinks() {
		return mAlcoholDrinks;
	}


	/**
	 * @return the current date with a legible format
	 */
	public String getDateInString(Context context) {
		return UtilityMethods.getDateAsString(mDateInMillis, context);
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
		dest.writeInt(mAlcoholDrinks);
		dest.writeLong(mDateInMillis);
		dest.writeTypedList(mEvents);
		dest.writeTypedList(mMoods);
	}

	public ArrayList<MotivatorEvent> getEvents() {
		return mEvents;
	}
	
	public ArrayList<MotivatorEvent> getUncheckedEvents() {
		SparseArray<MotivatorEvent> uncheckedEvents = new SparseArray<MotivatorEvent>();
		int arraySize = mEvents.size();
		for (int i = 0; i < arraySize; i++) {
			MotivatorEvent event = mEvents.get(i);
			if  (event.hasBeenChecked() && uncheckedEvents.get(event.getId()) != null) {
				uncheckedEvents.remove(event.getId());
			} else if (!event.hasBeenChecked()){
				uncheckedEvents.put(event.getId(), event);
			}
		}
		arraySize = uncheckedEvents.size();
		ArrayList<MotivatorEvent> result = new ArrayList<MotivatorEvent>();
		for (int i = 0; i < arraySize; i++) {
			result.add(uncheckedEvents.valueAt(i));
		}
		
		return result;
	}
	
	public ArrayList<Mood> getMoods() {
		return mMoods;
	}

	public void setEvents() {
		EventDataHandler eventData = new EventDataHandler(mContext);
		mEvents = eventData.getEventsForDay(mDateInMillis);
	}
}
