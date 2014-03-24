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



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.MoodDataHandler;
import org.apps8os.motivator.utils.MotivatorConstants;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;

/**
 * Represents the mood history of the user. In this activity, the functionality and layout
 * changes based on the orientation. Portrait orientation has the mood history of the user by day.
 * @author Toni Järvinen
 *
 */

public class MoodHistoryActivity extends FragmentActivity {
	
	private MoodDataHandler mDataHandler;
	private ViewPager mViewPager;
	private FragmentDatePagerAdapter mPagerAdapterDay;
	private FragmentWeekPagerAdapter mPagerAdapterWeek;
	
	private static long mSprintStartDateInMillis = 1393632000000L;
	private static int mDaysInSprint = 105;
	private int mNumberOfTodayInSprint;
	private int mNumberOfWeeksInSprint;
	private int mSelectedDay;
	private int mSelectedWeek;
	private Calendar mToday;
	private static Calendar mStartDate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_history);
	    mDataHandler = new MoodDataHandler(this);
	    mDataHandler.open();
	    
	    ActionBar actionBar = getActionBar();

		
		mToday = new GregorianCalendar();
		UtilityMethods.setToMidnight(mToday);
		mToday.setFirstDayOfWeek(Calendar.MONDAY);
		
		// Gets the index/amount of days for today for the day view.
		mNumberOfTodayInSprint = (int) TimeUnit.DAYS.convert(mToday.getTimeInMillis() - mSprintStartDateInMillis, TimeUnit.MILLISECONDS);
		mNumberOfTodayInSprint += 1;
		
		if (mNumberOfTodayInSprint > mDaysInSprint) {
			mNumberOfTodayInSprint = mDaysInSprint;
		}
	    actionBar.setTitle(mNumberOfTodayInSprint + " days of glory");
	    
		mStartDate = new GregorianCalendar();
		mStartDate.setTimeInMillis(mSprintStartDateInMillis);
		mStartDate.setFirstDayOfWeek(Calendar.MONDAY);
		
		mNumberOfWeeksInSprint = mToday.get(Calendar.WEEK_OF_YEAR) - mStartDate.get(Calendar.WEEK_OF_YEAR) + 1;
		if ( mNumberOfWeeksInSprint < 0 ) {
			mNumberOfWeeksInSprint = 52 + 1 - mStartDate.get(Calendar.WEEK_OF_YEAR) + mToday.get(Calendar.WEEK_OF_YEAR);
		} else {
			
		}
		
		// Sets the default selections as today or this week.
		mSelectedDay = mNumberOfTodayInSprint - 1;
		mSelectedWeek = mNumberOfWeeksInSprint - 1;
	    mViewPager = (ViewPager) findViewById(R.id.activity_mood_history_viewpager);
	    // Load correct layout and functionality based on orientation
	    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	    	loadPortraitView();
	    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	loadLandscapeView();
	    }
	    
	}
	
	/**
	 * Loading different view when orientation is portrait or landscape.
	 */
	@Override 
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			loadPortraitView();
		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			loadLandscapeView();
		}
	}
	
	/**
	 * Loading the action bar menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.mood_history, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Actions for the menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Search functionality for the search button
		case R.id.mood_history_search:
			// Spawn a dialog fragment so that the user can select a day.
			DialogFragment dialog = new DatePickerFragment();
			dialog.show(getSupportFragmentManager(), "datePicker");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Represents the DatePicker fragment which is created when the user
	 * clicks the search button. Implements on data set listener to get the
	 * selection.
	 * @author Toni Järvinen
	 *
	 */
	public static class DatePickerFragment extends DialogFragment
		    implements DatePickerDialog.OnDateSetListener {
		
		private Calendar mToday = new GregorianCalendar();
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
			// Set the minimun and maximum dates based on the sprint and today.
			pickerDialog.getDatePicker().setMinDate(mSprintStartDateInMillis);
			pickerDialog.getDatePicker().setMaxDate(mToday.getTimeInMillis());
			return pickerDialog;
		}
		
		/**
		 * Jumps to the selected day when selected.
		 */
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar date = new GregorianCalendar();
			date.set(Calendar.YEAR, year);
			date.set(Calendar.MONTH, monthOfYear);
			date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			long dateInMillis = date.getTimeInMillis();
			// If the date is in the allowed limits, set the selected day in the activity
			if (dateInMillis >= mSprintStartDateInMillis && dateInMillis < mToday.getTimeInMillis() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
				((MoodHistoryActivity) getActivity()).setSelectedDay(dateInMillis);
			}
		}
	}
	
	@Override
	public void onResume() {
		mDataHandler.open();
		super.onResume();
	}
	
	@Override
	public void onStop() {
		mDataHandler.close();
		super.onStop();
	}
	
	/**
	 * Used to set the selected day and week of the sprint in the activity.
	 * @param dayInMillis
	 */
	public void setSelectedDay(long dayInMillis) {
		mSelectedDay = (int) TimeUnit.DAYS.convert(dayInMillis - mSprintStartDateInMillis, TimeUnit.MILLISECONDS);
		Calendar selectedDayAsCalendar = new GregorianCalendar();
		selectedDayAsCalendar.setTimeInMillis(dayInMillis);
		selectedDayAsCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		mSelectedWeek = selectedDayAsCalendar.get(Calendar.WEEK_OF_YEAR) - mStartDate.get(Calendar.WEEK_OF_YEAR);
		// Take into account change of year.
		if ( mSelectedWeek < 0 ) {
			mSelectedWeek = 52 - mStartDate.get(Calendar.WEEK_OF_YEAR) + selectedDayAsCalendar.get(Calendar.WEEK_OF_YEAR);
		} else {
		}
		// Set the day or week depending on orientation.
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mViewPager.setCurrentItem(mSelectedDay);
		} else  {
			
			mViewPager.setCurrentItem(mSelectedWeek);
		}
	}
	
	/**
	 * Loads the views and funtionality for landscape orientation.
	 * TODO: Functionality, DUMMY calendar currently
	 */
	public void loadLandscapeView() {
		mPagerAdapterWeek = new FragmentWeekPagerAdapter(getSupportFragmentManager(), this);
		mViewPager.setAdapter(mPagerAdapterWeek);
		mViewPager.setCurrentItem(mSelectedWeek);
		mViewPager.setOffscreenPageLimit(1);
	}
	
	/**
	 * Loads the views and functionality for portrait orientation.
	 * In portrait view, days are represented in a horizontially scrollable carousel.
	 */
	public void loadPortraitView() {
	    mPagerAdapterDay = new FragmentDatePagerAdapter(getSupportFragmentManager());
	    mViewPager.setAdapter(mPagerAdapterDay);
	    mViewPager.setCurrentItem(mSelectedDay);
	    mViewPager.setOffscreenPageLimit(5);
	    
	    mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				mSelectedDay = arg0;
			}
	    	
	    });
	}
	
	/**
	 * Gets a DayInHistory object for the day represented as millisecond value.
	 * @param dayInMillis
	 * @return
	 */
	protected DayInHistory getDateInHistory(long dayInMillis) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dayInMillis);
		Cursor cursor = mDataHandler.getMoodsForDay(calendar);
		// Initialize the DayInHistory object with the first timestamp on the constructor.
	    DayInHistory previousDayWithMoods = new DayInHistory(dayInMillis);
	    // Add all moods to the DayInHistory object.
	    while (cursor != null && !cursor.isClosed()) {
	    	previousDayWithMoods.addMoodLevel(cursor.getInt(0));
	    	previousDayWithMoods.addEnergyLevel(cursor.getInt(1));
	    	if (cursor.isLast()) {
	    		cursor.close();
	    	} else {
	    		cursor.moveToNext();
	    	}
	    }
	    return previousDayWithMoods;
	}
	
	
	/**
	 * Fragment adapter for the date view in the activity. Extends FragmentStatePagerAdapter so that it can destroy
	 * fragments in memory more aggressively due to the large amount of fragments.
	 * @author Toni Järvinen
	 *
	 */
	private class FragmentDatePagerAdapter extends FragmentStatePagerAdapter {
		
		public FragmentDatePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mNumberOfTodayInSprint;
		}
		
		/**
		 * Page title as the date
		 */
		@Override 
		public CharSequence getPageTitle(int position) {
			long dateInMillis = mSprintStartDateInMillis + TimeUnit.MILLISECONDS.convert(position, TimeUnit.DAYS);
			Calendar date = new GregorianCalendar();
			date.setTimeInMillis(dateInMillis);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", getResources().getConfiguration().locale);
			return sdf.format(date.getTime());
		}
		
		/**
		 * Instantiates the MoodHistoryDayFragment for the position.
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment fragment;
			DayInHistory date = getDateInHistory(mSprintStartDateInMillis + TimeUnit.MILLISECONDS.convert(position, TimeUnit.DAYS));
			fragment = new MoodHistoryDayFragment();
			Bundle b = new Bundle();
			// Send the DayInHistory for the fragment as parcelable.
			b.putParcelable(MotivatorConstants.DAY_IN_HISTORY, date);
			fragment.setArguments(b);
			return fragment;
		}
		
	}
	
	/**
	 * Fragment adapter for the week view in the activity.
	 * @author Toni Järvinen
	 *
	 */
	private class FragmentWeekPagerAdapter extends FragmentPagerAdapter {

		private Context mContext;

		public FragmentWeekPagerAdapter(FragmentManager fm, Context context) {
			super(fm);
			mContext = context;
		}

		@Override
		public int getCount() {
			return mNumberOfWeeksInSprint;
		}
		
		
		@Override 
		public CharSequence getPageTitle(int position) {
			long dateInMillis = mSprintStartDateInMillis + (TimeUnit.MILLISECONDS.convert(position, TimeUnit.DAYS) * 7);
			Calendar date = new GregorianCalendar();
			date.setTimeInMillis(dateInMillis);
			return "Week " + date.get(Calendar.WEEK_OF_YEAR);
		}
		
		/**
		 * Instantiates the MoodHistoryWeekFragment for the position.
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment fragment;
			Calendar calendar = new GregorianCalendar();
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.setTimeInMillis(mSprintStartDateInMillis);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			ArrayList<DayInHistory> days = new ArrayList<DayInHistory>();
			calendar.add(Calendar.DATE, position * 7);
			while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				DayInHistory date = getDateInHistory(calendar.getTimeInMillis());
				days.add(date);
				calendar.add(Calendar.DATE, 1);
			}
			DayInHistory date = getDateInHistory(calendar.getTimeInMillis());
			days.add(date);
			// Puts the last day in this week as millisecond value to the arguments.
			fragment = new MoodHistoryWeekFragment();
			Bundle b = new Bundle();
			b.putParcelableArrayList(MotivatorConstants.DAY_IN_HISTORY_ARRAY, days);
			fragment.setArguments(b);
			return fragment;
		}
		
	}

}
