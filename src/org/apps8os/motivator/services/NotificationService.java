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

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.ui.MoodQuestionActivity;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Represents a simple notification service for sending notifications.
 * @author Toni Järvinen
 *
 */
public class NotificationService extends IntentService {
	
	public NotificationService() {
		super("Notification Service");
	}

	public final static int NOTIFICATION_ID_MOOD = 10;

	@Override
	protected void onHandleIntent(Intent intent) {
		// Set up the notification with a builder
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.today_screen_mood));
		
		EventDataHandler dataHandler = new EventDataHandler(this);
		Sprint currentSprint = dataHandler.getCurrentSprint();
		int currentDateInSprint = currentSprint.getCurrentDayOfTheSprint();
		builder.setContentText(getString(R.string.today_is_the_day) + " " + currentDateInSprint + " " + getString(R.string.of_glory));
		builder.setSmallIcon(R.drawable.ic_stat_notification_icon_temp1);
		builder.setTicker(getString(R.string.today_screen_mood));
		// Remove the notification when the user clicks it.
		builder.setAutoCancel(true);
		
		// Where to go when user clicks the notification
		Intent resultIntent = new Intent(this, MoodQuestionActivity.class);
		// Preserve the normal navigation of the app by adding the parent stack of the result activity
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MoodQuestionActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent pendingResultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingResultIntent);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID_MOOD, builder.build());
	}

}
