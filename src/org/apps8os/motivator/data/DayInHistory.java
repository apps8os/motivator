package org.apps8os.motivator.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;

/**
 * Represents a day in the users history. The definition which belongs to a certain day is done
 * by the using class.
 * @author Toni Järvinen
 *
 */
public class DayInHistory {
	
	private int mMoodSum;
	private int mMoodAmount;
	private int mEnergySum;
	private int mEnergyAmount;
	private int mAlcoholDrinks;
	private String mComment;
	private Calendar mDate;
	/**
	 * Create an instance. Here dayInMillis is usually the earliest timestamp of a certain day and it
	 * comes from a cursor over a database.
	 * @param dayInMillis
	 */
	public DayInHistory(long dayInMillis) {
		mDate = new GregorianCalendar();
		mDate.setTimeInMillis(dayInMillis);
	}
	
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
		return mMoodSum / mMoodAmount;
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
		return sdf.format(mDate.getTime());
	}
	
	
	public Calendar getDate() {
		return mDate;
	}
}
