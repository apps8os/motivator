package org.apps8os.motivator.data;

import java.util.concurrent.TimeUnit;

import android.os.Parcel;
import android.os.Parcelable;

public class Sprint implements Parcelable {
	
	private long mStartTime;
	private long mEndTime;
	private int mDaysInSprint;
	
	public Sprint(long startTime) {
		mStartTime = startTime;
	}
	
	public int getCurrentDayOfTheSprint() {
		if (System.currentTimeMillis() > mEndTime) {
			return -1;
		}
		long timeDif = System.currentTimeMillis() - mStartTime;
		return (int) TimeUnit.DAYS.convert(timeDif, TimeUnit.MILLISECONDS) + 1;
	}
	
	private Sprint(Parcel source) {
		mStartTime = source.readLong();
		mEndTime = source.readLong();
		mDaysInSprint = source.readInt();
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
		return mDaysInSprint;
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
	}
	
	

}
