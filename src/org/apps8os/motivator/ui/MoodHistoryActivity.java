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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Represents the mood history of the user by day. Here, previous day does not mean calendar day but rather
 * previous day with records on database.
 * @author Toni Järvinen
 *
 */

public class MoodHistoryActivity extends Activity {
	
	private MoodDataHandler mDataHandler;
	private LayoutInflater mInflater;
	private ViewPager mViewPager;
	private DatePagerAdapter mPagerAdapter;
	private DayInHistory mLastDay;
	private int mSelectedItem;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_history);
	    
	    
	    mDataHandler = new MoodDataHandler(this);
	    mDataHandler.open();
	    
	    mInflater = getLayoutInflater();
	    mViewPager = (ViewPager) findViewById(R.id.activity_mood_history_viewpager);
	    mPagerAdapter = new DatePagerAdapter(this);
	    mViewPager.setAdapter(mPagerAdapter);
	    mViewPager.setCurrentItem(0);
	    mViewPager.setOffscreenPageLimit(3);
	    
	    // Initialize the first days to be from today.
		Calendar tomorrow = new GregorianCalendar();
	    tomorrow.add(Calendar.DATE, 1);
	    loadSetOfDaysFrom(tomorrow);
	    
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
				if (mSelectedItem == mPagerAdapter.getCount() - 1 && mLastDay != null) {
					loadSetOfDaysFrom(mLastDay.getDate());
				} else {
				}
			}
			
		});
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
	 * Gets the previous DayInHistory with moods prior to the parameter currentDay.
	 * @param currentDay
	 * @return
	 */
	private DayInHistory getPreviousDayWithMoods(DayInHistory currentDay) {
		if ( currentDay == null ) {
			return null;
		}
		Cursor previousMoods = mDataHandler.getPreviousMoodFrom(currentDay.getDate());
		if (previousMoods == null) {
			return null;
		} else {
			previousMoods.moveToFirst();
			// Initialize the DayInHistory object with the first timestamp on the constructor.
		    DayInHistory previousDayWithMoods = new DayInHistory(previousMoods.getLong(2));
		    // Add all moods to the DayInHistory object.
		    while (!previousMoods.isClosed()) {
		    	previousDayWithMoods.addMoodLevel(previousMoods.getInt(0));
		    	previousDayWithMoods.addEnergyLevel(previousMoods.getInt(1));
		    	if (previousMoods.isLast()) {
		    		previousMoods.close();
		    	} else {
		    		previousMoods.moveToNext();
		    	}
		    }
		    return previousDayWithMoods;
		}
	}
	
	/**
	 * Initializes the first 3 instances of DayInHistory to mDays array.
	 * Gets 3 latest days with mood answers.
	 */
	private void loadSetOfDaysFrom(Calendar calendar) {
	    // Get a cursor with latest moods for today.
	    Cursor latestMoods = mDataHandler.getPreviousMoodFrom(calendar);
	    DayInHistory latestDayWithMoods = null;
	    // Check if we do not have any moods.
	    if (latestMoods != null) {
		    latestMoods.moveToFirst();
		    latestDayWithMoods = new DayInHistory(latestMoods.getLong(2));
		    // Get all the moods of the day.
		    while (!latestMoods.isClosed()) {
		    	latestDayWithMoods.addMoodLevel(latestMoods.getInt(0));
		    	latestDayWithMoods.addEnergyLevel(latestMoods.getInt(1));
		    	if (latestMoods.isLast()) {
		    		latestMoods.close();
		    	} else {
		    		latestMoods.moveToNext();
		    	}
		    }
	    }
	    
	    // Add the days to the adapter. If null, don't add more than one null page.
	    mPagerAdapter.addDay(latestDayWithMoods);
	    if (latestDayWithMoods == null) {
	    	mPagerAdapter.notifyDataSetChanged();
		    mLastDay = latestDayWithMoods;
	    } else {
		    DayInHistory next = getPreviousDayWithMoods(latestDayWithMoods);
		    mPagerAdapter.addDay(next);
		    if (next == null) {
			    mPagerAdapter.notifyDataSetChanged();
			    mLastDay = next;
		    } else {
		    	next = getPreviousDayWithMoods(next);
		    	mPagerAdapter.addDay(next);
		    	mPagerAdapter.notifyDataSetChanged();
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
		private ArrayList<View> dateViews = new ArrayList<View>();

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
				title.setText(day.getDateInString(mContext));
				// DUMMY
				comment.setText("Paras päivä koskaan!");
			} else {
				// DUMMY
				comment.setText("Answer more to the mood questions!");
				comment.setGravity(Gravity.CENTER);
			}
			dateViews.add(dateLayout);
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
