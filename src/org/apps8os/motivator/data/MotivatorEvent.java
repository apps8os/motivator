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

import java.util.GregorianCalendar;

import android.os.Parcel;
import android.os.Parcelable;

public class MotivatorEvent implements Parcelable {
	
	public static final String EVENT = "event";
	public static final int EVENT_CHECKED_ID = 1901;
	
	private long mStartTime;
	private long mEndTime;
	private int mPlannedDrinks;
	private int mId;
	private String mEventDateAsText;
	private String mStartTimeAsText = "NOT FOUND";
	
	public MotivatorEvent(int eventId) {
		mId = eventId;
	}
	
	public MotivatorEvent(Parcel source) {
		mStartTime = source.readLong();
		mEndTime = source.readLong();
		mPlannedDrinks = source.readInt();
		mId = source.readInt();
		mEventDateAsText = source.readString();
		mStartTimeAsText = source.readString();
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
		dest.writeString(mStartTimeAsText);
	}

	public String getStartTimeAsText() {
		return mStartTimeAsText;
	}

	public void setStartTimeAsText(String mStartTimeAsText) {
		this.mStartTimeAsText = mStartTimeAsText;
	}

}
