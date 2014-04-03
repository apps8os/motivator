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
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.MoodDataHandler;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

/**
 * Represents a activity where the user can view the average moods in relation to other data.
 * @author Toni Järvinen
 *
 */
public class MoodRelationHistoryActivity extends Activity {
	
	private long mFromTimeInMillis;
	private long mEndTimeInMillis;
	private int mAmountOfDays;
	private String mCaseSelector;
	private MoodDataHandler mDataHandler;
	private int mAvgMood = 0;
	private TextView mAvgMoodTextView;
	private Context mContext;
	
	private static final String AMOUNT_OF_DRINKS = "amount_of_drinks";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_relation_history);
	    SpinnerAdapter adapter = ArrayAdapter.createFromResource(this, R.array.time_frames, android.R.layout.simple_spinner_dropdown_item);
	    
	    mCaseSelector = AMOUNT_OF_DRINKS;
	    mDataHandler = new MoodDataHandler(this);
	    mContext = this;
	    ActionBar bar = getActionBar();
	    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    
	    mAvgMoodTextView = (TextView) findViewById(R.id.mood_relation_history_average_mood);
	    
	    OnNavigationListener listener = new TimeframeOnNavigationListener();
	    
	    bar.setDisplayShowTitleEnabled(false);
	    bar.setListNavigationCallbacks(adapter, listener);
	}
	
	/**
	 * Represents the navigation listener for the timeframe picker on ActionBar.
	 * @author Toni Järvinen
	 *
	 */
	private class TimeframeOnNavigationListener implements OnNavigationListener {
		@Override
		public boolean onNavigationItemSelected(int itemPosition, long itemId) {
			GregorianCalendar calendar = new GregorianCalendar();
			if (itemPosition == 0) {
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				calendar.add(Calendar.DATE, -7);
				mFromTimeInMillis = calendar.getTimeInMillis();
				mAmountOfDays = 7;
				DayInHistory[] days = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				mAvgMood = getAvgMood(days);
				mAvgMoodTextView.setText("" + mAvgMood);
			} else if (itemPosition == 1) {
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				calendar.add(Calendar.DATE, -14);
				mFromTimeInMillis = calendar.getTimeInMillis();
				mAmountOfDays = 14;
				DayInHistory[] days = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				mAvgMood = getAvgMood(days);
				mAvgMoodTextView.setText("" + mAvgMood);
			} else if (itemPosition == 2) {
				calendar.add(Calendar.DATE, -29);
				mFromTimeInMillis = calendar.getTimeInMillis();
				mAmountOfDays = 30;
				DayInHistory[] days = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				mAvgMood = getAvgMood(days);
				mAvgMoodTextView.setText("" + mAvgMood);
			} else if (itemPosition == 3) {
				showTimeFramePicker();
			}
			
			return false;
		}
		
		/**
		 * Get the average mood across the provided days.
		 * @param days
		 * @return
		 */
		private int getAvgMood(DayInHistory[] days) {
			int moodAmount = 0;
			int moodSum = 0;
			for (int i = 0; i < days.length; i++) {
				days[i].setEvents();
				int avgMood = days[i].getAvgMoodLevel();
				if (avgMood != 0) {
					moodSum += avgMood;
					moodAmount += 1;
				}
			}
			if (moodAmount > 0) {
				return (int) (moodSum / moodAmount + 0.5f);
			} else {
				return 0;
			}
		}
		
		/**
		 * A class for building a timeframe picker dialog with two date pickers.
		 */
		private void showTimeFramePicker() {
			LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
		    View timeframePicker = inflater.inflate(R.layout.element_timeframe_picker, null);
		    
		    final DatePicker startDate = (DatePicker) timeframePicker.findViewById(R.id.timeframe_startdate);
		    final DatePicker endDate = (DatePicker) timeframePicker.findViewById(R.id.timeframe_enddate);
		    
		    startDate.setMaxDate(System.currentTimeMillis());
		    endDate.setMaxDate(System.currentTimeMillis());
		    
		    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
		    dialogBuilder.setView(timeframePicker);
		    dialogBuilder.setTitle(getString(R.string.select_a_timeframe));
		    dialogBuilder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.set(Calendar.YEAR, endDate.getYear());
					calendar.set(Calendar.MONTH, endDate.getMonth());
					calendar.set(Calendar.DAY_OF_MONTH, endDate.getDayOfMonth());
					mEndTimeInMillis = calendar.getTimeInMillis();
					calendar.set(Calendar.YEAR, startDate.getYear());
					calendar.set(Calendar.MONTH, startDate.getMonth());
					calendar.set(Calendar.DAY_OF_MONTH, startDate.getDayOfMonth());
					mFromTimeInMillis = calendar.getTimeInMillis();
					
					mAmountOfDays = (int) TimeUnit.DAYS.convert(mEndTimeInMillis - mFromTimeInMillis, TimeUnit.MILLISECONDS) + 1;
					DayInHistory[] days = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
					mAvgMood = getAvgMood(days);
					mAvgMoodTextView.setText("" + mAvgMood);
					
					dialog.dismiss();
				}
			});
		    dialogBuilder.create().show();
		}
		
	}

}
