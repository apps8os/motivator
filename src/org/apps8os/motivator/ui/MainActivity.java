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
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.data.SprintDataHandler;
import org.apps8os.motivator.services.NotificationService;

import com.viewpagerindicator.TitlePageIndicator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Represents the main activity where the user can choose different parts of the application
 * @author Toni Järvinen
 *
 */
public class MainActivity extends Activity {
	
	public static final String MOTIVATOR_PREFS = "motivator_prefs";
	public static final String APP_VERSION = "application_version";
	private static final String SEEN_HELP = "seen_help";
	
	SectionsPagerAdapter mSectionsPagerAdapter;
	private String mTimeToNotify;					// Hours after midnight when to notify the user
	private Sprint mCurrentSprint;
	private SprintDataHandler mSprintDataHandler;
	private Resources mRes;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private ActionBar mActionBar;
	private boolean mHelpIsVisible = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_activity);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getFragmentManager());
		
		mSprintDataHandler = new SprintDataHandler(this);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.main_activity_pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setCurrentItem(1, false);
		
		mRes = getResources();
		mActionBar = getActionBar();
		
		//Bind the title indicator to the adapter
		final TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setBackgroundResource(R.color.light_gray);
		titleIndicator.setViewPager(mViewPager);
		
		// Listener for changing the actionbar color based on the fragment
		OnPageChangeListener pageChangeListener = new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 2) {
					mActionBar.setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_blue));
					mActionBar.setDisplayShowTitleEnabled(false);
					mActionBar.setDisplayShowTitleEnabled(true);
					titleIndicator.setFooterColor(mRes.getColor(R.color.actionbar_blue));
					//titleIndicator.setBackgroundResource(R.drawable.action_bar_blue);
				} else if (arg0 == 1) {
					mActionBar.setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_green));
					mActionBar.setDisplayShowTitleEnabled(false);
					mActionBar.setDisplayShowTitleEnabled(true);
					titleIndicator.setFooterColor(mRes.getColor(R.color.actionbar_green));
					//titleIndicator.setBackgroundResource(R.drawable.action_bar_green);
				} else if (arg0 == 0) {
					mActionBar.setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_orange));
					mActionBar.setDisplayShowTitleEnabled(false);
					mActionBar.setDisplayShowTitleEnabled(true);
					titleIndicator.setFooterColor(mRes.getColor(R.color.actionbar_orange));
					//titleIndicator.setBackgroundResource(R.drawable.action_bar_orange);
				}
			}
		};
		
		titleIndicator.setOnPageChangeListener(pageChangeListener);
		
		// Check the version number and set notifications again if version has changed.
		int versionNumber = -99;
		try {
			versionNumber = this.getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			versionNumber = -100;
			e.printStackTrace();
			Log.e("notification", "version number not found");
		}
		
		SharedPreferences motivatorPrefs = getSharedPreferences(MOTIVATOR_PREFS, 0);
		if( versionNumber != motivatorPrefs.getInt(APP_VERSION, -1)) {
			setNotifications();
			SharedPreferences.Editor editor = motivatorPrefs.edit();
			editor.putInt(APP_VERSION, versionNumber);
			editor.commit();
		}
		
		if (!motivatorPrefs.getBoolean(Sprint.FIRST_SPRINT_SET, false)) {	
			Intent intent = new Intent(this, StartingSprintActivity.class);
			finish();
			startActivity(intent);
		}
		if (!motivatorPrefs.getBoolean(SEEN_HELP, false)) {
			showHelp();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mCurrentSprint = mSprintDataHandler.getCurrentSprint();
		
		if (mCurrentSprint == null) {
			mCurrentSprint = mSprintDataHandler.getLatestEndedSprint();
		} else  {
			mActionBar.setSubtitle(mCurrentSprint.getSprintTitle());
			mActionBar.setTitle(getString(R.string.day) + " " + mCurrentSprint.getCurrentDayOfTheSprint() + "/" + mCurrentSprint.getDaysInSprint());
		}
		
		SharedPreferences motivatorPrefs = getSharedPreferences(MOTIVATOR_PREFS, 0);
		int eventAdded = motivatorPrefs.getInt(AddEventActivity.EVENT_ADDED, -1);
		if (eventAdded == MotivatorEvent.TODAY) {
			mViewPager.setCurrentItem(1);
			Editor editor = motivatorPrefs.edit();
			editor.putInt(AddEventActivity.EVENT_ADDED, -1);
			editor.commit();
		} else if (eventAdded == MotivatorEvent.PLAN){
			mViewPager.setCurrentItem(2);
			Editor editor = motivatorPrefs.edit();
			editor.putInt(AddEventActivity.EVENT_ADDED, -1);
			editor.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_screen, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_settings: 
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_start_sprint:
			intent = new Intent(this, StartingSprintActivity.class);
			startActivity(intent);
			finish();
			return true;
		case R.id.action_show_help:
			showHelp();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void showHelp() {
		if (!mHelpIsVisible ) {
			mHelpIsVisible = true;
			mViewPager.setCurrentItem(1);
			final FrameLayout contentRoot = (FrameLayout) findViewById(R.id.root_view);
			// Inflate the help overlay to the fragment.
			getLayoutInflater().inflate(R.layout.element_help_overlay, contentRoot, true);
			
			final TextView helpTitle = (TextView) contentRoot.findViewById(R.id.help_overlay_title);
			helpTitle.setText(getString(R.string.today_section));
			final TextView helpText = (TextView) contentRoot.findViewById(R.id.help_overlay_subtitle);
			helpText.setText(getString(R.string.today_section_help));
			final LinearLayout helpBackground = (LinearLayout) contentRoot.findViewById(R.id.help_text_background);
			helpBackground.setBackgroundResource(R.color.actionbar_green);
			((Button) contentRoot.findViewById(R.id.help_overlay_button)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mViewPager.getCurrentItem() == 0) {
						final View helpOverlay = (View) contentRoot.findViewById(R.id.help_overlay);
						helpOverlay.animate()
							.alpha(0f)
							.setDuration(500)
							.setListener(new AnimatorListenerAdapter() {
								
								// Set the visibility to gone when animation has ended.
								@Override
								public void onAnimationEnd(Animator animation) {
									helpOverlay.setVisibility(View.GONE);
									contentRoot.removeView(helpOverlay);
								}
							});
						mViewPager.setCurrentItem(1);
						
						SharedPreferences motivatorPrefs = getSharedPreferences(MOTIVATOR_PREFS, 0);
						SharedPreferences.Editor editor = motivatorPrefs.edit();
						editor.putBoolean(SEEN_HELP, true);
						editor.commit();
						mHelpIsVisible = false;
					} else if (mViewPager.getCurrentItem() == 1){
						mViewPager.setCurrentItem(2);
						helpTitle.setText(getString(R.string.plan_section));
						helpText.setText(getString(R.string.plan_section_help));
						helpBackground.setBackgroundResource(R.color.actionbar_blue);
					} else {
						mViewPager.setCurrentItem(0);
						helpTitle.setText(getString(R.string.history_section));
						helpText.setText(getString(R.string.history_section_help));
						helpBackground.setBackgroundResource(R.color.actionbar_orange);
						((Button) contentRoot.findViewById(R.id.help_overlay_button)).setText(getString(R.string.ok));
					}
				}
			});
		}
	}
	
	/**
	 * Set the notifications for the first time. After this, the notifications are controlled from the settings activity.
	 */
	public void setNotifications() {
		mTimeToNotify = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_NOTIFICATION_INTERVAL, getResources().getString(R.string.in_the_morning_value));
		// Set up notifying user to answer to the mood question
		// The time to notify the user
		GregorianCalendar notificationTime = new GregorianCalendar();
		notificationTime.set(Calendar.MINUTE, 0);
		notificationTime.set(Calendar.SECOND, 0);	
		// An alarm manager for scheduling notifications
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		// Set the notification to repeat over the given time at notificationTime
		Intent notificationIntent = new Intent(this, NotificationService.class);
		notificationIntent.putExtra(NotificationService.NOTIFICATION_TYPE, NotificationService.NOTIFICATION_MOOD);
		PendingIntent pendingNotificationIntent = PendingIntent.getService(this,0,notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager.cancel(pendingNotificationIntent);
		if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10) {
			notificationTime.add(Calendar.DATE, 1);
		}
		notificationTime.set(Calendar.HOUR_OF_DAY, 10);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
		/**
		if (mTimeToNotify == getString(R.string.in_the_morning_value)) {
			if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10) {
				notificationTime.add(Calendar.DATE, 1);
			}
			notificationTime.set(Calendar.HOUR_OF_DAY, 10);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingNotificationIntent);
		} else if (mTimeToNotify == getString(R.string.morning_and_evening_value)) {
			if (notificationTime.get(Calendar.HOUR_OF_DAY) >= 10 && notificationTime.get(Calendar.HOUR_OF_DAY) < 22) {
				notificationTime.set(Calendar.HOUR_OF_DAY, 22);
			} else if (notificationTime.get(Calendar.HOUR_OF_DAY) < 10) {
				notificationTime.set(Calendar.HOUR_OF_DAY, 10);
			} else {
				notificationTime.add(Calendar.DATE, 1);
				notificationTime.set(Calendar.HOUR_OF_DAY, 10);
			}
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, pendingNotificationIntent);
		} else if (mTimeToNotify == getString(R.string.every_hour_value)) {
			notificationTime.add(Calendar.HOUR_OF_DAY, 1);
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, pendingNotificationIntent);
		}
		**/
	}
	
	public void visitWebsite(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.paihdeneuvonta.fi"));
		startActivity(browserIntent);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	private class SectionsPagerAdapter extends FragmentPagerAdapter {
		
		private Bundle mBundle = new Bundle();

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
			if (position == 1) {
				mBundle.clear();
				mBundle.putParcelable(Sprint.CURRENT_SPRINT, mCurrentSprint);
				fragment = new TodaySectionFragment();
				fragment.setArguments(mBundle);
			} else if(position == 2) {
				fragment = new PlanSectionFragment();
			} else if (position == 0) {
				mBundle.clear();
				mBundle.putParcelable(Sprint.CURRENT_SPRINT, mCurrentSprint);
				fragment = new HistorySectionFragment();
				fragment.setArguments(mBundle);
			} else {
				return null;
			}
			fragment.setRetainInstance(true);
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
