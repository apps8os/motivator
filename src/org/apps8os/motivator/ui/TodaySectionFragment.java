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
import java.util.Collections;
import java.util.Comparator;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.AnswerCase;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.Question;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Represents the today section in the UI.
 */
public class TodaySectionFragment extends Fragment {
	
	private EventDataHandler mDataHandler;
	private LinearLayout mButtonLayout;
	private LayoutInflater mInflater;
	
	public TodaySectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		View rootView = mInflater.inflate(
				R.layout.fragment_main_activity_today_section, container, false);
		
		// The layout which has dynamic amount of future events/buttons.
		mButtonLayout = (LinearLayout) rootView.findViewById(R.id.main_activity_today_dynamic_buttons);
		
		// two buttons that are always present
		
		Button addEventButton = (Button) rootView.findViewById(R.id.main_activity_today_mood_button);
		addEventButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MoodQuestionActivity.class);
				startActivity(intent);
			}
		});
		
		Button addGoalButton = (Button) rootView.findViewById(R.id.main_activity_today_add_place_button);
		addGoalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
			}
		});
		
		return rootView;
	}
	
	/**
	 * Update the events for today on resume with an async task.
	 */
	@Override
	public void onResume() {
		new LoadPlansTask(getActivity()).execute();
		super.onResume();
	}
	
	/**
	 * Inner class for loading the plans to the plan section asynchronously.
	 * @author Toni Järvinen
	 *
	 */
	private class LoadPlansTask extends AsyncTask<Void, Void, ArrayList<AnswerCase>> {
		
		private Context mContext;

		public LoadPlansTask(Context context) {
			mContext = context;
			
			// Open the database connection
			mDataHandler = new EventDataHandler(getActivity());
			mDataHandler.open();
		}

		/**
		 * Load the events that are planned in background to an ArrayList of AnswerCase objects.
		 */
		@Override
		protected ArrayList<AnswerCase> doInBackground(Void... arg0) {
			ArrayList<AnswerCase> result = new ArrayList<AnswerCase>();
 			Cursor cursor = mDataHandler.getEventsToday();
 			cursor.moveToFirst();
 			int lastAnswerId = -1;		
 			// Looping through the cursor.
			while (!cursor.isClosed() && cursor.getCount() > 0) {
				// Check if we already made AnswerCase object for the answering instance, only gets the first answer of the instance.
				if (lastAnswerId != cursor.getInt(0)) {	
					Question question = mDataHandler.getQuestion(cursor.getInt(1));
					AnswerCase event;
					// Sorting helper can be the answer in this case as the answers are in ascending order timewise
					// Need to select the texts to correspond change from tomorrow to today and next weekend to this weekend
					if (cursor.getInt(2) == 1){
						event = new AnswerCase(cursor.getInt(0), question.getAnswer(0), cursor.getInt(2));
					} else if (cursor.getInt(2) == 2) {
						event = new AnswerCase(cursor.getInt(0), getString(R.string.this_weekend), cursor.getInt(2));
					} else {
						event = new AnswerCase(cursor.getInt(0), question.getAnswer(cursor.getInt(2)), cursor.getInt(2));
					}
					result.add(event);
				}
				lastAnswerId = cursor.getInt(0);
				if (cursor.isLast()) {
					cursor.close();
				} else {
					cursor.moveToNext();
				}
				
			}
			// Sort the list with the sorting helper
			Collections.sort(result, new Comparator<AnswerCase>() {
				@Override
				public int compare(AnswerCase case1, AnswerCase case2) {
					int case1Sorter = case1.getSortingHelper();
					int case2Sorter = case2.getSortingHelper();
					if (case1Sorter > case2Sorter) {
						return 1;
					} else if (case1Sorter == case2Sorter) {
						return 0;
					} else {
						return -1;
					}
				}
			});
			
			// Lastly close the database connection
			mDataHandler.close();
			
			return result;
		}
		
		/**
		 * Creates buttons for the upcoming events provided by doInBackground.
		 */
		@Override
		protected void onPostExecute(ArrayList<AnswerCase> result) {
			mButtonLayout.removeAllViews();
			View separator = mInflater.inflate(R.layout.element_main_activity_button_separator, mButtonLayout, false);
			mButtonLayout.addView(separator);
			
			// Create buttons for the result set.
			for (int i = 0; i < result.size(); i ++) {
				Button eventButton = (Button) mInflater.inflate(R.layout.element_main_activity_button, mButtonLayout, false);
				eventButton.setText(result.get(i).getButtonText());
				eventButton.setOnClickListener(new OpenEventDetailViewOnClickListener(result.get(i).getAnsweringId(), mContext));
				mButtonLayout.addView(eventButton);
				
				separator = mInflater.inflate(R.layout.element_main_activity_button_separator, mButtonLayout, false);
				mButtonLayout.addView(separator);
			}
			
		}
	}
}
