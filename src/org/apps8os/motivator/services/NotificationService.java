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
package org.apps8os.motivator.services;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.data.SprintDataHandler;
import org.apps8os.motivator.ui.MoodQuestionActivity;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Represents a notification service for sending notifications.
 * @author Toni Järvinen
 *
 */
public class NotificationService extends IntentService {
	
	public NotificationService() {
		super("Notification Service");
	}

	public final static int NOTIFICATION_ID_MOOD = 10;
	public final static String NOTIFICATION_TYPE = "notification_type";
	public final static int NOTIFICATION_MOOD = 100;
	public final static int NOTIFICATION_EVENT_START = 101;

	@Override
	protected void onHandleIntent(Intent intent) {
		// Set up the notification with a builder
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Bundle extras = intent.getExtras();
		
		// Check the notification type.
		int notificationType = extras.getInt(NOTIFICATION_TYPE);
		if (notificationType == NOTIFICATION_MOOD) {
			builder.setContentTitle(getString(R.string.today_screen_mood));
			
			SprintDataHandler dataHandler = new SprintDataHandler(this);
			Sprint currentSprint = dataHandler.getCurrentSprint();
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			// If there is no current sprint, cancel alarms.
			if (currentSprint == null) {
				Intent notificationIntent = new Intent(this, NotificationService.class);
				PendingIntent pendingNotificationIntent = PendingIntent.getService(this,0,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				alarmManager.cancel(pendingNotificationIntent);
			} else {
				int currentDateInSprint = currentSprint.getCurrentDayOfTheSprint();
				
				builder.setSmallIcon(R.drawable.ic_stat_notification_icon_1);
				// Remove the notification when the user clicks it.
				builder.setAutoCancel(true);
				
				// Where to go when user clicks the notification
				Intent resultIntent = new Intent(this, MoodQuestionActivity.class);
				DayDataHandler moodDataHandler = new DayDataHandler(this);
				
				// Check if there were events yesterday.
				DayInHistory yesterday = moodDataHandler.getDayInHistory(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
			    yesterday.setEvents();
			    ArrayList<MotivatorEvent> yesterdayEvents = yesterday.getUncheckedEvents();
			    if (!yesterdayEvents.isEmpty()) {
			    	// Put the events as extras to the intent so that we can pass them to the checking activity.
			    	resultIntent.putExtra(MotivatorEvent.YESTERDAYS_EVENTS, yesterdayEvents);
			    	resultIntent.putExtra(EventDataHandler.EVENTS_TO_CHECK, true);
			    	builder.setContentText(getString(R.string.you_had_an_event_yesterday));
			    } else {
			    	// No events to check.
			    	resultIntent.putExtra(EventDataHandler.EVENTS_TO_CHECK, false);
			    	builder.setContentText(getString(R.string.today_is_the_day) + " " + currentDateInSprint + " " + getString(R.string.of_glory));
			    }
				// Preserve the normal navigation of the app by adding the parent stack of the result activity
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				stackBuilder.addParentStack(MoodQuestionActivity.class);
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent pendingResultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
				builder.setContentIntent(pendingResultIntent);
				manager.notify(NOTIFICATION_ID_MOOD, builder.build());
			}
		} 
		
		else if (notificationType == NOTIFICATION_EVENT_START) {
			// Set up a notification for the start of an event.
			int eventId = extras.getInt(EventDataHandler.EVENT_ID);
			builder.setContentTitle("You have an event starting");
			builder.setSmallIcon(R.drawable.ic_stat_notification_icon_1);
			// Remove the notification when the user clicks it.
			builder.setAutoCancel(true);
			manager.notify(eventId, builder.build());
		}
	}

}
