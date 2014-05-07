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
import org.apps8os.motivator.services.NotificationService;
import org.apps8os.motivator.ui.MainActivity;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	public static final int QUESTION_ID_HOW_MUCH = 1001;
	public static final int QUESTION_ID_TIME_TO_GO = 1002;
	public static final int QUESTION_ID_TIME_TO_COME_BACK = 1003;
	public static final int QUESTION_ID_WITH_WHO = 1004;
	
	public static final String EVENT_ID = "event_id";
	public static final String EVENTS_TO_CHECK = "events_to_check";
	
	public static final String KEY_START_TIME = "start_time";
	public static final String KEY_END_TIME = "end_time";
	public static final String KEY_PLANNED_AMOUNT_OF_DRINKS = "planned_amount_of_drinks";
	public static final String KEY_WITH_WHO = "with_who";
	public static final String KEY_START_TIME_ANSWER = "start_time_answer";
	public static final String KEY_END_TIME_ANSWER = "end_time_answer";
	public static final String KEY_DAY_ANSWER = "day_time_answer";
	public static final String TABLE_NAME_EVENTS = "events";
	public static final String KEY_EVENT_CHECKED = "event_checked";
	public static final String KEY_EVENT_ID = "event_id";
	
	public static final String CREATE_EVENT_TABLE =
			"CREATE TABLE " + TABLE_NAME_EVENTS + " (" +
			KEY_ID + " INTEGER PRIMARY KEY, " +
			KEY_EVENT_ID + " INTEGER, " +
			KEY_START_TIME + " INTEGER, " +
			KEY_END_TIME + " INTEGER, " +
			KEY_PLANNED_AMOUNT_OF_DRINKS + " INTEGER, " +
			KEY_WITH_WHO + " INTEGER, " +
			KEY_START_TIME_ANSWER + " INTEGER, " +
			KEY_END_TIME_ANSWER + " INTEGER, " +
			KEY_DAY_ANSWER + " INTEGER, " +
			KEY_COMMENT + " TEXT, " +
			KEY_EVENT_CHECKED + " INTEGER, " +
			KEY_TIMESTAMP + " INTEGER);";
	
	private static final String TABLE_NAME = TABLE_NAME_EVENTS;
	private GregorianCalendar mCalendarCache;					// Caching the calendar for all answers belonging to the same instance.
	private SparseArray<Question> mQuestions;
	private Context mContext;
	
	public EventDataHandler(Context context) {
		super(context);
		mContext = context;
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
	
	public void insertEvent(int dayAnswer, int plannedDrinks, int startTimeAnswer, int endTimeAnswer, int withWho, String comment) {
		// Use SharedPreferences to store the event id so that it can be incremented even if the app is killed
		SharedPreferences eventIdIncrement = mContext.getSharedPreferences(MainActivity.MOTIVATOR_PREFS, 0);
		int eventId = eventIdIncrement.getInt(EVENT_ID, 1);
		SharedPreferences.Editor editor = eventIdIncrement.edit();
		editor.putInt(EVENT_ID, eventId + 1);
		editor.commit();
		
		SQLiteDatabase db = super.open();
		ContentValues values = new ContentValues();
		// Initialize the cache calendar for today/now
		GregorianCalendar calendarCache = new GregorianCalendar();
		UtilityMethods.setToDayStart(calendarCache);
		switch (dayAnswer) {
			// Answer today, no need to do anything
			case 0: {
				break;
			}
			// Tomorrow, set the calendar to tomorrow midnight.
			case 1: {
				calendarCache.add(Calendar.DATE, 1);
				break;
			}
			// Next weekend, set the calendar to next friday at midnight.
			case 2: {
				int daysUntilNextFriday = (Calendar.FRIDAY - calendarCache.get(Calendar.DAY_OF_WEEK));
				calendarCache.add(Calendar.DAY_OF_MONTH, daysUntilNextFriday);
				break;
			}
		}
		switch (startTimeAnswer) {
			case 0: 
				calendarCache.set(Calendar.HOUR_OF_DAY, 16);
				break;
			case 1:
				calendarCache.set(Calendar.HOUR_OF_DAY, 18);
				break;
			case 2:
				calendarCache.set(Calendar.HOUR_OF_DAY, 21);
				break;
		}
		values.put(KEY_EVENT_ID, eventId);
		values.put(KEY_START_TIME, calendarCache.getTimeInMillis());
		
		if (startTimeAnswer > -1) {
			AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			// Set the notification to repeat over the given time at notificationTime
			Intent notificationIntent = new Intent(mContext, NotificationService.class);
			notificationIntent.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_EVENT_START);
			notificationIntent.putExtra(EVENT_ID, eventId);
			PendingIntent pendingNotificationIntent = PendingIntent.getService(mContext, eventId,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP, calendarCache.getTimeInMillis(), pendingNotificationIntent);
		}
		
		UtilityMethods.setToDayStart(calendarCache);
		values.put(KEY_DAY_ANSWER, dayAnswer);
		values.put(KEY_PLANNED_AMOUNT_OF_DRINKS, plannedDrinks);
		values.put(KEY_EVENT_CHECKED, 0);
		values.put(KEY_TIMESTAMP, System.currentTimeMillis());
		if (startTimeAnswer != -1) {
			values.put(KEY_START_TIME_ANSWER, startTimeAnswer);
		}
		if (endTimeAnswer != -1) {
			switch (endTimeAnswer) {
				case 0: 
					calendarCache.set(Calendar.HOUR_OF_DAY, 21);
					break;
				case 1:
					calendarCache.set(Calendar.HOUR_OF_DAY, 23);
					calendarCache.set(Calendar.MINUTE, 59);
					break;
				case 2:
					calendarCache.add(Calendar.DATE, 1);
					calendarCache.set(Calendar.HOUR_OF_DAY, 2);
					break;
			}
			values.put(KEY_END_TIME, calendarCache.getTimeInMillis());
			values.put(KEY_END_TIME_ANSWER, endTimeAnswer);
		}
		if (withWho != -1) {
			values.put(KEY_WITH_WHO, withWho);
		}
		if (comment != null) {
			values.put(KEY_COMMENT, comment);
		}
		db.insert(TABLE_NAME_EVENTS, null, values);
		db.close();
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
		
		String selection = KEY_START_TIME + " >= " + tomorrowMidnight.getTimeInMillis();
		Cursor query = mDb.query(TABLE_NAME_EVENTS, null, selection, null, null, null, null);
		
		addEvents(events, query);
		
		query.close();
		close();
		return events;
	}
	
	public void addEvents(ArrayList<MotivatorEvent> events, Cursor query) {
		query.moveToFirst();
		for (int i = 0; i < query.getCount(); i ++) {
			MotivatorEvent event = new MotivatorEvent(query.getInt(1));
			event.setStartTime(query.getLong(2));
			event.setPlannedDrinks(query.getInt(4));
			
			if (!query.isNull(3)) {
				event.setEndTime(query.getLong(3));
			}
			if (!query.isNull(5)) {
				event.setWithWho(mQuestions.get(QUESTION_ID_WITH_WHO).getAnswer(query.getInt(5)));
			}
			if (!query.isNull(6)) {
				event.setStartTimeAsText(mQuestions.get(QUESTION_ID_TIME_TO_GO).getAnswer(query.getInt(6)));
			}
			if (!query.isNull(7)) {
				event.setEndTimeAsText(mQuestions.get(QUESTION_ID_TIME_TO_COME_BACK).getAnswer(query.getInt(7)));
			}
			if (!query.isNull(8)) {
				event.setEventDateAsText(mQuestions.get(QUESTION_ID_WHEN).getAnswer(query.getInt(8)));
			}
			if (!query.isNull(9)) {
				event.setName(query.getString(9));
			}
			if (query.getInt(10) == 1) {
				event.setChecked();
			}
			events.add(event);
			query.moveToNext();
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
		
		String selection = KEY_START_TIME + " >= " + todayMidnight.getTimeInMillis() + " AND " + KEY_START_TIME + " < " + tomorrowMidnight.getTimeInMillis();
		Cursor query = mDb.query(TABLE_NAME, null, selection, null, null, null, null);
		
		addEvents(events, query);
		
		query.close();
		close();
		return events;
	}
	
	/**
	 * Used to get all the events for given day.
	 * @param dayInMillis
	 * @return The events as an ArrayList
	 */
	public ArrayList<MotivatorEvent> getEventsForDay(long dayInMillis) {
		open();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dayInMillis);
		UtilityMethods.setToDayStart(calendar);
		long dayStartInMillis = calendar.getTimeInMillis();
		calendar.add(Calendar.DATE, 1);
		long dayEndInMillis = calendar.getTimeInMillis();
		
		String selection = KEY_START_TIME + " >= " + dayStartInMillis + " AND " + KEY_START_TIME + " < " + dayEndInMillis;
		Cursor query = mDb.query(TABLE_NAME, null, selection, null, null, null, null);
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		
		addEvents(events, query);
		query.close();
		close();
		return events;
	}
	
	public ArrayList<MotivatorEvent> getEventsBetween(long startDayInMillis, long endDayInMillis) {
		open();
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(startDayInMillis);
		UtilityMethods.setToDayStart(calendar);
		long startTimestamp = calendar.getTimeInMillis();
		calendar.setTimeInMillis(endDayInMillis);
		UtilityMethods.setToDayStart(calendar);
		long endTimestamp = calendar.getTimeInMillis();
		

		String selection = KEY_START_TIME + " >= " + startTimestamp + " AND " + KEY_START_TIME + " < " + endTimestamp;
		Cursor query = mDb.query(TABLE_NAME, null, selection, null, null, null, null);
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		
		addEvents(events, query);
		query.close();
		close();
		return events;
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
		return mQuestions.get(id);
	}
	
	public int getFirstQuestionId() {
		return mQuestions.keyAt(0);
	}

	public void deleteEvent(int eventId) {
		open();
		String selection = KEY_EVENT_ID + " = " + eventId;
		mDb.delete(TABLE_NAME, selection, null);
		AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
		// Set the notification to repeat over the given time at notificationTime
		Intent notificationIntent = new Intent(mContext, NotificationService.class);
		notificationIntent.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_EVENT_START);
		notificationIntent.putExtra(EVENT_ID, eventId);
		PendingIntent pendingNotificationIntent = PendingIntent.getService(mContext, eventId,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.cancel(pendingNotificationIntent);
		
		NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.cancel(eventId);
		close();
	}
	
}
