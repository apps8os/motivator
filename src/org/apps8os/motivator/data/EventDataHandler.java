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


import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apps8os.motivator.R;
import org.apps8os.motivator.utils.MotivatorConstants;
import org.apps8os.motivator.utils.UtilityMethods;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.util.SparseArray;

/**
 * Handles the access for event data. The specific content in event database is the
 * timestamp on midnight of the day when the event is supposed to be.
 * open() before using and close() after done
 * @author Toni Järvinen
 *
 */
public class EventDataHandler extends MotivatorDatabaseHelper {
	
	private static final String TABLE_NAME = TABLE_NAME_EVENTS;
	private GregorianCalendar mCalendarCache;					// Caching the calendar for all answers belonging to the same instance.
	private SparseArray<Question> mQuestions;
	
	public EventDataHandler(Context context) {
		super(context);
		
		mQuestions = new SparseArray<Question>();
		Resources res = context.getResources();
		// Inserting the questions to the SpareArrays.
		String[] eventQuestionIds =  res.getStringArray(R.array.event_question_ids);
		for (int i = 0; i < eventQuestionIds.length; i++) {
			// String array of questions
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier(eventQuestionIds[i], "array", context.getPackageName()));
			// discard the "id" part of the question id
			int id = Integer.parseInt(eventQuestionIds[i].substring(2));
			// Creation of new Question object and inserting it to the array.
			Question question = new Question(id, questionAndAnswers[0], Arrays.copyOfRange(questionAndAnswers, 1, questionAndAnswers.length));
			mQuestions.put(id, question);
		}
	}
	
	/**
	 * Inserting event to the database. In events, the specific content field is the midnight of the day
	 * when the event should be. This is used to determine whether it belongs to plan section or
	 * today section.
	 * @param answer
	 * @param questionId
	 * @param answersId
	 */
	public void insertAnswer(int answer, int questionId, int answersId) {
		// Check if the question is the one asking when the event will be. (should be the first one)
		if (questionId == getFirstQuestionId()) {
			// Initialize the cache calendar for today/now
			mCalendarCache = new GregorianCalendar();
			switch (answer) {
				// Answer today, no need to do anything
				case 0: {
					break;
				}
				// Tomorrow, set the calendar to tomorrow midnight.
				case 1: {
					UtilityMethods.setToMidnight(mCalendarCache);
					mCalendarCache.add(Calendar.DAY_OF_MONTH, 1);
					break;
				}
				// Next weekend, set the calendar to next friday at midnight.
				case 2: {
					UtilityMethods.setToMidnight(mCalendarCache);
					int daysUntilNextFriday = (Calendar.FRIDAY - mCalendarCache.get(Calendar.DAY_OF_WEEK));
					mCalendarCache.add(Calendar.DAY_OF_MONTH, daysUntilNextFriday);
					break;
				}
			}
		}
		// Insert the answer with the calendar timestamp
		super.insertAnswer(answer, questionId, answersId, mCalendarCache.getTimeInMillis(), TABLE_NAME);
	}
	
	/**
	 * Used to get events after today.
	 * @return	Cursor over the results
	 */
	public Cursor getEventsAfterToday() {
		GregorianCalendar tomorrowMidnight = new GregorianCalendar();
		UtilityMethods.setToMidnight(tomorrowMidnight);
		tomorrowMidnight.add(Calendar.DAY_OF_MONTH, 1);
		
		String selection = KEY_CONTENT + " > " + tomorrowMidnight.getTimeInMillis();
		String[] columns = {KEY_ID_ANSWERS, KEY_ID_QUESTION, KEY_ANSWER};
		Cursor query = db.query(TABLE_NAME, columns, selection, null, null, null, KEY_ID_ANSWERS);
		return query;
	}
	
	/**
	 * Used to get events that are today.
	 * @return
	 */
	public Cursor getEventsToday() {
		GregorianCalendar todayMidnight = new GregorianCalendar();
		UtilityMethods.setToMidnight(todayMidnight);
		
		GregorianCalendar tomorrowMidnight = new GregorianCalendar();
		UtilityMethods.setToMidnight(tomorrowMidnight);
		tomorrowMidnight.add(Calendar.DAY_OF_MONTH, 1);
		
		String selection = KEY_CONTENT + " > " + todayMidnight.getTimeInMillis() + " AND " + KEY_CONTENT + " < " + tomorrowMidnight.getTimeInMillis();
		String[] columns = {KEY_ID_ANSWERS, KEY_ID_QUESTION, KEY_ANSWER};
		Cursor query = db.query(TABLE_NAME, columns, selection, null, null, null, KEY_ID_ANSWERS);
		return query;
	}
	
	public void addDrink(int answerId) {
		String selection = KEY_ID_ANSWERS + " = " + answerId + " AND " + KEY_ID_QUESTION + " = " + MotivatorConstants.DRINK_AMOUNT_ID;
		String[] columns = {KEY_ANSWER};
		Cursor query = db.query(TABLE_NAME, columns, selection, null, null, null, null);
		int previousAmount = 0;
		if (query.moveToFirst()) {
			previousAmount = query.getInt(0);
		}
		ContentValues values = new ContentValues();
		values.put(KEY_ANSWER, previousAmount + 1);
		db.update(TABLE_NAME, values, selection, null);
		query.close();
	}
	
	public Cursor getEventWithId(int id) {
		String selection = KEY_ID_ANSWERS + " = " + id;
		String[] columns = {KEY_ID_QUESTION, KEY_ANSWER};
		Cursor query = db.query(TABLE_NAME, columns, selection, null, null, null, null);
		return query;
	}
	
	public int getAmountOfQuestions() {
		return mQuestions.size();
	}
	
	public void deleteLastRow() {
    	super.deleteLastRow(TABLE_NAME);
	}
	
	public Question getQuestion(int id) {
		return mQuestions.get(id);
	}
	
	public int getFirstQuestionId() {
		return mQuestions.keyAt(0);
	}

	public void deleteEvent(int mEventId) {
		String selection = KEY_ID_ANSWERS + " = " + mEventId;
		db.delete(TABLE_NAME, selection, null);
	}
	
}
