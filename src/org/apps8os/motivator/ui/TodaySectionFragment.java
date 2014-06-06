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
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Sprint;

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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Represents the today section in the UI.
 */
public class TodaySectionFragment extends Fragment {
	
	private EventDataHandler mEventDataHandler;
	private DayDataHandler mDayDataHandler;
	// private GoalDataHandler mGoalDataHandler;
	private LinearLayout mEventLayout;
	private LinearLayout mDrinkButton;
	private LinearLayout mMoodButton;
	private TextView mDrinkCounterTextView;
	private TextView mPlannedDrinksTextView;
	private Resources mRes;
	private View mRootView;
	private LayoutInflater mInflater;
	private Context mContext;
	private boolean mNoActiveSprintLayoutVisible = false;
	private int mDrinkCounter = 0;
	private int mPlannedDrinks = 0;
	private int mShowHelpAmount = 1;
	// private LinearLayout mGoalLayout;
	
	private int[] mAmountImages = {R.drawable.amount_zero, R.drawable.amount_one,
			R.drawable.amount_two, R.drawable.amount_three, R.drawable.amount_fourplus};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mInflater = inflater;
		mRootView = mInflater.inflate(
				R.layout.fragment_main_activity_today_section, container, false);
		mRes = getResources();
		// The layout which has dynamic amount of future events/buttons.
		mEventLayout = (LinearLayout) mRootView.findViewById(R.id.today_section_events);
		// mGoalLayout = (LinearLayout) mRootView.findViewById(R.id.today_section_goals);
		
		mContext = getActivity();
		mDayDataHandler = new DayDataHandler(mContext);
		// mGoalDataHandler = new GoalDataHandler(getActivity());
		mEventDataHandler = new EventDataHandler(mContext);
		
