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
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.Mood;
import org.apps8os.motivator.data.MoodDataHandler;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
	private LinearLayout mMoodImageRoot;
	
	private static final String AMOUNT_OF_DRINKS = "amount_of_drinks";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_relation_history);
	    ActionBar bar = getActionBar();
	    SpinnerAdapter adapter = ArrayAdapter.createFromResource(bar.getThemedContext(), R.array.time_frames, android.R.layout.simple_spinner_dropdown_item);
	    
	    mCaseSelector = AMOUNT_OF_DRINKS;
	    mDataHandler = new MoodDataHandler(this);
	    mContext = this;
	    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_orange));
	    
	    mAvgMoodTextView = (TextView) findViewById(R.id.mood_relation_history_average_mood);
	    mMoodImageRoot = (LinearLayout) findViewById(R.id.mood_image_root);
	    
	    OnNavigationListener listener = new TimeframeOnNavigationListener();
	    
	    bar.setDisplayShowTitleEnabled(false);
	    bar.setListNavigationCallbacks(adapter, listener);
	}
	
	/**
	 * Sets the mood images in the average mood view.
	 * @param mood
	 */
	private void setMoodImages(Mood mood) {
		if (mood.getEnergy() > 0) {
			Drawable energyImage = mContext.getResources().getDrawable(getResources().getIdentifier("energy" + mood.getEnergy(), "drawable", getPackageName()));
			((ImageView) mMoodImageRoot.getChildAt(0)).setImageDrawable(energyImage);
			Drawable moodImage = mContext.getResources().getDrawable(getResources().getIdentifier("mood" + mood.getMood(), "drawable", getPackageName()));
			((ImageView) mMoodImageRoot.getChildAt(1)).setImageDrawable(moodImage);
		}
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
				Mood avgMood = getAvgMood(days);
				setMoodImages(avgMood);
				mAvgMoodTextView.setText("" + avgMood.getEnergy() + " " + avgMood.getMood());
			} 
			
			else if (itemPosition == 1) {
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				calendar.add(Calendar.DATE, -14);
				mFromTimeInMillis = calendar.getTimeInMillis();
				mAmountOfDays = 14;
				DayInHistory[] days = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				Mood avgMood = getAvgMood(days);
				setMoodImages(avgMood);
				mAvgMoodTextView.setText("" + avgMood.getEnergy() + " " + avgMood.getMood());
			} 
			
			else if (itemPosition == 2) {
				calendar.add(Calendar.DATE, -29);
				mFromTimeInMillis = calendar.getTimeInMillis();
				mAmountOfDays = 30;
				DayInHistory[] days = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				Mood avgMood = getAvgMood(days);
				setMoodImages(avgMood);
				mAvgMoodTextView.setText("" + avgMood.getEnergy() + " " + avgMood.getMood());
			} 
			
			else if (itemPosition == 3) {
				showTimeFramePicker();
			}
			
			return false;
		}
		
		/**
		 * Get the average mood across the provided days.
		 * @param days
		 * @return
		 */
		private Mood getAvgMood(DayInHistory[] days) {
			ArrayList<Mood> allMoods = new ArrayList<Mood>();
			for (int i = 0; i < days.length; i++) {
				allMoods.addAll(days[i].getMoods());
			}
			int avgMood = 0;
			int avgEnergy = 0;
			for (int i = 0; i < allMoods.size(); i++) {
				avgMood += allMoods.get(i).getMood();
				avgEnergy += allMoods.get(i).getEnergy();
			}
			
			if (allMoods.size() > 0) {
				avgMood = avgMood / allMoods.size();
				avgEnergy = avgEnergy / allMoods.size();
			}
			return new Mood(avgMood, avgEnergy, 0, "");
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
					Mood avgMood = getAvgMood(days);
					setMoodImages(avgMood);
					mAvgMoodTextView.setText("" + avgMood.getEnergy() + " " + avgMood.getMood());
					
					if (avgMood.getMood() <= 0) {
						mAvgMoodTextView.setText(mContext.getString(R.string.no_added_moods));
					}
					
					dialog.dismiss();
				}
			});
		    
		    dialogBuilder.create().show();
		}
		
	}

}
