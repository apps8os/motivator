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


import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.services.NotificationService;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;


/**
 * Represents the main activity where the user can choose different parts of the application
 * @author Toni Järvinen
 *
 */
public class MainActivity extends FragmentActivity {
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	private int mTimeToNotify;					// Hours after midnight when to notify the user
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_activity);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.main_activity_pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		// Set the second tab as the default on launch
		mViewPager.setCurrentItem(1);
		
		mTimeToNotify = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_NOTIFICATION_INTERVAL, "12"));
		
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(SettingsActivity.KEY_SEND_NOTIFICATIONS, true)) {
			// Set up notifying user to answer to the mood question
			// The time to notify the user
			GregorianCalendar notificationTime = new GregorianCalendar();
			UtilityMethods.setToMidnight(notificationTime);
			notificationTime.add(GregorianCalendar.HOUR, mTimeToNotify);	
			// An alarm manager for scheduling notifications
			AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			// Set the notification to repeat over the given time at notificationTime
			Intent notificationIntent = new Intent(this, NotificationService.class);
			PendingIntent pendingNotificationIntent = PendingIntent.getService(this,0,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), TimeUnit.MILLISECONDS.convert(mTimeToNotify, TimeUnit.HOURS), pendingNotificationIntent);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings: 
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.

			Fragment fragment;
			// Set the second tab as today fragment
			if (position == 1) {
				fragment = new TodaySectionFragment();
			} else if(position == 2) {
				fragment = new PlanSectionFragment();
			} else if (position == 0) {
				fragment = new HistorySectionFragment();
			} else {
				return null;
			}
			return fragment;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
				case 0: {
					return getString(R.string.title_section1).toUpperCase(l);
				}
				case 1: {
					return getString(R.string.title_section2).toUpperCase(l);
				}
				case 2: {
					return getString(R.string.title_section3).toUpperCase(l);
				}
			}
			return null;
		}
	}

}
