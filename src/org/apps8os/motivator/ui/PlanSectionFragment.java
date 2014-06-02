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
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Represents the planning section in the UI.
 * The planned events are drawn in the onResume function.
 * @author Toni Järvinen
 *
 */
public class PlanSectionFragment extends Fragment {

	private EventDataHandler mEventDataHandler;
	private LinearLayout mEventLayout;
	private LayoutInflater mInflater;
	private View mRootView;
	
	public PlanSectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		mRootView = mInflater.inflate(
				R.layout.fragment_main_activity_plan_section, container, false);
		
		// The layout which has dynamic amount of future events/buttons.
		mEventLayout = (LinearLayout) mRootView.findViewById(R.id.main_activity_plan_dynamic_buttons);
		
		// two buttons that are always present
		
		LinearLayout addEventButton = (LinearLayout) mRootView.findViewById(R.id.main_activity_plan_add_event_button);
		addEventButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AddEventActivity.class);
				startActivity(intent);
			}
		});
		/** Commented out for now
		LinearLayout addGoalButton = (LinearLayout) rootView.findViewById(R.id.main_activity_plan_add_goal_button);
		addGoalButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), AddGoalActivity.class);
				startActivity(intent);
			}
		});
		**/
		return mRootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
	
	/**
	 * Update the upcoming events on resume with an async task.
	 */
	@Override
	public void onResume() {
		new LoadPlansTask(getActivity()).execute();
		Sprint currentSprint = getArguments().getParcelable(Sprint.CURRENT_SPRINT);
		if (currentSprint.isOver()) {
			mInflater.inflate(R.layout.element_no_active_sprint_overlay, ((FrameLayout) mRootView.findViewById(R.id.root_view)), true);
		}
		super.onResume();
	}
	
	/**
	 * Inner class for loading the plans to the plan section asynchronously.
	 * @author Toni Järvinen
	 *
	 */
	private class LoadPlansTask extends AsyncTask<Void, Void, ArrayList<MotivatorEvent>> {
		
		private Context mContext;
		private Resources mRes;

		public LoadPlansTask(Context context) {
			mContext = context;
			mRes = getResources();
			
			// Open the database connection
			mEventDataHandler = new EventDataHandler(getActivity());
		}

		/**
		 * Load the events that are planned in background to an ArrayList of AnswerCase objects.
		 */
		@Override
		protected ArrayList<MotivatorEvent> doInBackground(Void... arg0) {		
			return mEventDataHandler.getEventsAfterToday();
		}
		
		/**
		 * Creates buttons for the upcoming events provided by doInBackground.
		 */
		@Override
		protected void onPostExecute(ArrayList<MotivatorEvent> result) {
			mEventLayout.removeAllViews();

			if (result.size() == 0) {
				LinearLayout noEventsText = (LinearLayout) mInflater.inflate(R.layout.element_no_events, mEventLayout, false);
				mEventLayout.addView(noEventsText);
			}
			Calendar calendar = Calendar.getInstance();
			UtilityMethods.setToDayStart(calendar);
			// Create buttons for the result set.
			for (MotivatorEvent event : result) {
				final LinearLayout eventButton = (LinearLayout) mInflater.inflate(R.layout.element_main_activity_card_button, mEventLayout, false);
				LinearLayout buttonTextLayout = (LinearLayout) eventButton.getChildAt(0);
				String eventName = event.getName();
				String startTimeAsText = event.getStartTimeAsText();
				String eventDateAsText;
				if ((event.getStartTime() - calendar.getTimeInMillis()) < TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)) {
					eventDateAsText = getString(R.string.tomorrow);
				} else {
					eventDateAsText = UtilityMethods.getDateAsString(event.getStartTime(), getActivity());
				}
				if (eventName.length() > 0) {
					((TextView) buttonTextLayout.getChildAt(0)).setText(eventName);
					if (startTimeAsText.length() > 0) {
						((TextView) buttonTextLayout.getChildAt(1)).setText(eventDateAsText + " \u25A0 " + startTimeAsText);
					} else {
						((TextView) buttonTextLayout.getChildAt(1)).setText(eventDateAsText);
					}
				} else {
					((TextView) buttonTextLayout.getChildAt(0)).setText(eventDateAsText);
					if (startTimeAsText.length() > 0) {
						((TextView) buttonTextLayout.getChildAt(1)).setText(startTimeAsText);
					} else {
						((TextView) buttonTextLayout.getChildAt(1)).setVisibility(View.GONE);
					}
				}
				((TextView) buttonTextLayout.getChildAt(0)).setTextColor(mRes.getColor(R.color.actionbar_blue));
				((TextView) buttonTextLayout.getChildAt(1)).setTextColor(mRes.getColor(R.color.medium_gray));
				((ImageView) eventButton.getChildAt(2)).setImageResource(R.drawable.calendar_icon);
				
				eventButton.setOnClickListener(new OpenEventDetailViewOnClickListener(event, mContext, MotivatorEvent.PLAN));
				final int eventId = event.getId();
				mEventLayout.addView(eventButton);
				
				// Make it possible to cancel the event with long click.
				eventButton.setOnLongClickListener(new OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle(getString(R.string.cancel_event) + "?")
							.setMessage(getString(R.string.event_will_be_deleted))
							.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mEventDataHandler.deleteEvent(eventId);
								mEventLayout.removeView(eventButton);
								if (mEventLayout.getChildCount() == 0) {
									LinearLayout noEventsText = (LinearLayout) mInflater.inflate(R.layout.element_no_events, mEventLayout, false);
									mEventLayout.addView(noEventsText);
								}
								View toastLayout = (View) mInflater.inflate(R.layout.element_mood_toast, null);
								TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
								toastText.setText(getString(R.string.event_canceled));
								toastText.setTextColor(Color.WHITE);
								
								Toast canceled = new Toast(mContext);
								canceled.setDuration(Toast.LENGTH_SHORT);
								canceled.setView(toastLayout);
								canceled.show();
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
			}
	}
}
