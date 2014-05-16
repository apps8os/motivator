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
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.Sprint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;

/**
 * Represents a week in the mood history.
 * @author Toni Järvinen
 *
 */
public class MoodHistoryWeekFragment extends Fragment {
	
	private Resources mRes;
	private DayDataHandler mDayDataHandler;
	private View mRootView;
	private LayoutInflater mInflater;
	private ArrayList<DayInHistory> mDays;
	private long mSprintStartDate;
	private int mPosition;
	private int mSelectedAttribute;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		Bundle b = getArguments();
		mRootView = (View) inflater.inflate(R.layout.fragment_mood_history_landscape, viewGroup, false);
		mInflater = inflater;
		mRes = getActivity().getResources();
		mSprintStartDate = b.getLong(Sprint.CURRENT_SPRINT_STARTDATE);
		mPosition = b.getInt(MoodHistoryActivity.FRAGMENT_POSITION);
		mDayDataHandler = new DayDataHandler(getActivity());
		
		// Loading the days on a different thread.
		LoadDaysAsyncTask loadingTask = new LoadDaysAsyncTask(mSprintStartDate, mPosition);
		loadingTask.execute();
		mSelectedAttribute = ((MoodHistoryActivity) getActivity()).getSelectedAttribute();
		return mRootView;
		
	}
	
	/**
	 * Save the day to the savedInstanceState.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(Sprint.CURRENT_SPRINT_STARTDATE, mSprintStartDate);
		outState.putInt(MoodHistoryActivity.FRAGMENT_POSITION, mPosition);
	}
	
	/**
	 * Gets the short format of the week day.
	 * @param day
	 * @return
	 */
	public String getDay(DayInHistory day) {
		Calendar date = Calendar.getInstance();
		date.setTimeInMillis(day.getDateInMillis());
		switch (date.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.MONDAY:
			return mRes.getString(R.string.monday_short);
		case Calendar.TUESDAY:
			return mRes.getString(R.string.tuesday_short);
		case Calendar.WEDNESDAY:
			return mRes.getString(R.string.wednesday_short);
		case Calendar.THURSDAY:
			return mRes.getString(R.string.thursday_short);
		case Calendar.FRIDAY:
			return mRes.getString(R.string.friday_short);
		case Calendar.SATURDAY:
			return mRes.getString(R.string.saturday_short);
		case Calendar.SUNDAY:
			return mRes.getString(R.string.sunday_short);
		default:
			return "NOT FOUND";
		}
	}
	
	/**
	 * Inner class for loading the days in a different thread and the publishing the results to the UI thread.
	 * @author Toni Järvinen
	 *
	 */
	private class LoadDaysAsyncTask extends AsyncTask<Void, Void, ArrayList<DayInHistory>> {
		
		private long mSprintStartDateInMillis;
		private int mPosition;
		
		public LoadDaysAsyncTask(long startDateInMillis, int position) {
			mSprintStartDateInMillis = startDateInMillis;
			mPosition = position;
		}

		/**
		 * Load the days in background.
		 */
		@Override
		protected ArrayList<DayInHistory> doInBackground(Void... params) {
			ArrayList<DayInHistory> result = new ArrayList<DayInHistory>();
			
			// Get the first week in the sprint.
			Calendar calendar = Calendar.getInstance();
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.setTimeInMillis(mSprintStartDateInMillis);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			// Add 7 days / a week depending on the position. In the first position 0, this does not do anything and we add the first week.
			calendar.add(Calendar.DATE, mPosition * 7);
			// Add days until sunday.
			while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				DayInHistory date = mDayDataHandler.getDayInHistory(calendar.getTimeInMillis());
				result.add(date);
				calendar.add(Calendar.DATE, 1);
			}
			// Add the sunday.
			DayInHistory date = mDayDataHandler.getDayInHistory(calendar.getTimeInMillis());
			result.add(date);
			return result;
		}
		
		/**
		 * Publish the results to the UI thread.
		 */
		@Override
		public void onPostExecute(ArrayList<DayInHistory> result) {
			LinearLayout dayLayout = (LinearLayout) mRootView.findViewById(R.id.mood_history_weekview);
			dayLayout.setVisibility(View.GONE);
			// add the line graph.
			Line l = new Line();
			Line l2 = new Line();
			LinePoint p = new LinePoint();
			LinePoint p2 = new LinePoint();
			DayInHistory day;
			int resultSize = result.size();
			mDays = result;
			// Add the lower half with day views.
			for (int i = 0; i < resultSize; i++) {
				day = result.get(i);
				LinearLayout dayView = (LinearLayout) mInflater.inflate(R.layout.element_mood_history_week_view_day, dayLayout, false);
				TextView dayText = (TextView) ((LinearLayout) dayView.getChildAt(0)).getChildAt(0);
				dayText.setText(getDay(day));
				LinearLayout moodImageRoot = (LinearLayout) dayView.findViewById(R.id.mood_image_root);
				ImageView energyImage = (ImageView) moodImageRoot.getChildAt(0);
				ImageView moodImage = (ImageView) moodImageRoot.getChildAt(1);
				if (result.get(i).getAvgMoodLevel() == 0) {
					energyImage.setImageDrawable(mRes.getDrawable(R.drawable.energy_no_data));
					moodImage.setImageDrawable(mRes.getDrawable(R.drawable.mood_no_data));
				} else {
					energyImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("energy" + day.getAvgEnergyLevel(), "drawable", getActivity().getPackageName())));
					moodImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("mood" + day.getAvgMoodLevel(), "drawable", getActivity().getPackageName())));

				}
				dayLayout.addView(dayView);
				if (mSelectedAttribute == DayInHistory.AMOUNT_OF_DRINKS) {
					p.setX(i);
					int drinks = mDayDataHandler.getDrinksForDay(day.getDateInMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
					if (drinks > 4) {
						drinks = 4;
					}
					p.setY(drinks);
					l.addPoint(p);
					p = new LinePoint();
				} else if (mSelectedAttribute == DayInHistory.MOODS) {
					p.setX(i);
					p2.setX(i);
					int energy = day.getAvgEnergyLevel() - 1;
					int mood = day.getAvgMoodLevel() - 1;
					if (energy == -1) {
						p.setColor("#777777");
						p2.setColor("#777777");
						energy = 0;
						mood = 0;
					}
					p.setY(energy);
					p2.setY(mood);
					l.addPoint(p);
					l2.addPoint(p2);
					p = new LinePoint();
					p2 = new LinePoint();
				}
			}
			LineGraph li = (LineGraph) mRootView.findViewById(R.id.graph);
			if (mSelectedAttribute == DayInHistory.AMOUNT_OF_DRINKS) {
				l.setColor(Color.parseColor("#FFBB33"));
				
				li.addLine(l);
				li.setRangeY(0, 4);
				li.setLineToFill(0);
				li.setUsingDips(true);
			} else if (mSelectedAttribute == DayInHistory.MOODS) {
				l.setColor(Color.parseColor("#99cc00"));
				li.addLine(l);
				li.setRangeY(0, 4);
				li.setLineToFill(0);
				li.setUsingDips(true);
				
				l2.setColor(Color.parseColor("#FBBE45"));
				li.addLine(l2);
				li.setRangeY(0, 4);
				li.setLineToFill(0);
				li.setUsingDips(true);
			}
			
			final RelativeLayout loadingView = (RelativeLayout) mRootView.findViewById(R.id.mood_history_loading_panel);
			
			
			// Animate the switch from the loading view to the week view.
			int animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
			
			dayLayout.setAlpha(0f);
			dayLayout.setVisibility(View.VISIBLE);
			li.setAlpha(0f);
			
			dayLayout.animate()
	            .alpha(1f)
	            .setDuration(animationDuration)
	            .setListener(null);
			
			li.animate()
            .alpha(1f)
            .setDuration(animationDuration)
            .setListener(null);
			
			// Animate the fading out of the loading view.
			loadingView.animate()
					.alpha(0f)
					.setDuration(animationDuration)
					.setListener(new AnimatorListenerAdapter() {
						
						// Set the visibility to gone when animation has ended.
						@Override
						public void onAnimationEnd(Animator animation) {
							loadingView.setVisibility(View.GONE);
						}
					});
			
		}
		
	}

	/**
	 * Changing what is pictured in the LineGraph
	 * @param selector
	 */
	public void updateSelectedAttribute(int selector) {
		if (selector != mSelectedAttribute) {
			Line l = new Line();
			LinePoint p = new LinePoint();
			Line l2 = new Line();
			LinePoint p2 = new LinePoint();
			LineGraph li = (LineGraph) mRootView.findViewById(R.id.graph);
			int daysSize;
			DayInHistory day;
			switch (selector) {
			case DayInHistory.AMOUNT_OF_DRINKS:
				daysSize = mDays.size();
				li.removeAllLines();
				for (int i = 0; i < daysSize; i++) {
					day = mDays.get(i);
					p.setX(i);
					int drinks = mDayDataHandler.getDrinksForDay(day.getDateInMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
					if (drinks > 4) {
						drinks = 4;
					}
					p.setY(drinks);
					l.addPoint(p);
					p = new LinePoint();
				}
				l.setColor(Color.parseColor("#FFBB33"));
				li.addLine(l);
				li.setRangeY(0, 4);
				li.setLineToFill(0);
				li.setUsingDips(true);
				break;
			case DayInHistory.MOODS:
				daysSize = mDays.size();
				li.removeAllLines();
				for (int i = 0; i < daysSize; i++) {
					day = mDays.get(i);
					p.setX(i);
					p2.setX(i);
					int energy = day.getAvgEnergyLevel() - 1;
					int mood = day.getAvgMoodLevel() - 1;
					if (energy == -1) {
						p.setColor("#777777");
						p2.setColor("#777777");
						energy = 0;
						mood = 0;
					}
					p.setY(energy);
					p2.setY(mood);
					l.addPoint(p);
					l2.addPoint(p2);
					p = new LinePoint();
					p2 = new LinePoint();
				}
				l.setColor(Color.parseColor("#99cc00"));
				li.addLine(l);
				li.setRangeY(0, 4);
				li.setLineToFill(0);
				li.setUsingDips(true);
				
				l2.setColor(Color.parseColor("#FBBE45"));
				li.addLine(l2);
				li.setRangeY(0, 4);
				li.setLineToFill(0);
				li.setUsingDips(true);
				break;
			}
			mSelectedAttribute = selector;
		}
	}
}
