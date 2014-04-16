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

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Represents the today section in the UI.
 */
public class TodaySectionFragment extends Fragment {
	
	private EventDataHandler mDataHandler;
	private LinearLayout mEventLayout;
	private LinearLayout mButtonLayout;
	private View mRootView;
	private LayoutInflater mInflater;
	private int mDrinkCounter = 0;
	
	public TodaySectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		mRootView = mInflater.inflate(
				R.layout.fragment_main_activity_today_section, container, false);
		
		// The layout which has dynamic amount of future events/buttons.
		mEventLayout = (LinearLayout) mRootView.findViewById(R.id.main_activity_today_dynamic_buttons);
		final FrameLayout contentRoot = (FrameLayout) mRootView.findViewById(R.id.root_view);
		
		// Inflate the help overlay to the fragment.
		mInflater.inflate(R.layout.element_help_overlay, contentRoot, true);
		
		((TextView) contentRoot.findViewById(R.id.help_overlay_title)).setText(getString(R.string.today_section));
		((TextView) contentRoot.findViewById(R.id.help_overlay_subtitle)).setText(getString(R.string.today_section_help));
		((Button) contentRoot.findViewById(R.id.help_overlay_button)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final View helpOverlay = (View) contentRoot.findViewById(R.id.help_overlay);
				helpOverlay.animate()
					.alpha(0f)
					.setDuration(500)
					.setListener(new AnimatorListenerAdapter() {
						
						// Set the visibility to gone when animation has ended.
						@Override
						public void onAnimationEnd(Animator animation) {
							helpOverlay.setVisibility(View.GONE);
						}
					});
			}
			
		});
		
		
		
		mButtonLayout = (LinearLayout) mRootView.findViewById(R.id.main_activity_today_dynamic_buttons2);
		
		// two buttons that are always present
		
		Button addEventButton = (Button) mRootView.findViewById(R.id.main_activity_today_mood_button);
		addEventButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MoodQuestionActivity.class);
				startActivity(intent);
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
		
		sprintProgress.setMax(currentSprint.getDaysInSprint() -1);
		sprintProgress.setProgress(currentSprint.getCurrentDayOfTheSprint());
		sprintTextView.setText(Html.fromHtml(getString(R.string.day) + " " + currentSprint.getCurrentDayOfTheSprint() + "<br><small>"
				+ currentSprint.getSprintTitle()));
		
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
			mDataHandler = new EventDataHandler(getActivity());
		}

		/**
		 * Load the events that are planned in background to an ArrayList of MotivatorEvent objects.
		 */
		@Override
		protected ArrayList<MotivatorEvent> doInBackground(Void... arg0) {
			return mDataHandler.getEventsToday();
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
				final Button eventButton = (Button) mInflater.inflate(R.layout.element_main_activity_button, mEventLayout, false);
				eventButton.setText(result.get(i).getEventDateAsText());
				eventButton.setTextColor(getActivity().getResources().getColor(R.color.green));
				eventButton.setOnClickListener(new OpenEventDetailViewOnClickListener(result.get(i), mContext));
				mEventLayout.addView(eventButton);
				final int eventId = result.get(i).getId();
				
				// Make it possible to cancel the event with long click.
				eventButton.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setMessage(getString(R.string.cancel_event)+ "?").setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mDataHandler.deleteEvent(eventId);
								mEventLayout.removeView(eventButton);
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
					if (mDrinkCounter > 0) {
						addDrinkButton.setText(Html.fromHtml("Add Drink<br> <small>" + mDrinkCounter + " drinks<small>"));
					}
					addDrinkButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							mDataHandler.addDrink();
							mDrinkCounter += 1;
							addDrinkButton.setText(Html.fromHtml("Add Drink<br> <small>" + mDrinkCounter + " drinks<small>"));
						}
					});
					mButtonLayout.addView(addDrinkButton);
				}
			}
		}
	}
}
