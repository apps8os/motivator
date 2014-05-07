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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import android.widget.Toast;
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
	private TextView mPlannedDrinksTextView;
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
		mPlannedDrinksTextView = (TextView) mDrinkButton.findViewById(R.id.card_button_bottom_text);
		final Context context = getActivity();
		addDrink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mDrinkCounter += 1;
				mDayDataHandler.addDrink();
				mDrinkCounterTextView.setText(mDrinkCounter + " " + getString(R.string.drinks_today));
				if (mDrinkCounter > mPlannedDrinks) {
					mPlannedDrinksTextView.setTextColor(mRes.getColor(R.color.red));
					mDrinkButton.setBackgroundResource(R.drawable.card_background_red);
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Kaikki OK?").setMessage("Menit ylitse suunniteltujen juomien.").setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							View toastLayout = (View) mInflater.inflate(R.layout.element_mood_toast, null);
							TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
							toastText.setText("Hyvä! Pidä hauskaa.");
							toastText.setTextColor(Color.WHITE);
							
							Toast canceled = new Toast(context);
							canceled.setDuration(Toast.LENGTH_SHORT);
							canceled.setView(toastLayout);
							canceled.show();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							LinearLayout helpDialogLayout = (LinearLayout) mInflater.inflate(R.layout.element_alcohol_help_dialog, null);
							builder.setView(helpDialogLayout);
							builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									View toastLayout = (View) mInflater.inflate(R.layout.element_mood_toast, null);
									TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
									toastText.setText(getString(R.string.questionnaire_done_toast_bad_mood));
									toastText.setTextColor(Color.WHITE);
									
									Toast canceled = new Toast(context);
									canceled.setDuration(Toast.LENGTH_SHORT);
									canceled.setView(toastLayout);
									canceled.show();
								}
							});
							Dialog helpDialog = builder.create();
							helpDialog.show();
						}
					});
					Dialog dialog = builder.create();
					dialog.show();
				}
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
				if (mDrinkCounter <= mPlannedDrinks) {
					mPlannedDrinksTextView.setTextColor(mRes.getColor(R.color.medium_gray));
					mDrinkButton.setBackgroundResource(R.drawable.card_background);
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
				((TextView) buttonTextLayout.getChildAt(1)).setText(mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_HOW_MUCH).getAnswer(event.getPlannedDrinks()) + " " + getString(R.string.drinks));
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
						builder.setTitle(getString(R.string.cancel_event) + "?").setMessage(getString(R.string.event_will_be_deleted)).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mPlannedDrinks -= eventPlannedDrinks;
								if (mPlannedDrinks == 0) {
									mPlannedDrinksTextView.setText(getString(R.string.no_drinks_planned));
								} else {
									mPlannedDrinksTextView.setText(getString(R.string.drinks_planned) + " " + mPlannedDrinks);
								}
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
			if (mPlannedDrinks == 0) {
				mPlannedDrinksTextView.setText(getString(R.string.no_drinks_planned));
			} else {
				mPlannedDrinksTextView.setText(getString(R.string.drinks_planned) + " " + mPlannedDrinks);
			}
			if (mDrinkCounter > mPlannedDrinks) {
				mPlannedDrinksTextView.setTextColor(mRes.getColor(R.color.red));
				mDrinkButton.setBackgroundResource(R.drawable.card_background_red);
			} else {
				mPlannedDrinksTextView.setTextColor(mRes.getColor(R.color.medium_gray));
				mDrinkButton.setBackgroundResource(R.drawable.card_background);
			}
		}
	}
}
