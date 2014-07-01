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
import java.util.Collections;
import java.util.Comparator;

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
	

	public static final String DAY_IN_HISTORY = "day_in_history";
	public static final String DAY_IN_HISTORY_ARRAY = "day_in_history_array";
	
	private int mAlcoholDrinks = 0;
	private int mPlannedDrinks = 0;
	private long mDateInMillis;
	private ArrayList<MotivatorEvent> mEvents = new ArrayList<MotivatorEvent>();
	private ArrayList<Mood> mMoods = new ArrayList<Mood>();
	private Context mContext;
	
	public static final int AMOUNT_OF_DRINKS = 1;
	public static final int MOODS = 2;
	public static final int ALL = 3;
	
	/**
	 * Create an instance. Set the date in millis to midnight of the day.
	 * @param dayInMillis
	 */
	public DayInHistory(long dayInMillis, Context context) {
		Calendar mDate = Calendar.getInstance();
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
		mPlannedDrinks = source.readInt();
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
	
	/**
	 * Sets alcohol drinks.
	 * @param amount
	 */
	public void setAlcoholDrinks(int amount) {
		mAlcoholDrinks = amount;
	}
	
	/**
	 * Adds a mood the the day and sorts the list according to the timestamps.
	 * @param mood
	 */
	public void addMood(Mood mood) {
		mMoods.add(mood);
		
		Collections.sort(mMoods, new Comparator<Mood>() {
			@Override
			public int compare(Mood case1, Mood case2) {
				long case1Sorter = case1.getTimestamp();
				long case2Sorter = case2.getTimestamp();
				if (case1Sorter > case2Sorter) {
					return 1;
				} else if (case1Sorter == case2Sorter) {
					return 0;
				} else {
					return -1;
				}
			}
		});
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
	
	/**
	 * 
	 * @return the first recorded mood of the day
	 */
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
	public int getAlcoholDrinks(Context context) {
		DayDataHandler dataHandler = new DayDataHandler(context);
		return dataHandler.getClickedDrinksForDay(mDateInMillis);
	}
	
	/**
	 * Gets the planned alcohol drinks for the day.
	 * @param context
	 * @return
	 */
	public int getPlannedAlcoholDrinks() {
		int events = mEvents.size();
		int totalPlannedDrinks = 0;
		for (int i = 0; i < events; i++) {
			totalPlannedDrinks = totalPlannedDrinks + mEvents.get(i).getPlannedDrinks();
		}
		return totalPlannedDrinks;
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
		dest.writeInt(mPlannedDrinks);
		dest.writeLong(mDateInMillis);
		dest.writeTypedList(mEvents);
		dest.writeTypedList(mMoods);
	}

	/**
	 * All events for the day. Needs to have setEvents() called before.
	 * @return
	 */
	public ArrayList<MotivatorEvent> getEvents() {
		return mEvents;
	}
	
	/**
	 * Gets the events that have not been checked.
	 * @param context
	 * @return
	 */
	public ArrayList<MotivatorEvent> getUncheckedEvents(Context context) {
		ArrayList<MotivatorEvent> result = new ArrayList<MotivatorEvent>();
		EventDataHandler eventData = new EventDataHandler(mContext);
		MotivatorEvent checkedEvent;
		for (MotivatorEvent event : mEvents) {
			checkedEvent = eventData.getCheckedEvent(event.getId());
			if (checkedEvent == null) {
				result.add(event);
			}
		}		
		return result;
	}
	
	/**
	 * Gets the MotivatorEvent instances representing the plans.
	 * @param context
	 * @return
	 */
	public ArrayList<MotivatorEvent> getPlannedEvents(Context context) {
		ArrayList<MotivatorEvent> result = new ArrayList<MotivatorEvent>();
		for (MotivatorEvent event : mEvents) {
			if (!event.hasBeenChecked()) {
				result.add(event);
			}
		}		
		return result;
	}
	
	/**
	 * Gets the MotivatorEvent instances representing events that were checked/realized.
	 * @param context
	 * @return
	 */
	public ArrayList<MotivatorEvent> getCheckedEvents(Context context) {
		ArrayList<MotivatorEvent> result = new ArrayList<MotivatorEvent>();
		EventDataHandler eventData = new EventDataHandler(mContext);
		MotivatorEvent checkedEvent;
		for (MotivatorEvent event : mEvents) {
			checkedEvent = eventData.getCheckedEvent(event.getId());
			if (checkedEvent != null) {
				result.add(checkedEvent);
			}
		}		
		return result;
	}
	
	/**
	 * 
	 * @return all mooods for the day
	 */
	public ArrayList<Mood> getMoods() {
		return mMoods;
	}

	/**
	 * Gets the events for the day and stores them in the DayInHistory obejct.
	 */
	public void setEvents() {
		EventDataHandler eventData = new EventDataHandler(mContext);
		mEvents = eventData.getUncheckedEventsForDay(mDateInMillis);
	}
}
