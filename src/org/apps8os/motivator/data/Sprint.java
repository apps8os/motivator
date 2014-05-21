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
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class Sprint implements Parcelable {
	
	public static final String CURRENT_SPRINT = "current_sprint";
	public static final String CURRENT_SPRINT_STARTDATE = "current_sprint_start_date";
	public static final String FIRST_SPRINT_SET = "first_sprint_set";
	
	private long mStartTime;
	private long mEndTime;
	private int mDaysInSprint;
	private String mSprintTitle;
	private int mId;
	
	public Sprint(long startTime) {
		mStartTime = startTime;
	}
	
	/**
	 * @return The running number of today in the sprint, the amount of days if the sprint has ended.
	 */
	public int getCurrentDayOfTheSprint() {
		if (System.currentTimeMillis() > mEndTime) {
			return mDaysInSprint;
		}
		long timeDif = System.currentTimeMillis() - mStartTime;
		return (int) TimeUnit.DAYS.convert(timeDif, TimeUnit.MILLISECONDS) + 1;
	}
	
	private Sprint(Parcel source) {
		mStartTime = source.readLong();
		mEndTime = source.readLong();
		mDaysInSprint = source.readInt();
		mSprintTitle = source.readString();
		mId = source.readInt();
	}
	public static final Parcelable.Creator<Sprint> CREATOR = new Parcelable.Creator<Sprint>() {
		@Override
		public Sprint createFromParcel(Parcel source) {
			return new Sprint(source);
		}
		@Override
		public Sprint[] newArray(int size) {
			return new Sprint[size];
		}
	};

	/**
	 * @return the mStartTime
	 */
	public long getStartTime() {
		return mStartTime;
	}

	/**
	 * @param mStartTime the mStartTime to set
	 */
	public void setStartTime(long mStartTime) {
		this.mStartTime = mStartTime;
	}
	
	public String getStartTimeInString(Context context) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", context.getResources().getConfiguration().locale);
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(mStartTime);
		return sdf.format(date.getTime());
	}

	/**
	 * @return the mEndTime
	 */
	public long getEndTime() {
		return mEndTime;
	}

	/**
	 * @param mEndTime the mEndTime to set
	 */
	public void setEndTime(long mEndTime) {
		this.mEndTime = mEndTime;
	}

	/**
	 * @return the mDaysInSprint
	 */
	public int getDaysInSprint() {
		return (int) TimeUnit.DAYS.convert(mEndTime - mStartTime, TimeUnit.MILLISECONDS);
	}
	
	public boolean isOver() {
		return (mEndTime < System.currentTimeMillis());
	}

	/**
	 * @param mDaysInSprint the mDaysInSprint to set
	 */
	public void setDaysInSprint(int mDaysInSprint) {
		this.mDaysInSprint = mDaysInSprint;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(mStartTime);
		dest.writeLong(mEndTime);
		dest.writeInt(mDaysInSprint);
		dest.writeString(mSprintTitle);
		dest.writeInt(mId);
	}

	public String getSprintTitle() {
		return mSprintTitle;
	}

	public void setSprintTitle(String mSprintTitle) {
		this.mSprintTitle = mSprintTitle;
	}

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
	}
	
	

}
