package org.apps8os.motivator.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

public class Sprint implements Parcelable {
	
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
		Calendar date = new GregorianCalendar();
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
		return (int) TimeUnit.DAYS.convert(mEndTime - mStartTime, TimeUnit.MILLISECONDS) + 1;
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
