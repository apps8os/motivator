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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.MoodDataHandler;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.data.SprintDataHandler;
import org.apps8os.motivator.utils.UtilityMethods;

import com.viewpagerindicator.TitlePageIndicator;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.DatePicker;

/**
 * Represents the mood history of the user. In this activity, the functionality and layout
 * changes based on the orientation. Portrait orientation has the mood history of the user by day.
 * @author Toni Järvinen
 *
 */

public class MoodHistoryActivity extends Activity {
	
	public static final String FRAGMENT_POSITION = "fragment_position";
	
	private MoodDataHandler mMoodDataHandler;
	private ViewPager mViewPager;
	private FragmentDatePagerAdapter mPagerAdapterDay;
	private FragmentWeekPagerAdapter mPagerAdapterWeek;
	private Sprint mCurrentSprint;
	private Locale mLocale;
	private SimpleDateFormat mDateFormat;
	
	
	private static long mSprintStartDateInMillis = 1393632000000L;
	private static long mSprintEndDateInMillis;
	private static Calendar mSprintStartDate;
	private static int mDaysInSprint = 200;
	private int mNumberOfTodayInSprint;
	private int mNumberOfWeeksInSprint;
	private int mSelectedDay;
	private int mSelectedWeek;
	private Calendar mToday;
	private String[] mDayPageTitles;
	private String[] mWeekPageTitles;
	private Menu mMenu;

