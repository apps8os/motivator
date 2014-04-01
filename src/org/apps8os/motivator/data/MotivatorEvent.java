package org.apps8os.motivator.data;

import android.os.Parcel;
import android.os.Parcelable;

public class MotivatorEvent implements Parcelable {
	
	private long mStartTime;
	private long mEndTime;
	private int mPlannedDrinks;
	private int mActualDrinks = 0;
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
		mActualDrinks = source.readInt();
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
	 * Adds a drink.
	 */
	public void addDrink() {
		mActualDrinks += 1;
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
	 * @return the mActualDrinks
	 */
	public int getActualDrinks() {
		return mActualDrinks;
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
		dest.writeInt(mActualDrinks);
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
