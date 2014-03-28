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
import org.apps8os.motivator.utils.MotivatorConstants;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
import android.util.DisplayMetrics;
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
	private View mRootView;
	private LayoutInflater mInflater;
	private long mSprintStartDate;
	private int mPosition;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		Bundle b = getArguments();
		mRootView = (View) inflater.inflate(R.layout.fragment_mood_history_landscape, viewGroup, false);
		mInflater = inflater;
		mRes = getActivity().getResources();
		mSprintStartDate = b.getLong(MotivatorConstants.CURRENT_SPRINT_STARTDATE);
		mPosition = b.getInt(MotivatorConstants.FRAGMENT_POSITION);
		
		// Loading the days on a different thread.
		LoadDaysAsyncTask loadingTask = new LoadDaysAsyncTask(mSprintStartDate, mPosition, (MoodHistoryActivity) getActivity());
		loadingTask.execute();
		
		return mRootView;
		
	}
	
	/**
	 * Save the day to the savedInstanceState.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(MotivatorConstants.CURRENT_SPRINT_STARTDATE, mSprintStartDate);
		outState.putInt(MotivatorConstants.FRAGMENT_POSITION, mPosition);
	}
	
	/**
	 * Gets the short format of the week day.
	 * @param day
	 * @return
	 */
	public String getDay(DayInHistory day) {
		Calendar date = new GregorianCalendar();
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
		private MoodHistoryActivity mActivity;
		
		public LoadDaysAsyncTask(long startDateInMillis, int position, MoodHistoryActivity activity) {
			mSprintStartDateInMillis = startDateInMillis;
			mPosition = position;
			mActivity = activity;
		}

		/**
		 * Load the days in background.
		 */
		@Override
		protected ArrayList<DayInHistory> doInBackground(Void... params) {
			ArrayList<DayInHistory> result = new ArrayList<DayInHistory>();
			
			// Get the first week in the sprint.
			Calendar calendar = new GregorianCalendar();
			calendar.setFirstDayOfWeek(Calendar.MONDAY);
			calendar.setTimeInMillis(mSprintStartDateInMillis);
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			// Add 7 days / a week depending on the position. In the first position 0, this does not do anything and we add the first week.
			calendar.add(Calendar.DATE, mPosition * 7);
			// Add days until sunday.
			while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
				DayInHistory date = mActivity.getDateInHistory(calendar.getTimeInMillis());
				result.add(date);
				calendar.add(Calendar.DATE, 1);
			}
			// Add the sunday.
			DayInHistory date = mActivity.getDateInHistory(calendar.getTimeInMillis());
			result.add(date);
			return result;
		}
		
		/**
		 * Publish the results to the UI thread.
		 */
		@Override
		public void onPostExecute(ArrayList<DayInHistory> result) {
			LinearLayout dayLayout = (LinearLayout) mRootView.findViewById(R.id.mood_history_weekview);
			
			// Add the lower half with day views.
			for (int i = 0; i < result.size(); i++) {
				RelativeLayout dayView = (RelativeLayout) mInflater.inflate(R.layout.element_mood_history_week_view_day, dayLayout, false);
				TextView dayText = (TextView) dayView.getChildAt(1);
				dayText.setText(Html.fromHtml(getDay(result.get(i)) + "<br><small>" + result.get(i).getDateInString(getActivity())));
				if (result.get(i).getAvgMoodLevel() == 0) {
					ImageView moodImage = (ImageView) dayView.getChildAt(0);
					moodImage.setImageDrawable(mRes.getDrawable(R.drawable.temp_emoticon_bw));
				}
				DisplayMetrics dm = new DisplayMetrics();
				getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
				dayView.getLayoutParams().width = dm.widthPixels / 7;
				dayLayout.addView(dayView);
			}
			// add the line graph.
			Line l = new Line();
			LinePoint p = new LinePoint();
			p.setX(0);
			p.setY(2);
			l.addPoint(p);
			p = new LinePoint();
			p.setX(1);
			p.setY(1);
			l.addPoint(p);
			p = new LinePoint();
			p.setX(2);
			p.setY(1);
			l.addPoint(p);
			p = new LinePoint();
			p.setX(3);
			p.setY(1);
			l.addPoint(p);
			p = new LinePoint();
			p.setX(4);
			p.setY(2);
			l.addPoint(p);
			p = new LinePoint();
			p.setX(5);
			p.setY(4);
			l.addPoint(p);
			p = new LinePoint();
			p.setX(6);
			p.setY(3);
			l.addPoint(p);
			l.setColor(Color.parseColor("#FFBB33"));
			LineGraph li = (LineGraph) mRootView.findViewById(R.id.graph);
			li.addLine(l);
			li.setRangeY(0, 4);
			li.setLineToFill(0);
			
			// Remove the loading indicator from the layout.
			RelativeLayout loadingView = (RelativeLayout) mRootView.findViewById(R.id.mood_history_loading_panel);
			loadingView.setVisibility(View.GONE);
			
		}
		
	}
}
