package org.apps8os.motivator.ui;


import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.services.NotificationService;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class SettingsActivity extends Activity implements OnSharedPreferenceChangeListener {
	
	public static final String KEY_SEND_NOTIFICATIONS = "send_notifications";
	public static final String KEY_NOTIFICATION_INTERVAL = "minimum_notification_interval";
	private int mTimeToNotify;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    getFragmentManager().beginTransaction()
        .replace(android.R.id.content, new SettingsFragment())
        .commit();
	    mTimeToNotify = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_NOTIFICATION_INTERVAL, "12"));
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
		GregorianCalendar notificationTime = new GregorianCalendar();
		UtilityMethods.setToMidnight(notificationTime);
		notificationTime.add(GregorianCalendar.HOUR, mTimeToNotify);	
		// Set the notification to repeat over the given time at notificationTime
		Intent notificationIntent = new Intent(this, NotificationService.class);
		PendingIntent pendingNotificationIntent = PendingIntent.getService(this,0,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		// An alarm manager for scheduling notifications
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		if (key.equals(KEY_SEND_NOTIFICATIONS)) {
			if (sharedPreferences.getBoolean(key, true)) {
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(mTimeToNotify, TimeUnit.HOURS), pendingNotificationIntent);
			} else {
				alarmManager.cancel(pendingNotificationIntent);
			}
		} else if (key.equals(KEY_NOTIFICATION_INTERVAL)) {
			mTimeToNotify = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_NOTIFICATION_INTERVAL, "12"));
			alarmManager.cancel(pendingNotificationIntent);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(mTimeToNotify, TimeUnit.HOURS), pendingNotificationIntent);
		}
		
	}

}
