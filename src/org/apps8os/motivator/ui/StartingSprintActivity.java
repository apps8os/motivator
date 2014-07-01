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

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.data.SprintDataHandler;
import org.apps8os.motivator.services.NotificationService;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * An activity for starting a sprint.
 * @author Toni Järvinen
 *
 */
public class StartingSprintActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_starting_sprint);
		Button button = (Button) findViewById(R.id.start_sprint_button);
		final Activity activity = this;
		
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long startDayInMillis = UtilityMethods.setToDayStart(Calendar.getInstance()).getTimeInMillis();
				EditText numberOfDays = (EditText) findViewById(R.id.number_of_days_edit);
				EditText sprintTitle = (EditText) findViewById(R.id.name_of_sprint_edit);
				SprintDataHandler mDataHandler = new SprintDataHandler(activity);
				mDataHandler.insertSprint(startDayInMillis, Integer.parseInt(numberOfDays.getText().toString()), sprintTitle.getText().toString());
				
				
				SharedPreferences motivatorPrefs = getSharedPreferences(MainActivity.MOTIVATOR_PREFS, 0);
				Intent intent = new Intent(activity, MainActivity.class);
				if (!motivatorPrefs.getBoolean(Sprint.FIRST_SPRINT_SET, false)) {
					intent = new Intent(activity, IntroActivity.class);
				}
				SharedPreferences.Editor editor = motivatorPrefs.edit();
				editor.putBoolean(Sprint.FIRST_SPRINT_SET, true);
				editor.commit();
				
				
				// Clear the back stack to avoid weird behavior after the user starts a new sprint.
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); 
				startActivity(intent);
				
				Calendar notificationTime = Calendar.getInstance();
				notificationTime.set(Calendar.MINUTE, 0);
				notificationTime.set(Calendar.SECOND, 0);	
				// An alarm manager for scheduling notifications
				AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
				// Set the notification to repeat over the given time at notificationTime
				Intent notificationIntent = new Intent(activity, NotificationService.class);
				notificationIntent.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_MOOD);
				PendingIntent pendingNotificationIntent = PendingIntent.getService(activity,0,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
				alarmManager.cancel(pendingNotificationIntent);
				if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10) {
					notificationTime.add(Calendar.DATE, 1);
				}
				notificationTime.set(Calendar.HOUR_OF_DAY, 10);
				alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
				activity.finish();
			}
			
		});
	}

}