	private TitlePageIndicator titleIndicator;
	private static Calendar mStartDate;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_history);
	    mMoodDataHandler = new MoodDataHandler(this);
	    
	    mCurrentSprint = getIntent().getExtras().getParcelable(Sprint.CURRENT_SPRINT);
	    
	    mSprintStartDateInMillis = mCurrentSprint.getStartTime();
	    mSprintStartDate = new GregorianCalendar();
	    mSprintStartDate.setTimeInMillis(mCurrentSprint.getStartTime());
	    
	    mDaysInSprint = mCurrentSprint.getDaysInSprint();
	    ActionBar actionBar = getActionBar();

		mToday = new GregorianCalendar();
		UtilityMethods.setToDayStart(mToday);
		mToday.setFirstDayOfWeek(Calendar.MONDAY);
		
		mNumberOfTodayInSprint = mCurrentSprint.getCurrentDayOfTheSprint();
		
		// Check if the sprint is already over.
		if (mNumberOfTodayInSprint > mDaysInSprint) {
			mNumberOfTodayInSprint = mDaysInSprint;
		}
		actionBar.setSubtitle(mCurrentSprint.getSprintTitle());
	    actionBar.setTitle(mNumberOfTodayInSprint + " " + getString(R.string.days));
	    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_orange_no_separator));
	    
		mStartDate = new GregorianCalendar();
		mStartDate.setFirstDayOfWeek(Calendar.MONDAY);
		mStartDate.setTimeInMillis(mSprintStartDateInMillis);
		
		// Calculate the number of weeks in the sprint
		mNumberOfWeeksInSprint = mToday.get(Calendar.WEEK_OF_YEAR) - mStartDate.get(Calendar.WEEK_OF_YEAR) + 1;
		if ( mNumberOfWeeksInSprint < 0 ) {
			mNumberOfWeeksInSprint = 52 + 1 - mStartDate.get(Calendar.WEEK_OF_YEAR) + mToday.get(Calendar.WEEK_OF_YEAR);
		} else {
			
		}
		
		// Sets the default selections as today or this week.
		mSelectedDay = mNumberOfTodayInSprint - 1;
		mSelectedWeek = mNumberOfWeeksInSprint - 1;
	    mViewPager = (ViewPager) findViewById(R.id.activity_mood_history_viewpager);
	    titleIndicator = (TitlePageIndicator)findViewById(R.id.indicator);
	    
	    // Page change listener to keep the selected week and day in a member.
	    titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			    	mSelectedDay = arg0;
			    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			    	mSelectedWeek = arg0;
			    }
			}
	    });
	    
	    setPageTitles();
	    
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
		// Selecting the different attributes in the week view, not visible in portrait day view.
		MenuItem selectAttribute = mMenu.findItem(R.id.mood_history_select_attribute);
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			loadPortraitView();
			selectAttribute.setVisible(false);
		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			loadLandscapeView();
			selectAttribute.setVisible(true);
		}
	}
	
	/**
	 * Loading the action bar menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.mood_history, menu);
		mMenu = menu;
	    MenuItem selectAttribute = mMenu.findItem(R.id.mood_history_select_attribute);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			selectAttribute.setVisible(false);
		} else {
			selectAttribute.setVisible(true);
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Actions for the menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		MoodHistoryWeekFragment weekFragment;
		switch (item.getItemId()) {
		// Search functionality for the search button
		case R.id.mood_history_search:
			// Spawn a dialog fragment so that the user can select a day.
			DialogFragment dialog = new DatePickerFragment();
			dialog.show(getFragmentManager(), "datePicker");
			return true;
		case R.id.mood_history_attribute_drinking:
			// Setting the selected attribute in landscape view.
			weekFragment = mPagerAdapterWeek.getWeekFragment(mViewPager.getCurrentItem());
			weekFragment.updateSelectedAttribute(DayInHistory.AMOUNT_OF_DRINKS);
			return true;
		case R.id.mood_history_attribute_all:
			// Setting the selected attribute in landscape view.
			weekFragment = mPagerAdapterWeek.getWeekFragment(mViewPager.getCurrentItem());
			weekFragment.updateSelectedAttribute(DayInHistory.ALL);
			return true;
		case R.id.mood_history_change_sprint:
			// Spawn a dialog where the user can select the sprint depicted in this history.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final Sprint[] sprints = new SprintDataHandler(this).getSprints();
			// Get the string representations of the sprints.
			String[] sprintsAsString = new String[sprints.length];
			for (int i = 0; i < sprints.length; i++) {
				sprintsAsString[i] = sprints[i].getSprintTitle() + " " + sprints[i].getStartTimeInString(this);
			}
			builder.setTitle("Select sprint").setSingleChoiceItems(sprintsAsString, sprints.length-1, null);
			final AlertDialog alertDialog = builder.create();
			final Activity thisActivity = this;
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Start this activity again with the selected sprint as the passed Parcelable Sprint.
					// This is done so that the activity can update itself to the selected sprint.
					mCurrentSprint = sprints[alertDialog.getListView().getCheckedItemPosition()];
					Intent intent = new Intent (thisActivity, MoodHistoryActivity.class);
					intent.putExtra(Sprint.CURRENT_SPRINT, mCurrentSprint);
					startActivity(intent);
					alertDialog.dismiss();
					thisActivity.finish();
				}
			});
			alertDialog.show();
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
			pickerDialog.setTitle(getString(R.string.select_a_day));
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
		super.onResume();
	}
	
	/**
	 * Used to set the selected day and week of the sprint in the activity.
	 * @param dayInMillis
	 */
	public void setSelectedDay(long dayInMillis) {
		// Calculate the selected day position in the viewpager
		mSelectedDay = (int) TimeUnit.DAYS.convert(dayInMillis - mSprintStartDateInMillis, TimeUnit.MILLISECONDS);
		Calendar selectedDayAsCalendar = new GregorianCalendar();
		selectedDayAsCalendar.setFirstDayOfWeek(Calendar.MONDAY);
		selectedDayAsCalendar.setTimeInMillis(dayInMillis);
		// Calculate the selected day position as week in the viewpager
		mSelectedWeek = selectedDayAsCalendar.get(Calendar.WEEK_OF_YEAR) - mStartDate.get(Calendar.WEEK_OF_YEAR);
		// Take into account change of year.
		if ( mSelectedWeek < 0 ) {
			mSelectedWeek = 52 - mStartDate.get(Calendar.WEEK_OF_YEAR) + selectedDayAsCalendar.get(Calendar.WEEK_OF_YEAR);
		} else {
		}
		// Set the day or week depending on orientation.
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			mViewPager.setCurrentItem(mSelectedDay);
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mViewPager.setCurrentItem(mSelectedWeek);
		}
	}
	
	/**
	 * Load the page titles to an array. This done so the Title Pager Indicator does not have to construct
	 * them multiple times when changing pages.
	 */
	public void setPageTitles() {
		mDayPageTitles = new String[mNumberOfTodayInSprint];
		mLocale = getResources().getConfiguration().locale;
		mDateFormat = new SimpleDateFormat("dd.MM.yyyy", mLocale);
	    for (int i = 0; i < mDayPageTitles.length;i++) {
	    	Calendar date = (Calendar) mStartDate.clone();
			date.add(Calendar.DATE, i);
			mDayPageTitles[i] = mDateFormat.format(date.getTime());
	    }
	    
	    mWeekPageTitles = new String[mNumberOfWeeksInSprint];
	    for (int i = 0; i < mWeekPageTitles.length;i++) {
	    	long dateInMillis = mSprintStartDateInMillis + (TimeUnit.MILLISECONDS.convert(i, TimeUnit.DAYS) * 7);
			Calendar date = new GregorianCalendar();
			date.setTimeInMillis(dateInMillis);
			mWeekPageTitles[i] =  getString(R.string.week)+ " " + date.get(Calendar.WEEK_OF_YEAR);
	    }
	}
	
	/**
	 * Loads the views and funtionality for landscape orientation.
	 */
	public void loadLandscapeView() {
		mPagerAdapterWeek = new FragmentWeekPagerAdapter(getFragmentManager(), this);
		mViewPager.setAdapter(mPagerAdapterWeek);
		titleIndicator.setViewPager(mViewPager);
		mViewPager.setCurrentItem(mSelectedWeek);
		mViewPager.setOffscreenPageLimit(1);
	}
	
	/**
	 * Loads the views and functionality for portrait orientation.
	 * In portrait view, days are represented in a horizontially scrollable carousel.
	 */
	public void loadPortraitView() {
	    mPagerAdapterDay = new FragmentDatePagerAdapter(getFragmentManager());
	    mViewPager.setAdapter(mPagerAdapterDay);
		titleIndicator.setViewPager(mViewPager);
	    mViewPager.setCurrentItem(mSelectedDay);
	    mViewPager.setOffscreenPageLimit(5);
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
			return mDayPageTitles[position];
		}
		
		/**
		 * Instantiates the MoodHistoryDayFragment for the position.
		 */
		@Override
		public MoodHistoryDayFragment getItem(int position) {
			MoodHistoryDayFragment fragment;
			// Get a DayInHistory for the date represented by this position. Calculated from the start date.
			DayInHistory date = mMoodDataHandler.getDayInHistory(mSprintStartDateInMillis + TimeUnit.MILLISECONDS.convert(position, TimeUnit.DAYS));
			fragment = new MoodHistoryDayFragment();
			Bundle b = new Bundle();
			// Send the DayInHistory for the fragment as parcelable.
			b.putParcelable(DayInHistory.DAY_IN_HISTORY, date);
			fragment.setArguments(b);
			
			fragment.setRetainInstance(true);
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
		private SparseArray<MoodHistoryWeekFragment> mWeekFragments = new SparseArray<MoodHistoryWeekFragment>();

		public FragmentWeekPagerAdapter(FragmentManager fm, Context context) {
			super(fm);
			mContext = context;
		}

		@Override
		public int getCount() {
			return mNumberOfWeeksInSprint;
		}
		
		/**
		 * Page title with the week number.
		 */
		@Override 
		public CharSequence getPageTitle(int position) {
			/*
			long dateInMillis = mSprintStartDateInMillis + (TimeUnit.MILLISECONDS.convert(position, TimeUnit.DAYS) * 7);
			Calendar date = new GregorianCalendar();
			date.setTimeInMillis(dateInMillis);
			return getString(R.string.week)+ " " + date.get(Calendar.WEEK_OF_YEAR);
			*/
			return mWeekPageTitles[position];
		}
		
		/**
		 * Instantiates the MoodHistoryWeekFragment for the position. Gives the current sprint start as milliseconds and
		 * the fragments position as extras in a bundle.
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment fragment;
			fragment = new MoodHistoryWeekFragment();
			Bundle b = new Bundle();

			b.putLong(Sprint.CURRENT_SPRINT_STARTDATE, mSprintStartDateInMillis);
			b.putInt(FRAGMENT_POSITION, position);
			
			fragment.setArguments(b);
			
			fragment.setRetainInstance(true);
			return fragment;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			MoodHistoryWeekFragment weekFragment = (MoodHistoryWeekFragment) super.instantiateItem(container, position);
			mWeekFragments.put(position, weekFragment);
			return weekFragment;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			mWeekFragments.remove(position);
		}
		
		public MoodHistoryWeekFragment getWeekFragment(int position) {
			return mWeekFragments.get(position);
		}
		
	}

}
