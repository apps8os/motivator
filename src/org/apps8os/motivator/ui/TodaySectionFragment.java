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
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Sprint;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Represents the today section in the UI.
 */
public class TodaySectionFragment extends Fragment {
	
	private EventDataHandler mEventDataHandler;
	private DayDataHandler mDayDataHandler;
	private LinearLayout mEventLayout;
	private LinearLayout mDrinkButton;
	private TextView mDrinkCounterTextView;
	private Resources mRes;
	private View mRootView;
	private LayoutInflater mInflater;
	private int mDrinkCounter = 0;
	private int mPlannedDrinks = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		mRootView = mInflater.inflate(
				R.layout.fragment_main_activity_today_section, container, false);
		mRes = getResources();
		// The layout which has dynamic amount of future events/buttons.
		mEventLayout = (LinearLayout) mRootView.findViewById(R.id.main_activity_today_dynamic_buttons);
		
		mDayDataHandler = new DayDataHandler(getActivity());
		
		// two buttons that are always present
		
		LinearLayout moodQuestion = (LinearLayout) mRootView.findViewById(R.id.main_activity_today_mood_button);
		moodQuestion.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DayInHistory yesterday = mDayDataHandler.getDayInHistory(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
			    yesterday.setEvents();
				Intent intent = new Intent(getActivity(), MoodQuestionActivity.class);
				startActivity(intent);
			}
		});
		
		mDrinkButton = (LinearLayout) mRootView.findViewById(R.id.drinks_button);
		mDrinkCounterTextView = (TextView) mDrinkButton.findViewById(R.id.card_button_middle_text);
		ImageButton addDrink = (ImageButton) mDrinkButton.findViewById(R.id.add_drink_button);
		ImageButton removeDrink = (ImageButton) mDrinkButton.findViewById(R.id.remove_drink_button);
		mDrinkCounterTextView.setTextColor(mRes.getColor(R.color.medium_gray));
		
		addDrink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrinkCounter += 1;
				mDayDataHandler.addDrink();
				mDrinkCounterTextView.setText(mDrinkCounter + " " + getString(R.string.drinks_today));
			}
		});
		
		removeDrink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDrinkCounter > 0) {
					mDrinkCounter -= 1;
					mDayDataHandler.removeDrink();
					mDrinkCounterTextView.setText(mDrinkCounter + " " + getString(R.string.drinks_today));
				}
			}
		});
		
		return mRootView;
	}
	
	/**
	 * Update the events for today on resume with an async task.
	 */
	@Override
	public void onResume() {
		Sprint currentSprint = getArguments().getParcelable(Sprint.CURRENT_SPRINT);
		ProgressBar sprintProgress = (ProgressBar) mRootView.findViewById(R.id.today_section_sprint_progress_bar);
		TextView sprintTextView = (TextView) mRootView.findViewById(R.id.today_section_sprint_progress_text);
		
		mDrinkCounter = mDayDataHandler.getDrinksForDay(System.currentTimeMillis());
		mDrinkCounterTextView.setText(mDrinkCounter + " " + getString(R.string.drinks_today));
		
		sprintProgress.setMax(currentSprint.getDaysInSprint() -1);
		sprintProgress.setProgress(currentSprint.getCurrentDayOfTheSprint());
		sprintTextView.setText(Html.fromHtml(getString(R.string.day) + " " + currentSprint.getCurrentDayOfTheSprint() + "/" + currentSprint.getDaysInSprint()));
		
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
			mEventDataHandler = new EventDataHandler(getActivity());
		}

		/**
		 * Load the events that are planned in background to an ArrayList of MotivatorEvent objects.
		 */
		@Override
		protected ArrayList<MotivatorEvent> doInBackground(Void... arg0) {
			return mEventDataHandler.getEventsToday();
		}
		
		/**
		 * Creates buttons for the upcoming events provided by doInBackground.
		 */
		@Override
		protected void onPostExecute(ArrayList<MotivatorEvent> result) {
			mEventLayout.removeAllViews();
			
			mPlannedDrinks = 0;
			int resultSize = result.size();
			MotivatorEvent event;
			final TextView lowerDrinkButtonText = (TextView) mDrinkButton.findViewById(R.id.card_button_bottom_text);
			if (resultSize == 0) {
				LinearLayout noEventsText = (LinearLayout) mInflater.inflate(R.layout.element_no_events, mEventLayout, false);
				mEventLayout.addView(noEventsText);
			}
			// Create buttons for the result set.
			for (int i = 0; i < resultSize; i ++) {
				event = result.get(i);
				event.setEventDateAsText(getString(R.string.today));
				final LinearLayout eventButton = (LinearLayout) mInflater.inflate(R.layout.element_larger_event_card_button, mEventLayout, false);
				LinearLayout buttonTextLayout = (LinearLayout) eventButton.getChildAt(0);
				String eventName = event.getName();
				String startTimeAsText = event.getStartTimeAsText();
				((TextView) buttonTextLayout.getChildAt(1)).setText(event.getPlannedDrinks() + " " + getString(R.string.drinks));
				if (eventName.length() > 0) {
					((TextView) buttonTextLayout.getChildAt(0)).setText(eventName);
				} else {
					((TextView) buttonTextLayout.getChildAt(0)).setText(event.getEventDateAsText());
				}
				if (startTimeAsText.length() > 0) {
					((TextView) buttonTextLayout.getChildAt(2)).setText(getString(R.string.when_to_go) + ": " + startTimeAsText);
				}
				((ImageView) eventButton.getChildAt(1)).setImageResource(R.drawable.calendar_icon);
				eventButton.setOnClickListener(new OpenEventDetailViewOnClickListener(event, mContext, MotivatorEvent.TODAY));
				mEventLayout.addView(eventButton);
				final int eventId = event.getId();
				final int eventPlannedDrinks = event.getPlannedDrinks();
				mPlannedDrinks += eventPlannedDrinks;
				
				// Make it possible to cancel the event with long click.
				eventButton.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(getString(R.string.cancel_event)+ "?").setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mPlannedDrinks -= eventPlannedDrinks;
								if (mPlannedDrinks == 0) {
									lowerDrinkButtonText.setText(getString(R.string.no_drinks_planned));
								} else {
									lowerDrinkButtonText.setText(getString(R.string.drinks_planned) + " " + mPlannedDrinks);
								}
								mEventDataHandler.deleteEvent(eventId);
								mEventLayout.removeView(eventButton);
								if (mEventLayout.getChildCount() == 0) {
									LinearLayout noEventsText = (LinearLayout) mInflater.inflate(R.layout.element_no_events, mEventLayout, false);
									mEventLayout.addView(noEventsText);
								}
							}
						}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
						Dialog dialog = builder.create();
						dialog.show();
						return true;
					}
				});
			}
			lowerDrinkButtonText.setTextColor(mRes.getColor(R.color.medium_gray));
			if (mPlannedDrinks == 0) {
				lowerDrinkButtonText.setText(getString(R.string.no_drinks_planned));
			} else {
				lowerDrinkButtonText.setText(getString(R.string.drinks_planned) + " " + mPlannedDrinks);
			}
		}
	}
}
