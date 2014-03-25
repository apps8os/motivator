package org.apps8os.motivator.data;

public class MotivatorEvent {
	
	private long mStartTime;
	private long mEndTime;
	private int mPlannedDrinks;
	private int mActualDrinks = 0;
	private int mId;
	private String mEventAsText;
	
	public MotivatorEvent(int eventId) {
		mId = eventId;
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
	public void setEventAsText(String mEventAsText) {
		this.mEventAsText = mEventAsText;
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
	public String getEventText() {
		return mEventAsText;
	}

}
