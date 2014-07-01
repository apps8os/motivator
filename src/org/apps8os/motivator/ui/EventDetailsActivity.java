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

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.Mood;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Question;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

/**
 * Represents a details activity for an event. The event/plan can be in future or in history.
 * The layout and functionality is changed based on this.
 * @author Toni Järvinen
 *
 */
public class EventDetailsActivity extends Activity {
	
	private MotivatorEvent mEvent;
	private MotivatorEvent mCheckedEvent;
	private EventDataHandler mEventDataHandler;
	private Resources mRes;
	private Context mContext;
	private float mScale;
	private int mDrinks;
	
	public static final int EDIT_EVENT_HELP = 10011;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_event_details);
	    Bundle extras = getIntent().getExtras();
	    mEventDataHandler = new EventDataHandler(this);

	    mEvent = extras.getParcelable(MotivatorEvent.EVENT);
	    int section = extras.getInt(MotivatorEvent.SECTION);
	    
	    // If we have come through an notification we need to get the event with the id.
	    if (mEvent == null) {
	    	int eventId = extras.getInt(EventDataHandler.EVENT_ID);
	    	if (eventId != 0) {
	    		mEvent = mEventDataHandler.getUncheckedEvent(eventId);
	    		section = MotivatorEvent.TODAY;
	    	} else {
	    	}
	    }
	    
	    mRes = getResources();
	    mScale = mRes.getDisplayMetrics().density;
	    mContext = this;
	    
	    mDrinks = mEvent.getPlannedDrinks();
	    
	    TextView titleView = (TextView) findViewById(R.id.event_detail_title);
	    final EventDetailsActivity parentActivity = this;
	    mCheckedEvent = null;
	    
	    // Set up the changes button
	    final Button saveChangesButton = (Button) findViewById(R.id.event_detail_save_changes_button);
	    saveChangesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEvent.updateToDatabase(parentActivity);
				parentActivity.finish();
				
			}
	    });
	    // Set the button invisible until something has been changed.
	    saveChangesButton.setVisibility(View.GONE);
	    
	    if (section == MotivatorEvent.TODAY) {
	    	getActionBar().setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_green));
	    	titleView.setTextColor(mRes.getColor(R.color.actionbar_green));
	    	setUpcomingLayout();
	    } else if (section == MotivatorEvent.PLAN) {
	    	getActionBar().setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_blue));
	    	setUpcomingLayout();
	    } else if (section == MotivatorEvent.HISTORY) {
	    	setHistoryLayout();
	    }
	}
	
	/**
	 * Setting up the layout for the plans that have already happened.
	 */
	private void setHistoryLayout() {
		final TextView titleView = (TextView) findViewById(R.id.event_detail_title);
		final TextView startTimeTextView = (TextView) findViewById(R.id.event_time_to_go_entry);
		final TextView endTimeTextView = (TextView) findViewById(R.id.event_end_time_entry);
		final TextView withWhoTextView = (TextView) findViewById(R.id.event_with_who_entry);
		final TextView plannedDrinksTextView = ((TextView) findViewById(R.id.event_amount_of_drinks_entry));
    	final Button okButton = (Button) findViewById(R.id.event_detail_cancel_button);
    	final String eventName = mEvent.getName();
    	
    	DayDataHandler dataHandler = new DayDataHandler(this);
    	DayInHistory day = dataHandler.getDayInHistory(mEvent.getStartTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		
		// The checked event for this plan
    	mCheckedEvent = mEventDataHandler.getCheckedEvent(mEvent.getId());
	    
    	String title = UtilityMethods.getDateAsString(mEvent.getStartTime(), this);
    	getActionBar().setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_orange));
    	titleView.setTextColor(mRes.getColor(R.color.orange));
    	okButton.setText(getString(R.string.ok));
	    okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EventDetailsActivity.super.finish();
			}
	    });
	    
	    if (eventName.length() > 0) {
	    	titleView.setText(Html.fromHtml(title + "<br><small>" + eventName));
	    } else {
	    	titleView.setText(title);
	    }
	    String textToAdd = mEvent.getStartTimeAsText(this);
	    if (textToAdd.length() > 0) {
	    	startTimeTextView.setText(Html.fromHtml(textToAdd));
	    	startTimeTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    textToAdd = mEvent.getEndTimeAsText(this);
	    if (textToAdd.length() > 0) {
	    	endTimeTextView.setText(Html.fromHtml(textToAdd));
	    	endTimeTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    
	    textToAdd = mEvent.getWithWho(this);
	    if (textToAdd.length() > 0) {
	    	withWhoTextView.setText(Html.fromHtml(textToAdd));
	    	withWhoTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    
	    textToAdd = "" + mEvent.getPlannedDrinks();
	    if (textToAdd.length() > 0) {
		    plannedDrinksTextView.setText(textToAdd);
		    plannedDrinksTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    
	    if (mCheckedEvent != null) {
	    	if (mEvent.getPlannedDrinks() >= mCheckedEvent.getPlannedDrinks()) {
		    	titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_mark, 0);
		    	titleView.setPadding((int) (mScale * 21.333), 0, 0, 0);
	    	}
	    	((TextView) findViewById(R.id.event_amount_of_drinks_actual)).setText(getString(R.string.actual)+ ": " + mCheckedEvent.getPlannedDrinks());
	    	if (mCheckedEvent.getStartTimeAsText(this).length() > 0) {
	    		((TextView) findViewById(R.id.event_time_to_go_actual)).setText(getString(R.string.actual)+ ": " + mCheckedEvent.getStartTimeAsText(this));
	    	}
	    	if (mCheckedEvent.getEndTimeAsText(this).length() > 0) {
	    		((TextView) findViewById(R.id.event_end_time_actual)).setText(getString(R.string.actual)+ ": " + mCheckedEvent.getEndTimeAsText(this));
	    	}
	    	if (mCheckedEvent.getWithWho(this).length() > 0) {
	    		((TextView) findViewById(R.id.event_with_who_actual)).setText(getString(R.string.actual)+ ": " + mCheckedEvent.getWithWho(this));
	    	}
	    	
	    } else {
	    	((TextView) findViewById(R.id.event_amount_of_drinks_actual)).setVisibility(View.GONE);
	    	((TextView) findViewById(R.id.event_time_to_go_actual)).setVisibility(View.GONE);
	    	((TextView) findViewById(R.id.event_end_time_actual)).setVisibility(View.GONE);
	    	((TextView) findViewById(R.id.event_with_who_actual)).setVisibility(View.GONE);
	    }
	    
	   
	    Mood firstMood = day.getFirstMoodOfTheDay();
	    
	    // Setting a mood image for the first mood of the following day
	    if (firstMood.getEnergy() > 0) {
	    	// Scale for using density independent pixels
	    	
	    	TextView textView = new TextView(this);
	    	textView.setText(getString(R.string.first_mood_of_the_next_day));
	    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    	params.setMargins((int) (10 * mScale), (int) (20 * mScale), (int) (10 * mScale), (int) (10 * mScale));
	    	textView.setLayoutParams(params);
	    	textView.setGravity(Gravity.CENTER);
	    	textView.setTextSize(14);
	    	textView.setTypeface(null, Typeface.BOLD);
	    	textView.setTextColor(mRes.getColor(R.color.medium_gray));
	    	LinearLayout rootLayout = (LinearLayout) findViewById(R.id.event_info_layout);
	    	LinearLayout moodImage = (LinearLayout) getLayoutInflater().inflate(R.layout.element_mood_image, rootLayout, false);
	    	((ImageView) moodImage.getChildAt(0))
	    			.setImageResource(mRes.getIdentifier("energy" + firstMood.getEnergy(), "drawable", getPackageName()));
	    	((ImageView) moodImage.getChildAt(1))
	    			.setImageResource(mRes.getIdentifier("mood" + firstMood.getMood(), "drawable", getPackageName()));
	    	
	    	
	    	params = new LinearLayout.LayoutParams((int) (105 * mScale), (int) (100 * mScale));
	    	params.gravity = Gravity.CENTER;
	    	moodImage.setLayoutParams(params);
	    	rootLayout.addView(textView);
	    	rootLayout.addView(moodImage);
	    }
	}
	
	/**
	 * Setting up the layout for the plans that have not yet happened.
	 */
	private void setUpcomingLayout() {
		final TextView titleView = (TextView) findViewById(R.id.event_detail_title);
		final Button saveChangesButton = (Button) findViewById(R.id.event_detail_save_changes_button);
	    final TextView startTimeTextView = (TextView) findViewById(R.id.event_time_to_go_entry);
	    final TextView endTimeTextView = (TextView) findViewById(R.id.event_end_time_entry);
	    final TextView withWhoTextView = (TextView) findViewById(R.id.event_with_who_entry);
	    final TextView plannedDrinksTextView = ((TextView) findViewById(R.id.event_amount_of_drinks_entry));
	    
	    final LinearLayout startTimeLayout = (LinearLayout) findViewById(R.id.event_time_to_go);
	    final LinearLayout endTimeLayout = (LinearLayout) findViewById(R.id.event_end_time);
	    final LinearLayout withWhoLayout = (LinearLayout) findViewById(R.id.event_with_who);
	    final LinearLayout plannedDrinksLayout = (LinearLayout) findViewById(R.id.event_amount_of_drinks);
	    
		((TextView) findViewById(R.id.event_amount_of_drinks_actual)).setVisibility(View.GONE);
    	((TextView) findViewById(R.id.event_time_to_go_actual)).setVisibility(View.GONE);
    	((TextView) findViewById(R.id.event_end_time_actual)).setVisibility(View.GONE);
    	((TextView) findViewById(R.id.event_with_who_actual)).setVisibility(View.GONE);
    	
    	// Help view for the editing of an event
    	new ShowcaseView.Builder(this, true)
		    .setTarget(new ViewTarget(startTimeTextView))
		    .setContentTitle("Voit muuttaa suunnitelmaasi")
		    .setContentText("Kosketa kynällä merkittyjä osioita muuttaaksesi niitä.")
		    .hideOnTouchOutside()
		    .setStyle(R.style.ShowcaseView)
		    .singleShot(EDIT_EVENT_HELP)
		    .build();
    	
    	String title;
    	Calendar calendar = Calendar.getInstance();
    	UtilityMethods.setToDayStart(calendar);
    	if (mEvent.getStartTime() - calendar.getTimeInMillis() < TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
    		title = getString(R.string.today);
    	} else if (mEvent.getStartTime() - calendar.getTimeInMillis() < TimeUnit.MILLISECONDS.convert(2, TimeUnit.DAYS)) {
    		title = getString(R.string.tomorrow);
    	} else {
    		title = UtilityMethods.getDateAsString(mEvent.getStartTime(), this);
    	}
    	Button cancelButton = (Button) findViewById(R.id.event_detail_cancel_button);
	    cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(getString(R.string.cancel_event) + "?")
						.setMessage(getString(R.string.event_will_be_deleted))
						.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						mEventDataHandler.deleteEvent(mEvent.getId());
						
						View toastLayout = (View) getLayoutInflater().inflate(R.layout.element_mood_toast, null);
						TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
						toastText.setText(getString(R.string.event_canceled));
						toastText.setTextColor(Color.WHITE);
						

						Toast canceled = new Toast(mContext);
						canceled.setDuration(Toast.LENGTH_SHORT);
						canceled.setView(toastLayout);
						canceled.show();
						EventDetailsActivity.super.finish();
					}
				}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				Dialog dialog = builder.create();
				dialog.show();
			}
	    	
	    });
	    
	    startTimeTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0);
	    startTimeTextView.setPadding(0, 0, ((int) (mScale * 42.667)), 0);
	    startTimeLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				
				Question whenToGo = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_TIME_TO_GO);
				final String[] answers = whenToGo.getAnswers();
				builder.setTitle(whenToGo.getQuestion())
						.setSingleChoiceItems(answers, mEvent.getStartTimeAnswer() - 1, null);
				final AlertDialog alertDialog = builder.create();
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mEvent.setStartTimeAnswer(alertDialog.getListView().getCheckedItemPosition() + 1);
						startTimeTextView.setText(mEvent.getStartTimeAsText(mContext));
						startTimeTextView.setTextColor(mRes.getColor(R.color.purple));
						saveChangesButton.setVisibility(View.VISIBLE);
					}
				});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				alertDialog.show();
			}
	    	
	    });
	    
	    endTimeTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0);
	    endTimeTextView.setPadding(0, 0, (int) (mScale * 42.667), 0);
	    endTimeLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				
				Question endTime = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_TIME_TO_COME_BACK);
				final String[] answers = endTime.getAnswers();
				builder.setTitle(endTime.getQuestion())
						.setSingleChoiceItems(answers, mEvent.getEndTimeAnswer() - 1, null);
				final AlertDialog alertDialog = builder.create();
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mEvent.setEndTimeAnswer(alertDialog.getListView().getCheckedItemPosition() + 1);
						endTimeTextView.setText(mEvent.getEndTimeAsText(mContext));
						endTimeTextView.setTextColor(mRes.getColor(R.color.purple));
						saveChangesButton.setVisibility(View.VISIBLE);
					}
				});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				alertDialog.show();
			}
	    });
	    
	    withWhoTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0);
	    withWhoTextView.setPadding(0, 0, (int) (mScale * 42.667), 0);
	    withWhoLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				
				Question withWho = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_WITH_WHO);
				final String[] answers = withWho.getAnswers();
				builder.setTitle(withWho.getQuestion())
						.setSingleChoiceItems(answers, mEvent.getWithWhoAnswer() - 1, null);
				final AlertDialog alertDialog = builder.create();
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mEvent.setWithWhoAnswer(alertDialog.getListView().getCheckedItemPosition() + 1);
						withWhoTextView.setText(mEvent.getWithWho(mContext));
						withWhoTextView.setTextColor(mRes.getColor(R.color.purple));
						saveChangesButton.setVisibility(View.VISIBLE);
					}
				});
				alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				alertDialog.show();
			}
	    	
	    });
	    
	    plannedDrinksTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.edit_icon, 0, 0, 0);
	    plannedDrinksTextView.setPadding(0, 0, (int) (mScale * 42.667), 0);
	    plannedDrinksLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final LinearLayout amountLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.element_number_entry, null);
				final EditText input = (EditText) amountLayout.findViewById(R.id.number_entry_edit_text);
				input.setHint(getString(R.string.amount_of_drinks));
				final AlertDialog alertDialog = new AlertDialog.Builder(mContext)
					    .setTitle("Syötä määrä")
					    .setView(amountLayout)
					    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int whichButton) {
					        }
					    })
					    .show();
				input.setText("" + mDrinks);
				Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
				theButton.setOnClickListener(new OnClickListener() {
					

					@Override
					public void onClick(View v) {
						String value = input.getText().toString();
						if (value.length() < 1) {
							Toast.makeText(mContext, "Syötä määrä", Toast.LENGTH_SHORT).show();
						} else {
							alertDialog.dismiss();
							mDrinks = Integer.parseInt(value);
							mEvent.setPlannedDrinks(mDrinks);
							plannedDrinksTextView.setText("" + mDrinks);
							plannedDrinksTextView.setTextColor(mRes.getColor(R.color.purple));
							saveChangesButton.setVisibility(View.VISIBLE);
						}
					}
				});
			}
	    	
	    });
	    
	    final String eventName = mEvent.getName();
	    if (eventName.length() > 0) {
	    	titleView.setText(Html.fromHtml(title + "<br><small>" + eventName));
	    } else {
	    	titleView.setText(title);
	    }
	    String textToAdd = mEvent.getStartTimeAsText(this);
	    if (textToAdd.length() > 0) {
	    	startTimeTextView.setText(Html.fromHtml(textToAdd));
	    	startTimeTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    textToAdd = mEvent.getEndTimeAsText(this);
	    if (textToAdd.length() > 0) {
	    	endTimeTextView.setText(Html.fromHtml(textToAdd));
	    	endTimeTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    
	    textToAdd = mEvent.getWithWho(this);
	    if (textToAdd.length() > 0) {
	    	withWhoTextView.setText(Html.fromHtml(textToAdd));
	    	withWhoTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    
	    textToAdd = "" + mEvent.getPlannedDrinks();
	    if (textToAdd.length() > 0) {
		    plannedDrinksTextView.setText(textToAdd);
		    plannedDrinksTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	}
}