		// two buttons that are always present
		mMoodButton = (LinearLayout) mRootView.findViewById(R.id.main_activity_today_mood_button);
		mMoodButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DayInHistory yesterday = mDayDataHandler.getDayInHistory(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
			    yesterday.setEvents();
				Intent intent = new Intent(getActivity(), MoodQuestionActivity.class);
				startActivity(intent);
			}
		});
		
		setDrinksButton();
		
		return mRootView;
	}
	
	/**
	 * Update the events for today on resume with an async task.
	 */
	@Override
	public void onResume() {
		Sprint currentSprint = getArguments().getParcelable(Sprint.CURRENT_SPRINT);
		// ProgressBar sprintProgress = (ProgressBar) mRootView.findViewById(R.id.today_section_sprint_progress_bar);
		// TextView sprintTextView = (TextView) mRootView.findViewById(R.id.today_section_sprint_progress_text);
		
		mDrinkCounter = mDayDataHandler.getDrinksForDay(System.currentTimeMillis());
		mDrinkCounterTextView.setText(mDrinkCounter + " " + getString(R.string.drinks_today));
		
		/** Sprint progress layout, commented out for now
		sprintProgress.setMax(currentSprint.getDaysInSprint() -1);
		sprintProgress.setProgress(currentSprint.getCurrentDayOfTheSprint());
		sprintTextView.setText(Html.fromHtml(getString(R.string.day) + " " + currentSprint.getCurrentDayOfTheSprint() + "/" + currentSprint.getDaysInSprint()));
		**/
		
		if (currentSprint.isOver() && !mNoActiveSprintLayoutVisible) {
			if (mRootView.findViewById(R.id.no_sprint_overlay) == null) {
				mInflater.inflate(R.layout.element_no_active_sprint_overlay, ((FrameLayout) mRootView.findViewById(R.id.root_view)), true);
				((Button) mRootView.findViewById(R.id.start_new_sprint)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(mContext, StartingSprintActivity.class);
						startActivity(intent);
					}
					
				});
			} else {
				mRootView.findViewById(R.id.no_sprint_overlay).setVisibility(View.VISIBLE);
			}
			mNoActiveSprintLayoutVisible = true;
		}
		new LoadPlansTask(getActivity()).execute();
		// new LoadGoalsTask().execute();

		super.onResume();
	}
	
	/**
	 * Sets the layout, which allows the user to add drinks, with listeners.
	 */
	private void setDrinksButton() {
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
				
				
				// Set the color of the drinks button to red if drinks go over the planned amount.
				if (mDrinkCounter > mPlannedDrinks) {
					mPlannedDrinksTextView.setTextColor(mRes.getColor(R.color.red));
					mDrinkButton.setBackgroundResource(R.drawable.card_background_red);
				}
				
				// Opening prompts depending on the amount of drinks drunk and planned amounts
				if (mPlannedDrinks == 0 && mDrinkCounter == 1) {
					
					// Set up a prompt for adding a plan if there are no plans and the user adds a drink.
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(getString(R.string.you_have_no_plans))
						.setMessage(getString(R.string.do_you_want_to_add_event))
						.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							Intent intent = new Intent(getActivity(), AddEventActivity.class);
							startActivity(intent);
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					Dialog dialog = builder.create();
					dialog.show();
					
				} else if (mPlannedDrinks > 0 && mPlannedDrinks < 4 && mDrinkCounter == mPlannedDrinks + 1) {
					
					// Set up a prompt asking whether everything is ok if the drink amount goes over the planned.
					// If user has planned to drink over 4, dont ask this.
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle(getString(R.string.you_went_over_planned_drinks))
						.setMessage(getString(R.string.is_everything_ok))
						.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							View toastLayout = (View) mInflater.inflate(R.layout.element_mood_toast, null);
							TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
							toastText.setText("Hyvä! Pidä hauskaa.");
							toastText.setTextColor(Color.WHITE);
							
							Toast moodToast = new Toast(context);
							moodToast.setDuration(Toast.LENGTH_SHORT);
							moodToast.setView(toastLayout);
							moodToast.show();
						}
					}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							// Set up a dialog with info on where to get help if user answers everything is not ok
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
									
									Toast moodToast = new Toast(context);
									moodToast.setDuration(Toast.LENGTH_SHORT);
									moodToast.setView(toastLayout);
									moodToast.show();
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
			if (resultSize == 0) {
				LinearLayout noEventsText = (LinearLayout) mInflater.inflate(R.layout.element_no_events, mEventLayout, false);
				mEventLayout.addView(noEventsText);
				/**
				noEventsText.setClickable(true);
				noEventsText.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(getActivity(), AddEventActivity.class);
						startActivity(intent);
					}
				});
				**/
			}
			// Create buttons for the result set.
			for (MotivatorEvent event : result) {
				event.setEventDateAsText(getString(R.string.today));
				final LinearLayout eventButton = (LinearLayout) mInflater.inflate(R.layout.element_larger_event_card_button, mEventLayout, false);
				LinearLayout buttonTextLayout = (LinearLayout) eventButton.getChildAt(0);
				String eventName = event.getName();
				String startTimeAsText = event.getStartTimeAsText(mContext);
				LinearLayout drinkAmountLayout = ((LinearLayout) buttonTextLayout.getChildAt(1));
				((TextView) drinkAmountLayout.getChildAt(1)).setText(" " + getString(R.string.drinks));
				((ImageView) eventButton.findViewById(R.id.drink_amount_image)).setImageResource(mAmountImages[event.getPlannedDrinks()]);
				
				if (eventName.length() > 0) {
					((TextView) buttonTextLayout.getChildAt(0)).setText(eventName);
				} else {
					((TextView) buttonTextLayout.getChildAt(0)).setText(event.getEventDateAsText());
				}
				if (startTimeAsText.length() > 0) {
					((TextView) buttonTextLayout.getChildAt(2)).setText(getString(R.string.when_to_go) + ": " + startTimeAsText);
				} else {
					((TextView) buttonTextLayout.getChildAt(2)).setVisibility(View.GONE);
				}
				((ImageView) eventButton.getChildAt(2)).setImageResource(R.drawable.event_today_icon);
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
									String drinkAmount;
									if (mPlannedDrinks < 4) {
										drinkAmount = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_HOW_MUCH).getAnswer(mPlannedDrinks + 1);
									} else {
										drinkAmount = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_HOW_MUCH).getAnswer(5);
									}
									mPlannedDrinksTextView.setText(getString(R.string.drinks_planned) + " " + drinkAmount);
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
				String drinkAmount;
				if (mPlannedDrinks < 4) {
					drinkAmount = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_HOW_MUCH).getAnswer(mPlannedDrinks + 1);
				} else {
					drinkAmount = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_HOW_MUCH).getAnswer(5);
				}
				mPlannedDrinksTextView.setText(getString(R.string.drinks_planned) + " " + drinkAmount);
			}
			if (mDrinkCounter > mPlannedDrinks) {
				mPlannedDrinksTextView.setTextColor(mRes.getColor(R.color.red));
				mDrinkButton.setBackgroundResource(R.drawable.card_background_red);
			} else {
				mPlannedDrinksTextView.setTextColor(mRes.getColor(R.color.medium_gray));
				mDrinkButton.setBackgroundResource(R.drawable.card_background);
			}
			if (mShowHelpAmount < mPlannedDrinks) {
				mShowHelpAmount = mPlannedDrinks + 1;
			}
		}
	}
	
	/** Commented out for now
	private class LoadGoalsTask extends AsyncTask<Void, Void, ArrayList<Goal>> {

		@Override
		protected ArrayList<Goal> doInBackground(Void... params) {
			return mGoalDataHandler.getOngoingGoals();
		}
		
		@Override
		protected void onPostExecute(ArrayList<Goal> result) {
			mGoalLayout.removeAllViews();
			int resultSize = result.size();
			if (resultSize == 0) {
				LinearLayout noEventsText = (LinearLayout) mInflater.inflate(R.layout.element_no_events, mGoalLayout, false);
				((TextView) noEventsText.getChildAt(0)).setText(getString(R.string.no_goals));
				mGoalLayout.addView(noEventsText);
			} else {
				Goal goal;
				for (int i = 0; i < resultSize; i++) {
					goal = result.get(i);
					final LinearLayout goalButton = (LinearLayout) mInflater.inflate(R.layout.element_goal_button, mGoalLayout, false);
					LinearLayout buttonTextLayout = (LinearLayout) goalButton.getChildAt(0);
					((TextView) buttonTextLayout.getChildAt(0)).setText(goal.getGoalAsText());
					int days = goal.getDays();
					if (goal.getGoalId() < 2) {
						((TextView) buttonTextLayout.getChildAt(1)).setText(goal.getCurrentAmount(getActivity()) + "/" + goal.getTargetAmount());
					}  else {
						((TextView) buttonTextLayout.getChildAt(1)).setVisibility(View.GONE);
					}
					ProgressBar goalProgress = (ProgressBar) buttonTextLayout.getChildAt(2);
					goalProgress.setMax(goal.getDays());
					goalProgress.setProgress(goal.getCurrentDayInGoal());
					mGoalLayout.addView(goalButton);
				}
			}
		}
		
	}
	**/
}
