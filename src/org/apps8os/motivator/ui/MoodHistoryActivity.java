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



import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.MoodDataHandler;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Represents the mood history of the user. In this activity, the functionality and layout
 * changes based on the orientation. Portrait orientation has the mood history of the user by day.
 * Here , previous day does not mean calendar day but rather previous day with records on database.
 * @author Toni Järvinen
 *
 */

public class MoodHistoryActivity extends Activity {
	
	private MoodDataHandler mDataHandler;
	private LayoutInflater mInflater;
	private ViewPager mViewPager;
	private DatePagerAdapter mPagerAdapterDay;
	private WeekPagerAdapter mPagerAdapterWeek;
	private DayInHistory mLastDay;
	private Calendar mLatestWeekEnd;
	private int mSelectedItem;
	
	private boolean mLoadedFirst = false;
	private boolean mLoadedFirstLandscape = false;
	
	private ArrayList<View> dateViews = new ArrayList<View>();
	private ArrayList<String> viewTitles = new ArrayList<String>();
	
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
	    mPagerAdapterDay = new DatePagerAdapter(this);
	    mViewPager.setAdapter(mPagerAdapterDay);
	    mViewPager.setCurrentItem(mSelectedItem);
	    mViewPager.setOffscreenPageLimit(3);
	    
	    if (!mLoadedFirst) {
		    // Initialize the first days to be from today.
			Calendar now = new GregorianCalendar();
		    loadSetOfDaysFrom(now.getTimeInMillis());
		    mLoadedFirst = true;
	    }
	    
	    // Set an anonymous implementation of onPageChangeListener to load more pages when needed.
	    mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				mSelectedItem = arg0;	
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {		
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// Load 3 or less more pages if we are at the end already.
				if (mSelectedItem == mPagerAdapterDay.getCount() - 2 && mLastDay != null) {
					loadSetOfDaysFrom(mLastDay.getDateInMillis());
				} else {
				}
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
	/**
	 * Gets the previous DayInHistory with moods prior to the parameter currentDay.
	 * @param currentDay
	 * @return
	 */
	private DayInHistory getPreviousDayWithMoods(DayInHistory currentDay) {
		if ( currentDay == null ) {
			return null;
		}
		Cursor previousMoods = mDataHandler.getPreviousMoodFrom(currentDay.getDateInMillis());
		if (previousMoods == null) {
			return null;
		} else {
			previousMoods.moveToFirst();
			// Initialize the DayInHistory object with the first timestamp on the constructor.
		    DayInHistory previousDayWithMoods = getDateWithMoods(previousMoods.getLong(2), previousMoods);
		    return previousDayWithMoods;
		}
	}
	
	/**
	 * Initializes the first 3 instances of DayInHistory to mDays array.
	 * Gets 3 days that have mood data from the given timestamp.
	 */
	private void loadSetOfDaysFrom(long timeStamp) {
	    // Get a cursor with latest moods for today.
	    Cursor latestMoods = mDataHandler.getPreviousMoodFrom(timeStamp);
	    DayInHistory latestDayWithMoods = null;
	    // Check if we do not have any moods.
	    if (latestMoods != null) {
		    latestMoods.moveToFirst();
		    latestDayWithMoods = getDateWithMoods(latestMoods.getLong(2), latestMoods);;
	    }
	    // Add the days to the adapter. If null, don't add more than one null page.
	    mPagerAdapterDay.addDay(latestDayWithMoods);
	    if (latestDayWithMoods == null) {
	    	mPagerAdapterDay.notifyDataSetChanged();
		    mLastDay = latestDayWithMoods;
	    } else {
		    DayInHistory next = getPreviousDayWithMoods(latestDayWithMoods);
		    mPagerAdapterDay.addDay(next);
		    if (next == null) {
			    mPagerAdapterDay.notifyDataSetChanged();
			    mLastDay = next;
		    } else {
		    	next = getPreviousDayWithMoods(next);
		    	mPagerAdapterDay.addDay(next);
		    	mPagerAdapterDay.notifyDataSetChanged();
			    mLastDay = next;
		    }
	    }
	}
	
	
	/**
	 * Adapter for the day viewpager.
	 * @author Toni Järvinen
	 *
	 */
	private class DatePagerAdapter extends PagerAdapter {
		
		private Context mContext;

		public DatePagerAdapter(Context context) {
			mContext = context;
		}

		@Override
		public int getCount() {
			return dateViews.size();
		}
		
		@Override
		public Object instantiateItem(ViewGroup viewGroup, int position) {			
			// Gets the view from the ArrayList and adds it to the group
			View view = dateViews.get(position);
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
		public void addDay(DayInHistory day) {
			ScrollView dateLayout = (ScrollView) mInflater.inflate(R.layout.element_mood_history_day_view, null);
			LinearLayout innerLayout = (LinearLayout) dateLayout.getChildAt(0);
			TextView title = (TextView) innerLayout.getChildAt(0);
			TextView comment = (TextView) innerLayout.getChildAt(2);
			// Set default page if the day is null or set the content from day object if it exists.
			if (day != null) {
				viewTitles.add(day.getDateInString(mContext));
				title.setText(R.string.your_mood);
				// DUMMY
				comment.setText("Paras päivä koskaan!");
			} else {
				// DUMMY
				viewTitles.add("");
				comment.setText(R.string.last_mood);
				comment.setGravity(Gravity.CENTER);
			}
			dateViews.add(dateLayout);
		}
		
		@Override 
		public CharSequence getPageTitle(int position) {
			return viewTitles.get(position);
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
