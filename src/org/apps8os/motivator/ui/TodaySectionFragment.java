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
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Question;
import org.apps8os.motivator.utils.MotivatorConstants;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Html;
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
	private LinearLayout mEventLayout;
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
		mEventLayout = (LinearLayout) rootView.findViewById(R.id.main_activity_today_dynamic_buttons);
		
		mButtonLayout = (LinearLayout) rootView.findViewById(R.id.main_activity_today_dynamic_buttons2);
		
		// two buttons that are always present
		
		Button addEventButton = (Button) rootView.findViewById(R.id.main_activity_today_mood_button);
		addEventButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MoodQuestionActivity.class);
				startActivity(intent);
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
	private class LoadPlansTask extends AsyncTask<Void, Void, ArrayList<MotivatorEvent>> {
		
		private Context mContext;

		public LoadPlansTask(Context context) {
			mContext = context;
			
			// Open the database connection
			mDataHandler = new EventDataHandler(getActivity());
			mDataHandler.open();
			
		}

		/**
		 * Load the events that are planned in background to an ArrayList of MotivatorEvent objects.
		 */
		@Override
		protected ArrayList<MotivatorEvent> doInBackground(Void... arg0) {
			ArrayList<MotivatorEvent> result = new ArrayList<MotivatorEvent>();
 			Cursor cursor = mDataHandler.getEventsToday();
 			cursor.moveToFirst();
 			int lastAnswerId = -1;
 			// Looping through the cursor.
 			if (cursor.getCount() > 0) {
 				// Initialize a MotivatorEvent object with the answerId from the database as the eventId.
 				MotivatorEvent event = new MotivatorEvent(cursor.getInt(0));
				while (!cursor.isClosed()) {
					// Check if we have looped through the answers relating to this event with the answerId.
					if (lastAnswerId != cursor.getInt(0) && lastAnswerId != -1) {
						// Add the event to the list and initialize a new instance.
						result.add(event);
						event = new MotivatorEvent(cursor.getInt(0));
					}
					// Get the question with the questionId.
					Question question = mDataHandler.getQuestion(cursor.getInt(1));
					
					if (question != null) {
						// Handle the different questions/answers.
						switch (question.getId()) {
						case MotivatorConstants.QUESTION_ID_WHEN:
							event.setStartTime(cursor.getLong(3));
							// All events in this section should have today as the text so get the answer representing today.
							event.setEventAsText(question.getAnswer(0));
							break;
							
						// The start time is initialized to the change of day, add the amount of hours to get the time of the day.
						case MotivatorConstants.QUESTION_ID_TIME_TO_GO:
							long time;
							switch (cursor.getInt(2)) {
							case 0:
								time = TimeUnit.MILLISECONDS.convert(15, TimeUnit.HOURS);
								break;
							case 1:
								time = TimeUnit.MILLISECONDS.convert(18, TimeUnit.HOURS);
								break;
							case 2:
								time = TimeUnit.MILLISECONDS.convert(21, TimeUnit.HOURS);
								break;
							default:
								time = 0;
							}
							event.setStartTime(event.getStartTime() + time);
							break;
						}
					}
					lastAnswerId = cursor.getInt(0);
					if (cursor.isLast()) {
						cursor.close();
					} else {
						cursor.moveToNext();
					}
				}
				result.add(event);
 			}
			// Sort the list
			Collections.sort(result, new Comparator<MotivatorEvent>() {
				@Override
				public int compare(MotivatorEvent case1, MotivatorEvent case2) {
					long case1Sorter = case1.getStartTime();
					long case2Sorter = case2.getStartTime();
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
		protected void onPostExecute(ArrayList<MotivatorEvent> result) {
			mEventLayout.removeAllViews();
			View separator = mInflater.inflate(R.layout.element_main_activity_button_separator, mEventLayout, false);
			mEventLayout.addView(separator);
			
			// Create buttons for the result set.
			for (int i = 0; i < result.size(); i ++) {
				Button eventButton = (Button) mInflater.inflate(R.layout.element_main_activity_button, mEventLayout, false);
				eventButton.setText(result.get(i).getEventText());
				eventButton.setOnClickListener(new OpenEventDetailViewOnClickListener(result.get(i).getId(), mContext));
				mEventLayout.addView(eventButton);
				
				separator = mInflater.inflate(R.layout.element_main_activity_button_separator, mEventLayout, false);
				mEventLayout.addView(separator);
				
				// Add a button for adding drinks when an event is active.
				if (result.get(i).getStartTime() < System.currentTimeMillis()) {
					mButtonLayout.removeAllViews();
					separator = mInflater.inflate(R.layout.element_main_activity_button_separator, mButtonLayout, false);
					mButtonLayout.addView(separator);
					final MotivatorEvent todaysEvent = result.get(i);
					final Button addDrinkButton = (Button) mInflater.inflate(R.layout.element_main_activity_button, mButtonLayout, false);
					Drawable bottle = getActivity().getResources().getDrawable(R.drawable.pullo1);
					addDrinkButton.setCompoundDrawablesWithIntrinsicBounds(bottle, null, null, null);
					addDrinkButton.setText(Html.fromHtml("Add Drink"));
					addDrinkButton.setOnClickListener(new OnClickListener() {
						private int mDrinkCounter = 0;
						@Override
						public void onClick(View v) {
							mDataHandler.open();
							mDataHandler.addDrink(todaysEvent.getId());
							mDrinkCounter += 1;
							addDrinkButton.setText(Html.fromHtml("Add Drink<br> <small>" + mDrinkCounter + " drinks<small>"));
							mDataHandler.close();
						}
					});
					mButtonLayout.addView(addDrinkButton);
				}
			}
		}
	}
}
