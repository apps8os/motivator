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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents an event in the application. The event can be a checked, unchecked or planned event.
 * @author Toni Järvinen
 *
 */
public class MotivatorEvent implements Parcelable {
	
	public static final String EVENT = "event";
	public static final String YESTERDAYS_EVENTS = "yesterdays_events";
	public static final String EVENT_WITH_DATE = "event_with_date";
	public static final String SECTION = "section";
	
	public static final int HISTORY = 0;
	public static final int TODAY = 1;
	public static final int PLAN = 2;
	
	private long mStartTime = 0;
	private long mEndTime = 0;
	private int mPlannedDrinks;
	private int mId;
	private String mEventDateAsText = "";
	private int mStartTimeAnswer = 0;
	private int mEndTimeAnswer = 0;
	private int mWithWhoAnswer = 0;
	private String mName = "";
	private int mChecked = 0;

	public MotivatorEvent(int eventId) {
		mId = eventId;
	}
	
	public MotivatorEvent(Parcel source) {
		mStartTime = source.readLong();
		mEndTime = source.readLong();
		mPlannedDrinks = source.readInt();
		mId = source.readInt();
		mEventDateAsText = source.readString();
		mStartTimeAnswer = source.readInt();
		mEndTimeAnswer = source.readInt();
		mWithWhoAnswer = source.readInt();
		mName = source.readString();
		mChecked = source.readInt();
	}

	public static final Parcelable.Creator<MotivatorEvent> CREATOR = new Parcelable.Creator<MotivatorEvent>() {

		@Override
		public MotivatorEvent createFromParcel(Parcel source) {
			return new MotivatorEvent(source);
		}

		@Override
		public MotivatorEvent[] newArray(int size) {
			return new MotivatorEvent[size];
		}
		
	};
	
	/**
	 * @return the mWithWho
	 */
	public String getWithWho(Context context) {
		if (mWithWhoAnswer == 0) {
			return "";
		}
		EventDataHandler dataHandler = new EventDataHandler(context);
		return dataHandler.getQuestion(EventDataHandler.QUESTION_ID_WITH_WHO).getAnswer(mWithWhoAnswer);
	}
	
	/**
	 * @param mWithWho the mWithWho to set
	 */
	public void setWithWhoAnswer(int answer) {
		mWithWhoAnswer = answer;
	}
	
	public int getWithWhoAnswer() {
		return mWithWhoAnswer;
	}
	
	/**
	 * @param mStartTime the mStartTime to set
	 */
	public void setStartTime(long mStartTime) {
		this.mStartTime = mStartTime;
	}

	/**
	 * @param mEndTime the mEndTime to set
	 */
	public void setEndTime(long mEndTime) {
		this.mEndTime = mEndTime;
	}

	/**
	 * @param mPlannedDrinks the mPlannedDrinks to set
	 */
	public void setPlannedDrinks(int mPlannedDrinks) {
		this.mPlannedDrinks = mPlannedDrinks;
	}

	/**
	 * @param mEventAsText the mEventAsText to set
	 */
	public void setEventDateAsText(String mEventAsText) {
		this.mEventDateAsText = mEventAsText;
	}
	

	/**
	 * @return the mStartTime
	 */
	public long getStartTime() {
		return mStartTime;
	}

	/**
	 * @return the mEndTime
	 */
	public long getEndTime() {
		return mEndTime;
	}

	/**
	 * @return the mPlannedDrinks
	 */
	public int getPlannedDrinks() {
		return mPlannedDrinks;
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public String getPlannedDrinksAsText(Context context) {
		EventDataHandler dataHandler = new EventDataHandler(context);
		return dataHandler.getQuestion(EventDataHandler.QUESTION_ID_HOW_MUCH).getAnswer(mPlannedDrinks + 1);
	}


	/**
	 * @return the mId
	 */
	public int getId() {
		return mId;
	}
	
	/**
	 * @return the mEventAsText
	 */
	public String getEventDateAsText() {
		return mEventDateAsText;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mStartTime);
		dest.writeLong(mEndTime);
		dest.writeInt(mPlannedDrinks);
		dest.writeInt(mId);
		dest.writeString(mEventDateAsText);
		dest.writeInt(mStartTimeAnswer);
		dest.writeInt(mEndTimeAnswer);
		dest.writeInt(mWithWhoAnswer);
		dest.writeString(mName);
		dest.writeInt(mChecked);
	}

	/**
	 * 
	 * @param context
	 * @return
	 */
	public String getStartTimeAsText(Context context) {
		if (mStartTimeAnswer == 0) {
			return "";
		}
		EventDataHandler dataHandler = new EventDataHandler(context);
		return dataHandler.getQuestion(EventDataHandler.QUESTION_ID_TIME_TO_GO).getAnswer(mStartTimeAnswer);
	}
	
	/**
	 * 
	 * @param answer
	 */
	public void setStartTimeAnswer(int answer) {
		mStartTimeAnswer = answer;
	}
	
	public int getStartTimeAnswer() {
		return mStartTimeAnswer;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	/**
	 * Checks if this instance is an checked event instance.
	 * @return
	 */
	public boolean hasBeenChecked() {
		return (mChecked == 1);
	}

	public void setChecked() {
		mChecked = 1;
	}
	
	public void setEndTimeAnswer(int answer) {
		mEndTimeAnswer = answer;
	}
	
	public int getEndTimeAnswer() {
		return mEndTimeAnswer;
	}
	
	/**
	 * Update the event to database.
	 * @param context
	 */
	public void updateToDatabase(Context context) {
		EventDataHandler dataHandler = new EventDataHandler(context);
		dataHandler.updateEvent(mId, mStartTimeAnswer, mEndTimeAnswer, mWithWhoAnswer, mPlannedDrinks + 1, mStartTime, mName);
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public String getEndTimeAsText(Context context) {
		if (mEndTimeAnswer == 0) {
			return "";
		}
		EventDataHandler dataHandler = new EventDataHandler(context);
		return dataHandler.getQuestion(EventDataHandler.QUESTION_ID_TIME_TO_COME_BACK).getAnswer(mEndTimeAnswer);
	}

}
