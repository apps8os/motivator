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
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.data.SprintDataHandler;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	private int mCaseSelector;
	private int mArgument = -1;
	private DayDataHandler mDataHandler;
	private int mAvgMood = 0;
	private TextView mAvgMoodTextView;
	private TextView mAvgEnergyTextView;
	private Context mContext;
	private LinearLayout mMoodImageRoot;
	private DayInHistory[] mDays;
	
	private int[] mTitlesEnergy = {
    		R.string.energy_level1, R.string.energy_level2, 
    		R.string.energy_level3, R.string.energy_level4,
    		R.string.energy_level5
    		};
    
	private int[] mTitlesMood = {
    		R.string.mood_level1, R.string.mood_level2,
    		R.string.mood_level3, R.string.mood_level4,
    		R.string.mood_level5
    		};
	
	private static final int AMOUNT_OF_DRINKS = 1;
	private static final int ALL = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_relation_history);
	    ActionBar bar = getActionBar();
	    SpinnerAdapter adapter = ArrayAdapter.createFromResource(bar.getThemedContext(), R.array.time_frames, android.R.layout.simple_spinner_dropdown_item);
	    
	    mCaseSelector = ALL;
	    mDataHandler = new DayDataHandler(this);
	    mContext = this;
	    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    bar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_orange));
	    
	    ((TextView) findViewById(R.id.mood_relation_history_top_title)).setText(Html.fromHtml(getString(R.string.filter_by) + ":<br>" + getString(R.string.amount_of_drinks)));
	    
	    mAvgMoodTextView = (TextView) findViewById(R.id.mood_relation_history_average_mood);
	    mAvgEnergyTextView = (TextView) findViewById(R.id.mood_relation_history_average_energy);
	    mMoodImageRoot = (LinearLayout) findViewById(R.id.mood_image_root);
	    
	    final Button zeroAmount = (Button) findViewById(R.id.mood_relation_history_top_button1);
	    final Button oneAmount = (Button) findViewById(R.id.mood_relation_history_top_button2);
	    final Button twoAmount = (Button) findViewById(R.id.mood_relation_history_top_button3);
	    final Button threeAmount = (Button) findViewById(R.id.mood_relation_history_top_button4);

	    zeroAmount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (zeroAmount.isSelected()) {
					mArgument = -1;
					mCaseSelector = ALL;
					zeroAmount.setSelected(false);
				} else {
					mArgument = 0;
					mCaseSelector = AMOUNT_OF_DRINKS;
					zeroAmount.setSelected(true);
				}
				setAvgMood(mDays, mCaseSelector, mArgument);
				
				oneAmount.setSelected(false);
				twoAmount.setSelected(false);
				threeAmount.setSelected(false);
			}
	    });
	    
	    oneAmount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (oneAmount.isSelected()) {
					mArgument = -1;
					mCaseSelector = ALL;
					oneAmount.setSelected(false);
				} else {
					mArgument = 1;
					mCaseSelector = AMOUNT_OF_DRINKS;
					oneAmount.setSelected(true);
				}
				setAvgMood(mDays, mCaseSelector, mArgument);
				zeroAmount.setSelected(false);
				twoAmount.setSelected(false);
				threeAmount.setSelected(false);
			}
	    });
	    
	    twoAmount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (twoAmount.isSelected()) {
					mArgument = -1;
					mCaseSelector = ALL;
					twoAmount.setSelected(false);
				} else {
					mArgument = 2;
					mCaseSelector = AMOUNT_OF_DRINKS;
					twoAmount.setSelected(true);
				}
				setAvgMood(mDays, mCaseSelector, mArgument);
				zeroAmount.setSelected(false);
				oneAmount.setSelected(false);
				threeAmount.setSelected(false);
			}
	    });
	    
	    threeAmount.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (threeAmount.isSelected()) {
					mArgument = -1;
					mCaseSelector = ALL;
					threeAmount.setSelected(false);
				} else {
					mArgument = 3;
					mCaseSelector = AMOUNT_OF_DRINKS;
					threeAmount.setSelected(true);
				}
				setAvgMood(mDays, mCaseSelector, mArgument);
				zeroAmount.setSelected(false);
				oneAmount.setSelected(false);
				twoAmount.setSelected(false);
			}
	    });
	    
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
		} else {
			((ImageView) mMoodImageRoot.getChildAt(0)).setImageResource(R.drawable.energy_no_data);
			((ImageView) mMoodImageRoot.getChildAt(1)).setImageResource(R.drawable.mood_no_data);
		}
	}
	
	/**
	 * Get the average mood across the provided days.
	 * @param days
	 * @return
	 */
	private void setAvgMood(DayInHistory[] days, int selector, int argument) {
		ArrayList<Mood> allMoods = new ArrayList<Mood>();
		for (int i = 0; i < days.length; i++) {
			if (selector == ALL) {
				allMoods.addAll(days[i].getMoods());
			} else if (selector == AMOUNT_OF_DRINKS && mDataHandler.getDrinksForDay(days[i].getDateInMillis()) == argument) {
				allMoods.addAll(days[i].getMoods());
			}
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
		} else {
			avgMood = -1;
			avgEnergy = -1;
		}
		
		Mood avgMoods = new Mood(avgMood, avgEnergy, 0, "");
		setMoodImages(avgMoods);
		if (avgMoods.getEnergy() == -1) {
			mAvgMoodTextView.setText(getString(R.string.no_added_moods));
			mAvgEnergyTextView.setText("");
		} else {
			mAvgMoodTextView.setText("" + getString(R.string.energy) + " " + getString(mTitlesMood[avgMoods.getMood() - 1]));
			mAvgEnergyTextView.setText("" + getString(R.string.mood) + " " + getString(mTitlesEnergy[avgMoods.getEnergy() - 1]));
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
				mDays = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				setAvgMood(mDays, mCaseSelector, mArgument);
			} 
			
			else if (itemPosition == 1) {
				calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				calendar.add(Calendar.DATE, -14);
				mFromTimeInMillis = calendar.getTimeInMillis();
				mAmountOfDays = 14;
				mDays = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				setAvgMood(mDays, mCaseSelector, mArgument);
			} 
			
			else if (itemPosition == 2) {
				calendar.add(Calendar.DATE, -29);
				mFromTimeInMillis = calendar.getTimeInMillis();
				mAmountOfDays = 30;
				mDays = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				setAvgMood(mDays, mCaseSelector, mArgument);
			} 
			
			else if (itemPosition == 3) {
				SprintDataHandler sprintHandler = new SprintDataHandler(MoodRelationHistoryActivity.this);
				Sprint current = sprintHandler.getCurrentSprint();
				mFromTimeInMillis = current.getStartTime();
				mAmountOfDays = current.getCurrentDayOfTheSprint();
				mDays = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
				setAvgMood(mDays, mCaseSelector, mArgument);
			} 
			
			else if (itemPosition == 4) {
				showTimeFramePicker();
			}
			
			return false;
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
					mDays = mDataHandler.getDaysAfter(mFromTimeInMillis, mAmountOfDays);
					setAvgMood(mDays, mCaseSelector, mArgument);
					
					dialog.dismiss();
				}
			});
		    
		    dialogBuilder.create().show();
		}
	}

}
