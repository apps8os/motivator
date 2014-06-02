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

import org.apps8os.motivator.R;
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
 * Handles the access for event data.
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
	
	public static final int EVENT_NOT_CHECKED = 0;
	public static final int EVENT_CHECKED = 1;
	
	public static final String EVENT_ID = "event_id";
	public static final String EVENT_NAME = "event_name";
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
		boolean required;
		for (int i = 0; i < eventQuestionIds.length; i++) {
			required = Arrays.asList(requiredQuestionIds).contains(eventQuestionIds[i]);
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
	 * Inserting an event to the database. This handles the calculation of the answers to timestamps.
	 * Also adds alarms for notification on the event start.
	 * @param eventId
	 * @param dayAnswer
	 * @param plannedDrinks
	 * @param startTimeAnswer
	 * @param endTimeAnswer
	 * @param withWho
	 * @param name
	 * @param date
	 * @param checked
	 */
	private void insertEvent(int eventId, int dayAnswer, int plannedDrinks, int startTimeAnswer, int endTimeAnswer, int withWho, String name, long date, int checked) {
		// Use SharedPreferences to store the event id so that it can be incremented even if the app is killed
		SharedPreferences eventIdIncrement = mContext.getSharedPreferences(MainActivity.MOTIVATOR_PREFS, 0);
		int innerEventId;
		if (eventId == -1) {
			innerEventId = eventIdIncrement.getInt(EVENT_ID, 1);
			SharedPreferences.Editor editor = eventIdIncrement.edit();
			editor.putInt(EVENT_ID, innerEventId + 1);
			editor.commit();
		} else {
			innerEventId = eventId;
		}
		
		SQLiteDatabase db = super.open();
		ContentValues values = new ContentValues();
		// Initialize the cache calendar for today/now
		Calendar calendarCache = Calendar.getInstance();
		if (checked == EVENT_CHECKED) {
			calendarCache.add(Calendar.DATE, -1);
		}
		UtilityMethods.setToDayStart(calendarCache);
		switch (dayAnswer) {
			// Answer today, no need to do anything
			case 1: {
				break;
			}
			// Tomorrow, set the calendar to tomorrow midnight.
			case 2: {
				calendarCache.add(Calendar.DATE, 1);
				break;
			}
			case 3: {
				calendarCache.setTimeInMillis(date);
				break;
			}
		}
		switch (startTimeAnswer) {
			case 1: 
				calendarCache.set(Calendar.HOUR_OF_DAY, 16);
				break;
			case 2:
				calendarCache.set(Calendar.HOUR_OF_DAY, 18);
				break;
			case 3:
				calendarCache.set(Calendar.HOUR_OF_DAY, 21);
				break;
		}
		values.put(KEY_EVENT_ID, innerEventId);
		values.put(KEY_START_TIME, calendarCache.getTimeInMillis());
		
		// Schedule an alarm for notification when the event is starting.
		if (startTimeAnswer > 0 && checked == EVENT_NOT_CHECKED) {
			AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
			// Set the notification to repeat over the given time at notificationTime
			Intent notificationIntent = new Intent(mContext, NotificationService.class);
			notificationIntent.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_EVENT_START);
			notificationIntent.putExtra(EVENT_ID, innerEventId);
			notificationIntent.putExtra(EVENT_NAME, name);
			PendingIntent pendingNotificationIntent = PendingIntent.getService(mContext, innerEventId,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.set(AlarmManager.RTC_WAKEUP, calendarCache.getTimeInMillis(), pendingNotificationIntent);
		}
		
		UtilityMethods.setToDayStart(calendarCache);
		values.put(KEY_DAY_ANSWER, dayAnswer);
		values.put(KEY_PLANNED_AMOUNT_OF_DRINKS, plannedDrinks - 1);
		values.put(KEY_EVENT_CHECKED, checked);
		values.put(KEY_TIMESTAMP, System.currentTimeMillis());
		if (startTimeAnswer != 0) {
			values.put(KEY_START_TIME_ANSWER, startTimeAnswer);
		}
		if (endTimeAnswer != 0) {
			switch (endTimeAnswer) {
				case 1: 
					calendarCache.set(Calendar.HOUR_OF_DAY, 21);
					break;
				case 2:
					calendarCache.set(Calendar.HOUR_OF_DAY, 23);
					calendarCache.set(Calendar.MINUTE, 59);
					break;
				case 3:
					calendarCache.add(Calendar.DATE, 1);
					calendarCache.set(Calendar.HOUR_OF_DAY, 2);
					break;
			}
			values.put(KEY_END_TIME, calendarCache.getTimeInMillis());
			values.put(KEY_END_TIME_ANSWER, endTimeAnswer);
		}
		if (withWho != 0) {
			values.put(KEY_WITH_WHO, withWho);
		}
		if (name != null) {
			values.put(KEY_COMMENT, name);
		}
		db.insert(TABLE_NAME_EVENTS, null, values);
		db.close();
	}
	
	/**
	 * Inserting event with a specific date as long
	 * @param dayAnswer
	 * @param plannedDrinks
	 * @param startTimeAnswer
	 * @param endTimeAnswer
	 * @param withWho
	 * @param comment
	 * @param date
	 */
	public void insertEvent(int dayAnswer, int plannedDrinks, int startTimeAnswer, int endTimeAnswer, int withWho, String comment, long date) {
		insertEvent(-1, dayAnswer, plannedDrinks, startTimeAnswer, endTimeAnswer, withWho, comment, date, 0);
	}
	
	/**
	 * Inserting checked event to the database.
	 * @param eventId
	 * @param plannedDrinks
	 * @param startTimeAnswer
	 * @param endTimeAnswer
	 * @param withWho
	 * @param comment
	 */
	public void insertCheckedEvent(int eventId, int plannedDrinks, int startTimeAnswer, int endTimeAnswer, int withWho, String comment) {
		insertEvent(eventId, 1, plannedDrinks, startTimeAnswer, endTimeAnswer, withWho, comment, 0L, 1);
	}
	
	/**
	 * Used to get events after today.
	 * @return	Cursor over the results
	 */
	public ArrayList<MotivatorEvent> getEventsAfterToday() {
		open();
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		Calendar tomorrowMidnight = Calendar.getInstance();
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
	
	/**
	 * Adds event from the cursor to the ArrayList.
	 * @param events
	 * @param query
	 */
	private void addEvents(ArrayList<MotivatorEvent> events, Cursor query) {
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
		Calendar todayMidnight = Calendar.getInstance();
		UtilityMethods.setToDayStart(todayMidnight);
		
		Calendar tomorrowMidnight = Calendar.getInstance();
		UtilityMethods.setToDayStart(tomorrowMidnight);
		tomorrowMidnight.add(Calendar.DAY_OF_MONTH, 1);
		
		String selection = KEY_START_TIME + " >= " + todayMidnight.getTimeInMillis() + " AND " + KEY_START_TIME + " < " + tomorrowMidnight.getTimeInMillis();
		Cursor query = mDb.query(TABLE_NAME_EVENTS, null, selection, null, null, null, null);
		
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
	public ArrayList<MotivatorEvent> getUncheckedEventsForDay(long dayInMillis) {
		open();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dayInMillis);
		UtilityMethods.setToDayStart(calendar);
		long dayStartInMillis = calendar.getTimeInMillis();
		calendar.add(Calendar.DATE, 1);
		long dayEndInMillis = calendar.getTimeInMillis();
		
		String selection = KEY_START_TIME + " >= " + dayStartInMillis + " AND " + KEY_START_TIME + " < " + dayEndInMillis + " AND " + KEY_EVENT_CHECKED + " = " + EVENT_NOT_CHECKED;
		Cursor query = mDb.query(TABLE_NAME_EVENTS, null, selection, null, null, null, null);
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		
		addEvents(events, query);
		query.close();
		close();
		return events;
	}
	
	/**
	 * Gets the unchecked events between the timestamps
	 * @param startDayInMillis
	 * @param endDayInMillis
	 * @return
	 */
	public ArrayList<MotivatorEvent> getUncheckedEventsBetween(long startDayInMillis, long endDayInMillis) {
		open();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startDayInMillis);
		UtilityMethods.setToDayStart(calendar);
		long startTimestamp = calendar.getTimeInMillis();
		calendar.setTimeInMillis(endDayInMillis);
		UtilityMethods.setToDayStart(calendar);
		long endTimestamp = calendar.getTimeInMillis();
		

		String selection = KEY_START_TIME + " >= " + startTimestamp + " AND " + KEY_START_TIME + " < " + endTimestamp + " AND " + KEY_EVENT_CHECKED + " = " + EVENT_NOT_CHECKED;
		Cursor query = mDb.query(TABLE_NAME_EVENTS, null, selection, null, null, null, null);
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		
		addEvents(events, query);
		query.close();
		close();
		return events;
	}
	
	/**
	 * Gets the unchecked event with given id.
	 * @param uncheckedEventId
	 * @return
	 */
	public MotivatorEvent getUncheckedEvent(int uncheckedEventId) {
		open();
		String selection = KEY_EVENT_ID + " = " + uncheckedEventId + " AND " + KEY_EVENT_CHECKED + " = " + EVENT_NOT_CHECKED;
		Cursor query = mDb.query(TABLE_NAME_EVENTS, null, selection, null, null, null, null);
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		
		addEvents(events, query);
		query.close();
		close();
		if (events.size() > 0) {
			return events.get(0);
		} else {
			return null;
		}
	}
	
	public MotivatorEvent getCheckedEvent(int uncheckedEventId) {
		open();
		String selection = KEY_EVENT_ID + " = " + uncheckedEventId + " AND " + KEY_EVENT_CHECKED + " = " + EVENT_CHECKED;
		Cursor query = mDb.query(TABLE_NAME_EVENTS, null, selection, null, null, null, null);
		ArrayList<MotivatorEvent> events = new ArrayList<MotivatorEvent>();
		
		addEvents(events, query);
		query.close();
		close();
		if (events.size() > 0) {
			return events.get(0);
		} else {
			return null;
		}
	}
	
	public int getAmountOfQuestions() {
		return mQuestions.size();
	}
	
	public Question getQuestion(int id) {
		return mQuestions.get(id);
	}
	
	public int getFirstQuestionId() {
		return mQuestions.keyAt(0);
	}

	/**
	 * Deletes the event with given id from the database. Also removes any alarms scheduled for the event.
	 * @param eventId
	 */
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
	
	/**
	 * Gets an raw int from the database. Valid fields are
	 * {KEY_PLANNED_AMOUNT_OF_DRINKS, KEY_WITH_WHO, KEY_START_TIME_ANSWER, KEY_END_TIME_ANSWER, KEY_DAY_ANSWER}
	 * @param eventId
	 * @param field
	 * @return
	 */
	public int getRawFieldUnchecked(int eventId, String field) {
		boolean validField = false;
		String fields[] = {KEY_PLANNED_AMOUNT_OF_DRINKS, KEY_WITH_WHO, KEY_START_TIME_ANSWER, KEY_END_TIME_ANSWER, KEY_DAY_ANSWER};
		for (String element: fields) {
			if (element == field) {
				validField = true;
				break;
			}
		}
		if (validField) {
			open();
			String selection = KEY_EVENT_ID + " = " + eventId + " AND " + KEY_EVENT_CHECKED + " = " + 0;
			String columns[] = {field};
			Cursor query = mDb.query(TABLE_NAME_EVENTS, columns, selection, null, null, null, null);
			if (query.moveToFirst()) {
				int result = query.getInt(0);
				query.close();
				close();
				return result;
			} else {
				query.close();
				close();
				return -2;
			}
			
		} else {
			return -1;
		}
	}
	
	
	public long getLatestAddedEventTimestamp() {
		open();
		String columns[] = {KEY_TIMESTAMP};
		Cursor query = mDb.query(TABLE_NAME_EVENTS, columns, null, null, null, null, KEY_TIMESTAMP);
		if (query.moveToLast()) {
			return query.getLong(0);
		} else {
			return 0L;
		}
	}
	
}
