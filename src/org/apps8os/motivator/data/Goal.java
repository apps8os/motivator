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

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import android.content.Context;

/**
 * Represents a Goal. Not yet implemented in the application.
 * @author Toni Järvinen
 *
 */
public class Goal {
	
	private long mStartTime = 0;
	private int mDays;
	private long mEndTime = 0;
	private int mGoalId;
	private String mGoalAsText;
	private int mId;
	private int mTargetAmount;
	private int mCurrentAmount;
	private int mGoalStatus;
	
	public Goal(int id, long startTime, int days, long endTime, int goalId, String goalAsText, int goalStatus) {
		mId = id;
		mStartTime = startTime;
		mDays = days;
		mEndTime = endTime;
		mGoalId = goalId;
		mGoalAsText = goalAsText;
		mGoalStatus = goalStatus;
	}
	
	public void setTargetAmount(int amount) {
		mTargetAmount = amount;
	}
	
	/**
	 * @return the mStartTime
	 */
	public long getStartTime() {
		return mStartTime;
	}

	/**
	 * @return the mDays
	 */
	public int getDays() {
		return mDays;
	}

	/**
	 * @return the mEndTime
	 */
	public long getEndTime() {
		return mEndTime;
	}

	/**
	 * @return the mGoalId
	 */
	public int getGoalId() {
		return mGoalId;
	}

	/**
	 * @return the mId
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return the mAmount
	 */
	public int getTargetAmount() {
		return mTargetAmount;
	}

	/**
	 * @return the mProgress
	 */
	public int getCurrentAmount(Context context) {
		DayDataHandler dataHandler = new DayDataHandler(context);
		switch (mGoalId) {
		case 0:
			mCurrentAmount = dataHandler.getClickedDrinksForDay(System.currentTimeMillis());
			break;
		case 1:
			Calendar calendarFirst = Calendar.getInstance();
			Calendar calendarSecond = Calendar.getInstance();
			long[] timestampsForDrinks = dataHandler.getTimestampsForDrinksBetween(mStartTime, mEndTime);
			if (timestampsForDrinks.length > 0) {
				mCurrentAmount = 1;
			} else {
				mCurrentAmount = 0;
			}
			for (int i = 0; i < timestampsForDrinks.length - 1; i++) {
				calendarFirst.setTimeInMillis(timestampsForDrinks[i]);
				calendarSecond.setTimeInMillis(timestampsForDrinks[i+1]);
				mCurrentAmount = mCurrentAmount + (Math.abs(calendarFirst.get(Calendar.DATE) - calendarSecond.get(Calendar.DATE)));
			}
		}
		return mCurrentAmount;
	}
	
	public int getStatus(Context context) {
		if (mGoalStatus != GoalDataHandler.GOAL_ACTIVE) {
			return mGoalStatus;
		}
		GoalDataHandler dataHandler = new GoalDataHandler(context);
		if (mGoalId < 2) {
			int currentAmount = getCurrentAmount(context);
			if (currentAmount > mTargetAmount) {
				mGoalStatus = GoalDataHandler.GOAL_FAILED;
				dataHandler.updateGoalStatus(mId, mGoalStatus);
			}
		}
		
		if (mGoalStatus == GoalDataHandler.GOAL_ACTIVE && System.currentTimeMillis() > mEndTime) {
			mGoalStatus = GoalDataHandler.GOAL_COMPLETE;
			dataHandler.updateGoalStatus(mId, mGoalStatus);
		}
		
		return mGoalStatus;
	}
	
	public int getCurrentDayInGoal() {
		if (System.currentTimeMillis() > mEndTime) {
			return mDays;
		}
		long timeDif = System.currentTimeMillis() - mStartTime;
		return (int) TimeUnit.DAYS.convert(timeDif, TimeUnit.MILLISECONDS) + 1;
	}

	public String getGoalAsText() {
		return mGoalAsText;
	}

	
	
}
