package org.apps8os.motivator.services;

import java.util.Calendar;

import org.apps8os.motivator.ui.SettingsActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (prefs.getBoolean(SettingsActivity.KEY_SEND_NOTIFICATIONS, true)) {
				// Set up notifying user to answer to the mood question
				// The time to notify the user
				Calendar notificationTime = Calendar.getInstance();
				notificationTime.set(Calendar.MINUTE, 0);
				notificationTime.set(Calendar.SECOND, 0);	
				// An alarm manager for scheduling notifications
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				// Set the notification to repeat over the given time at notificationTime
				Intent notificationIntent = new Intent(context, NotificationService.class);
				notificationIntent.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_MOOD);
				PendingIntent pendingNotificationIntent = PendingIntent.getService(context,0,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				alarmManager.cancel(pendingNotificationIntent);
				if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10) {
					notificationTime.add(Calendar.DATE, 1);
				}
				notificationTime.set(Calendar.HOUR_OF_DAY, 10);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
			}
		}
	}

}
