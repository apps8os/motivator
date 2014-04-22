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
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.R.array;
import org.apps8os.motivator.utils.UtilityMethods;

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
	
	public static final int QUESTION_ID_WHEN = 1000;
	public static final int QUESTION_ID_WHERE = 1001;
	public static final int QUESTION_ID_TIME_TO_GO = 1002;
	
	private static final String TABLE_NAME = TABLE_NAME_EVENTS;
	private GregorianCalendar mCalendarCache;					// Caching the calendar for all answers belonging to the same instance.
	private SparseArray<Question> mQuestions;
	
	public EventDataHandler(Context context) {
		super(context);
		
		mQuestions = new SparseArray<Question>();
		Resources res = context.getResources();
		// Inserting the questions to the SpareArrays.
		String[] eventQuestionIds =  res.getStringArray(R.array.event_question_ids);
		String[] requiredQuestionIds = res.getStringArray(R.array.event_required_ids);
		for (int i = 0; i < eventQuestionIds.length; i++) {
			boolean required = Arrays.asList(requiredQuestionIds).contains(eventQuestionIds[i]);
			// String array of questions
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier(eventQuestionIds[i], "array", context.getPackageName()));
			// discard the "id" part of the question id
			int id = Integer.parseInt(eventQuestionIds[i].substring(2));
			// Creation of new Question object and inserting it to the array.
			Question question = new Question(id, questionAndAnswers[0], Arrays.copyOfRange(questionAndAnswers, 1, questionAndAnswers.length), required);
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
		open();
		// Check if the question is the one asking when the event will be.
		if (questionId == QUESTION_ID_WHEN) {
			// Initialize the cache calendar for today/now
			mCalendarCache = new GregorianCalendar();
			UtilityMethods.setToDayStart(mCalendarCache);
			switch (answer) {
				// Answer today, no need to do anything
				case 0: {
					break;
				}
				// Tomorrow, set the calendar to tomorrow midnight.
				case 1: {
					mCalendarCache.add(Calendar.DAY_OF_MONTH, 1);
					break;
				}
				// Next weekend, set the calendar to next friday at midnight.
				case 2: {
					int daysUntilNextFriday = (Calendar.FRIDAY - mCalendarCache.get(Calendar.DAY_OF_WEEK));
					mCalendarCache.add(Calendar.DAY_OF_MONTH, daysUntilNextFriday);
					break;
				}
			}
		}
		// Insert the answer with the calendar timestamp
		super.insertAnswer(answer, questionId, answersId, mCalendarCache.getTimeInMillis(), TABLE_NAME);
		close();
	}
	
	/**
	 * Used to get events after today.
	 * @return	Cursor over the results
	 */
	public ArrayList<MotivatorEvent> getEventsAfterToday() {
		open();
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		GregorianCalendar tomorrowMidnight = new GregorianCalendar();
		tomorrowMidnight.setFirstDayOfWeek(Calendar.MONDAY);
		UtilityMethods.setToDayStart(tomorrowMidnight);
		tomorrowMidnight.add(Calendar.DAY_OF_MONTH, 1);
		
		String selection = KEY_CONTENT + " >= " + tomorrowMidnight.getTimeInMillis();
		String[] columns = {KEY_ID_ANSWERS, KEY_ID_QUESTION, KEY_ANSWER, KEY_CONTENT};
		Cursor query = mDb.query(TABLE_NAME, columns, selection, null, null, null, KEY_ID_ANSWERS);
		
		query.moveToFirst();
			int lastAnswerId = -1;
			// Looping through the cursor.
			if (query.getCount() > 0) {
				// Initialize a MotivatorEvent object with the answerId from the database as the eventId.
				MotivatorEvent event = new MotivatorEvent(query.getInt(0));
			while (!query.isClosed()) {
				// Check if we have looped through the answers relating to this event with the answerId.
				if (lastAnswerId != query.getInt(0) && lastAnswerId != -1) {
					// Add the event to the list and initialize a new instance.
					events.add(event);
					event = new MotivatorEvent(query.getInt(0));
				}
				Question question = getQuestion(query.getInt(1));
				
				if (question != null) {
					// Handle the different questions/answers.
					switch (question.getId()) {
					case QUESTION_ID_WHEN:
						event.setStartTime(query.getLong(3));
						event.setEventDateAsText(question.getAnswer(query.getInt(2)));
						break;
					case QUESTION_ID_TIME_TO_GO:
						switch (query.getInt(2)) {
						case 0:
							event.setStartTimeAsText(question.getAnswer(0));
							break;
						case 1:
							event.setStartTimeAsText(question.getAnswer(1));
							break;
						case 2:
							event.setStartTimeAsText(question.getAnswer(2));
							break;
						default:
						}
						break;
					}
				}
				lastAnswerId = query.getInt(0);
				if (query.isLast()) {
					query.close();
				} else {
					query.moveToNext();
				}
			}
			events.add(event);
			}
		// Sort the list
		Collections.sort(events, new Comparator<MotivatorEvent>() {
			@Override
			public int compare(MotivatorEvent case1, MotivatorEvent case2) {
				long case1Sorter = case1.getStartTime();
				long case2Sorter = case2.getStartTime();
				if (case1Sorter > case2Sorter) {
					return 1;
				} else if (case1Sorter == case2Sorter) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		close();
		return events;
	}
	
	/**
	 * Used to get events that are today.
	 * @return
	 */
	public ArrayList<MotivatorEvent> getEventsToday() {
		open();
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		GregorianCalendar todayMidnight = new GregorianCalendar();
		UtilityMethods.setToDayStart(todayMidnight);
		
		GregorianCalendar tomorrowMidnight = new GregorianCalendar();
		UtilityMethods.setToDayStart(tomorrowMidnight);
		tomorrowMidnight.add(Calendar.DAY_OF_MONTH, 1);
		
		String selection = KEY_CONTENT + " >= " + todayMidnight.getTimeInMillis() + " AND " + KEY_CONTENT + " < " + tomorrowMidnight.getTimeInMillis();
		String[] columns = {KEY_ID_ANSWERS, KEY_ID_QUESTION, KEY_ANSWER, KEY_CONTENT};
		Cursor query = mDb.query(TABLE_NAME, columns, selection, null, null, null, KEY_ID_ANSWERS);
		
		query.moveToFirst();
		int lastAnswerId = -1;
		// Looping through the cursor.
		if (query.getCount() > 0) {
			// Initialize a MotivatorEvent object with the answerId from the database as the eventId.
			MotivatorEvent event = new MotivatorEvent(query.getInt(0));
			while (!query.isClosed()) {
				// Check if we have looped through the answers relating to this event with the answerId.
				if (lastAnswerId != query.getInt(0) && lastAnswerId != -1) {
					// Add the event to the list and initialize a new instance.
					events.add(event);
					event = new MotivatorEvent(query.getInt(0));
				}
				// Get the question with the questionId.
				Question question = getQuestion(query.getInt(1));
				
				if (question != null) {
					// Handle the different questions/answers.
					switch (question.getId()) {
					case QUESTION_ID_WHEN:
						event.setStartTime(query.getLong(3));
						// All events in this section should have today as the text so get the answer representing today.
						event.setEventDateAsText(question.getAnswer(0));
						break;
						
					// The start time is initialized to the change of day, add the amount of hours to get the time of the day.
					case QUESTION_ID_TIME_TO_GO:
						long time;
						switch (query.getInt(2)) {
						case 0:
							time = TimeUnit.MILLISECONDS.convert(15, TimeUnit.HOURS);
							event.setStartTimeAsText(question.getAnswer(0));
							break;
						case 1:
							time = TimeUnit.MILLISECONDS.convert(18, TimeUnit.HOURS);
							event.setStartTimeAsText(question.getAnswer(1));
							break;
						case 2:
							time = TimeUnit.MILLISECONDS.convert(21, TimeUnit.HOURS);
							event.setStartTimeAsText(question.getAnswer(2));
							break;
						default:
							time = 0;
						}
						event.setStartTime(event.getStartTime() + time);
						break;
					}
				}
				lastAnswerId = query.getInt(0);
				if (query.isLast()) {
					query.close();
				} else {
					query.moveToNext();
				}
			}
			events.add(event);
			}
		// Sort the list
		Collections.sort(events, new Comparator<MotivatorEvent>() {
			@Override
			public int compare(MotivatorEvent case1, MotivatorEvent case2) {
				long case1Sorter = case1.getStartTime();
				long case2Sorter = case2.getStartTime();
				if (case1Sorter > case2Sorter) {
					return 1;
				} else if (case1Sorter == case2Sorter) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		close();
		return events;
	}
	
	private ArrayList<MotivatorEvent> getEvents(Cursor query) {
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		open();
		query.moveToFirst();
		int lastAnswerId = -1;
		// Looping through the cursor.
		if (query.getCount() > 0) {
			// Initialize a MotivatorEvent object with the answerId from the database as the eventId.
			MotivatorEvent event = new MotivatorEvent(query.getInt(0));
			while (!query.isClosed()) {
				// Check if we have looped through the answers relating to this event with the answerId.
				if (lastAnswerId != query.getInt(0) && lastAnswerId != -1) {
					// Add the event to the list and initialize a new instance.
					events.add(event);
					event = new MotivatorEvent(query.getInt(0));
				}
				// Get the question with the questionId.
				Question question = getQuestion(query.getInt(1));
				
				if (question != null) {
					// Handle the different questions/answers.
					switch (question.getId()) {
					case QUESTION_ID_WHEN:
						event.setStartTime(query.getLong(3));
						// All events in this section should have today as the text so get the answer representing today.
						event.setEventDateAsText(question.getAnswer(0));
						break;
						
					// The start time is initialized to the change of day, add the amount of hours to get the time of the day.
					case QUESTION_ID_TIME_TO_GO:
						long time;
						switch (query.getInt(2)) {
						case 0:
							time = TimeUnit.MILLISECONDS.convert(15, TimeUnit.HOURS);
							break;
						case 1:
							time = TimeUnit.MILLISECONDS.convert(18, TimeUnit.HOURS);
							break;
						case 2:
							time = TimeUnit.MILLISECONDS.convert(21, TimeUnit.HOURS);
							break;
						default:
							time = 0;
						}
						event.setStartTime(event.getStartTime() + time);
						break;
					}
				}
				lastAnswerId = query.getInt(0);
				if (query.isLast()) {
					query.close();
				} else {
					query.moveToNext();
				}
			}
			events.add(event);
			}
		// Sort the list
		Collections.sort(events, new Comparator<MotivatorEvent>() {
			@Override
			public int compare(MotivatorEvent case1, MotivatorEvent case2) {
				long case1Sorter = case1.getStartTime();
				long case2Sorter = case2.getStartTime();
				if (case1Sorter > case2Sorter) {
					return 1;
				} else if (case1Sorter == case2Sorter) {
					return 0;
				} else {
					return -1;
				}
			}
		});
		close();
		return events;
	}
	
	/**
	 * Used to get all the events for given day.
	 * @param dayInMillis
	 * @return The events as an ArrayList
	 */
	public ArrayList<MotivatorEvent> getEventsForDay(long dayInMillis) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dayInMillis);
		UtilityMethods.setToDayStart(calendar);
		long dayStartInMillis = calendar.getTimeInMillis();
		calendar.add(Calendar.DATE, 1);
		long dayEndInMillis = calendar.getTimeInMillis();
		
		open();
		String selection = KEY_CONTENT + " >= " + dayStartInMillis + " AND " + KEY_CONTENT + " < " + dayEndInMillis;
		String[] columns = {KEY_ID_ANSWERS, KEY_ID_QUESTION, KEY_ANSWER, KEY_CONTENT};
		Cursor query = mDb.query(TABLE_NAME, columns, selection, null, null, null, KEY_ID_ANSWERS);
		
		return getEvents(query);
	}
	
	public int getAmountOfQuestions() {
		return mQuestions.size();
	}
	
	public void deleteLastRow() {
		open();
    	super.deleteLastRow(TABLE_NAME);
    	close();
	}
	
	public void deleteRowsWithAnsweringId(int answerId) {
		open();
		super.deleteRowsWithAnsweringId(TABLE_NAME, answerId);
		close();
	}
	
	public Question getQuestion(int id) {
		if (id == MotivatorDatabaseHelper.DRINK_AMOUNT_ID) {
			return null;
		}
		return mQuestions.get(id);
	}
	
	public int getFirstQuestionId() {
		return mQuestions.keyAt(0);
	}

	public void deleteEvent(int mEventId) {
		open();
		String selection = KEY_ID_ANSWERS + " = " + mEventId;
		mDb.delete(TABLE_NAME, selection, null);
		close();
	}
	
}
