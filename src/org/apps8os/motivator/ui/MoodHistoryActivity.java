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
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Represents the mood history of the user. In this activity, the functionality and layout
 * changes based on the orientation. Portrait orientation has the mood history of the user by day.
 * Here , previous day does not mean calendar day but rather previous day with records on database.
 * @author Toni Järvinen
 *
 */

public class MoodHistoryActivity extends FragmentActivity {
	
	private MoodDataHandler mDataHandler;
	private LayoutInflater mInflater;
	private ViewPager mViewPager;
	private FragmentDatePagerAdapter mPagerAdapterDay;
	private WeekPagerAdapter mPagerAdapterWeek;
	private Calendar mLatestWeekEnd;
	
	private static long mSprintStartDateInMillis = 1393632000000L;
	private int mIndexOfTodayInSprint;
	private int mSelectedDay;
	private static Calendar mToday;
	
	private boolean mLoadedFirstLandscape = false;

	private ArrayList<View> weekViews = new ArrayList<View>();
	private ArrayList<String> weekTitles = new ArrayList<String>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_history);
	    mDataHandler = new MoodDataHandler(this);
	    mDataHandler.open();
		mInflater = getLayoutInflater();
		
		mToday = new GregorianCalendar();
		mIndexOfTodayInSprint = (int) TimeUnit.DAYS.convert(mToday.getTimeInMillis() - mSprintStartDateInMillis, TimeUnit.MILLISECONDS);
		mIndexOfTodayInSprint += 1;
		
		mSelectedDay = mIndexOfTodayInSprint;
		
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.mood_history, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mood_history_search:
			DialogFragment dialog = new DatePickerFragment();
			dialog.show(getSupportFragmentManager(), "datePicker");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	public static class DatePickerFragment extends DialogFragment
		    implements DatePickerDialog.OnDateSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
			DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
			pickerDialog.getDatePicker().setMinDate(mSprintStartDateInMillis);
			pickerDialog.getDatePicker().setMaxDate(mToday.getTimeInMillis());
			return pickerDialog;
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar date = new GregorianCalendar();
			date.set(Calendar.YEAR, year);
			date.set(Calendar.MONTH, monthOfYear);
			date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			long dateInMillis = date.getTimeInMillis();
			if (dateInMillis >= mSprintStartDateInMillis && dateInMillis <= mToday.getTimeInMillis()) {
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
	
	
	public void setSelectedDay(long dayInMillis) {
		mSelectedDay = (int) TimeUnit.DAYS.convert(dayInMillis - mSprintStartDateInMillis, TimeUnit.MILLISECONDS);
		mViewPager.setCurrentItem(mSelectedDay);
	}
	
	/**
	 * Loads the views and funtionality for landscape orientation.
	 * TODO: Functionality, DUMMY calendar currently
	 */
	public void loadLandscapeView() {
		mPagerAdapterWeek = new WeekPagerAdapter(this);
		mViewPager.setAdapter(mPagerAdapterWeek);
		
	    if (!mLoadedFirstLandscape) {
		    // Initialize the first days to be from today.
			mLatestWeekEnd = new GregorianCalendar();
		    mLoadedFirstLandscape = true;
		    for (int i = 0; i < 3; i++) {
		    	mPagerAdapterWeek.addPreviousWeek();
		    	mPagerAdapterWeek.notifyDataSetChanged();
		    }
	    }
	}
	
	/**
	 * Loads the views and functionality for portrait orientation.
	 * In portrait view, days are represented in a horizontially scrollable carousel.
	 */
	public void loadPortraitView() {
	    mPagerAdapterDay = new FragmentDatePagerAdapter(getSupportFragmentManager());
	    mViewPager.setAdapter(mPagerAdapterDay);
	    mViewPager.setCurrentItem(mSelectedDay);
	    mViewPager.setOffscreenPageLimit(3);
	    
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
	
	private DayInHistory getDateWithMoods(long dayInMillis, Cursor cursor) {
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
	
	private DayInHistory getDateInHistory(long dayInMillis) {
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
	 * Adapter for the day viewpager.
	 * @author Toni Järvinen
	 *
	 */
	private class FragmentDatePagerAdapter extends FragmentStatePagerAdapter {
		
		public FragmentDatePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return mIndexOfTodayInSprint;
		}
		
		
		@Override 
		public CharSequence getPageTitle(int position) {
			long dateInMillis = mSprintStartDateInMillis + TimeUnit.MILLISECONDS.convert(position, TimeUnit.DAYS);
			Calendar date = new GregorianCalendar();
			date.setTimeInMillis(dateInMillis);
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", getResources().getConfiguration().locale);
			return sdf.format(date.getTime());
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment;
			DayInHistory date = getDateInHistory(mSprintStartDateInMillis + TimeUnit.MILLISECONDS.convert(position, TimeUnit.DAYS));
			fragment = new MoodHistoryDayFragment();
			Bundle b = new Bundle();
			b.putParcelable(MotivatorConstants.DAY_IN_HISTORY, date);
			fragment.setArguments(b);
			return fragment;
		}
		
	}
	
	/**
	 * Adapter for the day viewpager.
	 * @author Toni Järvinen
	 *
	 */
	private class WeekPagerAdapter extends PagerAdapter {
		
		private Context mContext;

		public WeekPagerAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return weekViews.size();
		}
		
		@Override
		public Object instantiateItem(ViewGroup viewGroup, int position) {			
			// Gets the view from the ArrayList and adds it to the group
			View view = weekViews.get(position);
			if (view.getParent() != null) {
				((ViewGroup) view.getParent()).removeView(view);
			}
			viewGroup.addView(view);
			return view;
		}
		/**
		 * Adds a view representing a day to the adapter list of views.
		 * @param day
		 */
		public void addPreviousWeek() {
			RelativeLayout weekLayout = (RelativeLayout) mInflater.inflate(R.layout.element_mood_history_landscape, null);
			mLatestWeekEnd.setFirstDayOfWeek(Calendar.MONDAY);
			int currentDayOfWeek = mLatestWeekEnd.get(Calendar.DAY_OF_WEEK);
			mLatestWeekEnd.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			int daysInThisWeek = currentDayOfWeek - mLatestWeekEnd.get(Calendar.DAY_OF_WEEK);
			if (daysInThisWeek < 0) {
				daysInThisWeek = 6;
			}
			LinearLayout dayLayout = (LinearLayout) weekLayout.getChildAt(1);
			for (int i = 0; i <= daysInThisWeek; i++) {
				Cursor moods = mDataHandler.getMoodsForDay(mLatestWeekEnd);
				DayInHistory day = getDateWithMoods(mLatestWeekEnd.getTimeInMillis(), moods);
				WeekDayView dayView = (WeekDayView) mInflater.inflate(R.layout.element_mood_history_week_day, dayLayout, false);
				dayView.setDay(day);
				dayLayout.addView(dayView);
				mLatestWeekEnd.add(Calendar.DATE, 1);
			}
			mLatestWeekEnd.add(Calendar.DATE, (-daysInThisWeek - 1));
			weekTitles.add("Week " + mLatestWeekEnd.get(Calendar.WEEK_OF_YEAR));
			mLatestWeekEnd.add(Calendar.DATE, -1);
			weekViews.add(weekLayout);
		}
		
		@Override 
		public CharSequence getPageTitle(int position) {
			return weekTitles.get(position);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		@Override
		public void destroyItem(ViewGroup viewGroup, int position, Object object) {
			viewGroup.removeView((View) object);
		}
		
	}

}
