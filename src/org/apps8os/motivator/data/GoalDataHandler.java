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
import java.util.Arrays;
import java.util.Calendar;

import org.apps8os.motivator.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

/**
 * Handles the access for goal data. The specific content in goal database is...
 * open() before using and close() after done
 * @author Toni Järvinen
 *
 */
public class GoalDataHandler extends MotivatorDatabaseHelper {
	
	private SparseArray<Question> mQuestions;
	
	public static final int GOAL_ACTIVE = 0;
	public static final int GOAL_COMPLETE = 1;
	public static final int GOAL_FAILED = 2;
	
	public static final int QUESTION_ID_WHAT_TO_ACHIEVE = 3000;
	public static final int QUESTION_ID_TIMEFRAME = 3001;
	
	public static final String KEY_START_TIME = "start_time";
	public static final String KEY_END_TIME = "end_time";
	public static final String KEY_LENGTH_IN_DAYS = "length_in_days";
	public static final String KEY_GOAL_ID = "goal_id";
	public static final String KEY_GOAL_AS_TEXT = "goal_as_text";
	public static final String KEY_AMOUNT = "amount";
	public static final String KEY_STATUS = "complete";
	public static final String TABLE_NAME_GOALS = "goals";
	
	public static final String CREATE_GOAL_TABLE = 
					"CREATE TABLE " + TABLE_NAME_GOALS + " (" +
					KEY_ID + " INTEGER PRIMARY KEY, " +
					KEY_START_TIME + " INTEGER, " +
					KEY_END_TIME + " INTEGER, " +
					KEY_LENGTH_IN_DAYS + " INTEGER, " + 
					KEY_GOAL_ID + " INTEGER, " + 
					KEY_AMOUNT + " INTEGER, " + 
					KEY_STATUS + " INTEGER, " + 
					KEY_TIMESTAMP + " INTEGER);";
	
	private Context mContext;
	
	public GoalDataHandler(Context context) {
		super(context);
		mContext = context;
		
		mQuestions = new SparseArray<Question>();
		Resources res = context.getResources();
		// Inserting the questions to the SpareArrays.
		String[] goalQuestionIds =  res.getStringArray(R.array.goal_question_ids);
		String[] requiredQuestionIds = res.getStringArray(R.array.goal_required_ids);
		for (int i = 0; i < goalQuestionIds.length; i++) {
			boolean required = Arrays.asList(requiredQuestionIds).contains(goalQuestionIds[i]);
			// String array of questions
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier(goalQuestionIds[i], "array", context.getPackageName()));
			// discard the "id" part of the question id
			int id = Integer.parseInt(goalQuestionIds[i].substring(2));
			// Creation of new Question object and inserting it to the array.
			Question question = new Question(id, questionAndAnswers[0], Arrays.copyOfRange(questionAndAnswers, 1, questionAndAnswers.length), required);
			mQuestions.put(id, question);
		}
	}
	
	public void insertGoal(long startTime, int daysAnswer, int goalId, int amount) {
		SQLiteDatabase db = super.open();
		ContentValues values = new ContentValues();
		values.put(KEY_START_TIME, startTime);
		Calendar calendar = Calendar.getInstance();
		switch (daysAnswer) {
			case 0:
				values.put(KEY_LENGTH_IN_DAYS, 7);
				calendar.add(Calendar.DATE, 7);
				values.put(KEY_END_TIME, calendar.getTimeInMillis());
				break;
			case 1:
				values.put(KEY_LENGTH_IN_DAYS, 14);
				calendar.add(Calendar.DATE, 14);
				values.put(KEY_END_TIME, calendar.getTimeInMillis());
				break;
			case 2:
				values.put(KEY_LENGTH_IN_DAYS, 28);
				calendar.add(Calendar.DATE, 28);
				values.put(KEY_END_TIME, calendar.getTimeInMillis());
				break;
		}
		values.put(KEY_GOAL_ID, goalId);
		values.put(KEY_AMOUNT, amount);
		values.put(KEY_STATUS, GOAL_ACTIVE);
		values.put(KEY_TIMESTAMP, System.currentTimeMillis());
		db.insert(TABLE_NAME_GOALS, null, values);
		db.close();
	}
	
	public ArrayList<Goal> getOngoingGoals() {
		open();
		ArrayList<Goal> result = new ArrayList<Goal>();
		String selection = KEY_START_TIME + " < " +  System.currentTimeMillis() + " AND " + KEY_END_TIME + " > " + System.currentTimeMillis();
		Cursor query = mDb.query(TABLE_NAME_GOALS, null, selection, null, null, null, KEY_END_TIME);
		
		addGoals(result, query);
		query.close();
		close();
		return result;
	}
	
	private void addGoals(ArrayList<Goal> result, Cursor query) {
		Goal goal;
		if (query.moveToFirst()) {
			for (int i = 0; i < query.getCount(); i++) {
				int goalId = query.getInt(4);
				int amount = query.getInt(5);
				int days = query.getInt(3);
				String goalAsText = getQuestion(QUESTION_ID_WHAT_TO_ACHIEVE).getAnswer(goalId);
				switch(days) {
				case 7:
					if (goalId == 1) {
						goalAsText = goalAsText.replaceAll("X", "" + amount);
						goalAsText = goalAsText + " " + mContext.getString(R.string.in_a_week);
					} else if (goalId == 0) {
						goalAsText = goalAsText.replaceAll("X", "" + amount);
						goalAsText = goalAsText + " " + mContext.getString(R.string.for_a_week);
					} else {
						goalAsText = goalAsText + " " + mContext.getString(R.string.for_a_week);
					}
					break;
				case 14:
					if (goalId == 1) {
						goalAsText = goalAsText.replaceAll("X", "" + amount);
						goalAsText = goalAsText + " " + mContext.getString(R.string.in_two_weeks);
					} else if (goalId == 0) {
						goalAsText = goalAsText.replaceAll("X", "" + amount);
						goalAsText = goalAsText + " " + mContext.getString(R.string.for_two_weeks);
					} else {
						goalAsText = goalAsText + " " + mContext.getString(R.string.for_two_weeks);
					}
					break;
				case 28:
					if (goalId == 1) {
						goalAsText = goalAsText.replaceAll("X", "" + amount);
						goalAsText = goalAsText + " " + mContext.getString(R.string.in_four_weeks);
					} else if (goalId == 0) {
						goalAsText = goalAsText.replaceAll("X", "" + amount);
						goalAsText = goalAsText + " " + mContext.getString(R.string.for_four_weeks);
					} else {
						goalAsText = goalAsText + " " + mContext.getString(R.string.for_four_weeks);
					}
					break;
				}
				goal = new Goal(query.getInt(0), query.getLong(1), days, query.getLong(2), goalId, goalAsText, query.getInt(6));
				if (goalId < 2) {
					goal.setTargetAmount(amount);
				}
				result.add(goal);
				query.moveToNext();
			}
		}
	}
	
	public void updateGoalStatus(int id, int status) {
		open();
		ContentValues values = new ContentValues();
		values.put(KEY_STATUS, status);
		String selection = KEY_ID + " = " + id;
		mDb.update(TABLE_NAME_GOALS, values, selection, null);
		close();
	}

	public Question getQuestion(int id) {
		return mQuestions.get(id);
	}

	public int getAmountOfQuestions() {
		return mQuestions.size();
	}

	public int getFirstQuestionId() {
		return mQuestions.keyAt(0);
	}
}
