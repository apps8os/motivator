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
package org.apps8os.motivator.ui;


import java.util.Calendar;

import org.apps8os.motivator.services.NotificationService;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * A settings activity for application settings.
 * @author Toni Järvinen
 *
 */
public class SettingsActivity extends Activity implements OnSharedPreferenceChangeListener {
	
	public static final String KEY_SEND_NOTIFICATIONS = "send_notifications";
	public static final String KEY_NOTIFICATION_INTERVAL = "minimum_notification_interval";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// An alarm manager for scheduling notifications
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// Set the notification to repeat over the given time at notificationTime
		Intent notificationIntent = new Intent(this, NotificationService.class);
		notificationIntent.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_MOOD);
		PendingIntent pendingNotificationIntent = PendingIntent.getService(this,0,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);	
		// Check if we need to send notifications or not.
		if (key.equals(KEY_SEND_NOTIFICATIONS)) {
			if (sharedPreferences.getBoolean(key, true)) {
				setNotifications(pendingNotificationIntent, alarmManager);
			} else {
				alarmManager.cancel(pendingNotificationIntent);
			}
		} 
		// Check if the interval has changed and update the repeating alarm
		else if (key.equals(KEY_NOTIFICATION_INTERVAL)) {
			setNotifications(pendingNotificationIntent, alarmManager);
		}
		
	}
	

	/**
	 * Set the notifications based on the selected interval.
	 * @param pendingNotificationIntent
	 * @param alarmManager
	 */
	public void setNotifications(PendingIntent pendingNotificationIntent, AlarmManager alarmManager) {
		// The time to notify the user
		Calendar notificationTime = Calendar.getInstance();
		notificationTime.set(Calendar.MINUTE, 0);
		notificationTime.set(Calendar.SECOND, 0);

		//mTimeToNotify = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_NOTIFICATION_INTERVAL, mRes.getString(R.string.in_the_morning_value));
		alarmManager.cancel(pendingNotificationIntent);
		if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10) {
			notificationTime.add(Calendar.DATE, 1);
		}
		notificationTime.set(Calendar.HOUR_OF_DAY, 10);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
		/**
		if (mTimeToNotify == mRes.getString(R.string.in_the_morning_value)) {
			if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10) {
				notificationTime.add(Calendar.DATE, 1);
			}
			notificationTime.set(Calendar.HOUR_OF_DAY, 10);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
		} else if (mTimeToNotify == mRes.getString(R.string.morning_and_evening_value)) {
			if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10 && notificationTime.get(Calendar.HOUR_OF_DAY) < 22) {
				notificationTime.set(Calendar.HOUR_OF_DAY, 22);
			} else if (notificationTime.get(Calendar.HOUR_OF_DAY) < 10) {
				notificationTime.set(Calendar.HOUR_OF_DAY, 10);
			} else {
				notificationTime.add(Calendar.DATE, 1);
				notificationTime.set(Calendar.HOUR_OF_DAY, 10);
			}
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingNotificationIntent);
		} else if (mTimeToNotify == mRes.getString(R.string.every_hour_value)) {
			notificationTime.add(Calendar.HOUR_OF_DAY, 1);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pendingNotificationIntent);
		}
		**/
	}

}
