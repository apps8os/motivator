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
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.data.SprintDataHandler;
import org.apps8os.motivator.utils.UtilityMethods;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * Represents a details activity for an event.
 * @author Toni Järvinen
 *
 */
public class EventDetailsActivity extends Activity {
	
	private MotivatorEvent mEvent;
	private EventDataHandler mEventDataHandler;
	private Resources mRes;
	
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
	    if (mEvent == null) {
	    	int eventId = extras.getInt(EventDataHandler.EVENT_ID);
	    	if (eventId != 0) {
	    		mEvent = mEventDataHandler.getUncheckedEvent(eventId);
	    		section = MotivatorEvent.TODAY;
	    	} else {
	    	}
	    }
	    
	    mRes = getResources();
	    final Context context = this;
	    
	    TextView titleView = (TextView) findViewById(R.id.event_detail_title);
	    String title; 
	    final EventDetailsActivity parentActivity = this;
	    final int eventId = mEvent.getId();
	    MotivatorEvent checkedEvent = null;
	    final Button saveChangesButton = (Button) findViewById(R.id.event_detail_save_changes_button);
	    final TextView startTimeTextView = (TextView) findViewById(R.id.event_time_to_go_entry);
	    final TextView endTimeTextView = (TextView) findViewById(R.id.event_end_time_entry);
	    final TextView withWhoTextView = (TextView) findViewById(R.id.event_with_who_entry);
	    final TextView plannedDrinksTextView = ((TextView) findViewById(R.id.event_amount_of_drinks_entry));
	    saveChangesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mEvent.updateToDatabase(parentActivity);
				parentActivity.finish();
				
			}
	    });
	    saveChangesButton.setVisibility(View.GONE);
	    if (section == MotivatorEvent.TODAY) {
	    	getActionBar().setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_green));
	    	titleView.setTextColor(mRes.getColor(R.color.actionbar_green));
	    } else if (section == MotivatorEvent.PLAN) {
	    	getActionBar().setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_blue));
	    }
	    
	    if (section == MotivatorEvent.HISTORY) {
	    	checkedEvent = mEventDataHandler.getCheckedEvent(mEvent.getId());
		    
	    	title = UtilityMethods.getDateAsString(mEvent.getStartTime(), this);
	    	getActionBar().setBackgroundDrawable(mRes.getDrawable(R.drawable.action_bar_orange));
	    	titleView.setTextColor(mRes.getColor(R.color.orange));
	    	Button okButton = (Button) findViewById(R.id.event_detail_cancel_button);
	    	okButton.setText(getString(R.string.ok));
		    okButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					parentActivity.finish();
				}
		    });
		    
		    if (checkedEvent != null) {
		    	titleView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_mark, 0);
		    	((TextView) findViewById(R.id.event_amount_of_drinks_actual)).setText(getString(R.string.actual)+ ": " + checkedEvent.getPlannedDrinks());
		    	if (checkedEvent.getStartTimeAsText(this).length() > 0) {
		    		((TextView) findViewById(R.id.event_time_to_go_actual)).setText(getString(R.string.actual)+ ": " + checkedEvent.getStartTimeAsText(this));
		    	}
		    	if (checkedEvent.getEndTimeAsText(this).length() > 0) {
		    		((TextView) findViewById(R.id.event_end_time_actual)).setText(getString(R.string.actual)+ ": " + checkedEvent.getEndTimeAsText(this));
		    	}
		    	if (checkedEvent.getWithWho(this).length() > 0) {
		    		((TextView) findViewById(R.id.event_with_who_actual)).setText(getString(R.string.actual)+ ": " + checkedEvent.getWithWho(this));
		    	}
		    	
		    } else {
		    	((TextView) findViewById(R.id.event_amount_of_drinks_actual)).setVisibility(View.GONE);
		    	((TextView) findViewById(R.id.event_time_to_go_actual)).setVisibility(View.GONE);
		    	((TextView) findViewById(R.id.event_end_time_actual)).setVisibility(View.GONE);
		    	((TextView) findViewById(R.id.event_with_who_actual)).setVisibility(View.GONE);
		    }
		    
		    DayDataHandler dataHandler = new DayDataHandler(this);
		    DayInHistory day = dataHandler.getDayInHistory(mEvent.getStartTime() + TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
		    Mood firstMood = day.getFirstMoodOfTheDay();
		    
		    // Setting a mood image for the first mood of the following day
		    if (firstMood.getEnergy() > 0) {
		    	// Scale for using density independent pixels
		    	final float scale = mRes.getDisplayMetrics().density;
		    	TextView textView = new TextView(this);
		    	textView.setText(getString(R.string.first_mood_of_the_next_day));
		    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		    	params.setMargins((int) (10 * scale), (int) (20 * scale), (int) (10 * scale), (int) (10 * scale));
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
		    	
		    	
		    	params = new LinearLayout.LayoutParams((int) (105 * scale), (int) (100 * scale));
		    	params.gravity = Gravity.CENTER;
		    	moodImage.setLayoutParams(params);
		    	rootLayout.addView(textView);
		    	rootLayout.addView(moodImage);
		    }
	    } else {
	    	((TextView) findViewById(R.id.event_amount_of_drinks_actual)).setVisibility(View.GONE);
	    	((TextView) findViewById(R.id.event_time_to_go_actual)).setVisibility(View.GONE);
	    	((TextView) findViewById(R.id.event_end_time_actual)).setVisibility(View.GONE);
	    	((TextView) findViewById(R.id.event_with_who_actual)).setVisibility(View.GONE);
	    	
	    	new ShowcaseView.Builder(this, true)
			    .setTarget(new ViewTarget(startTimeTextView))
			    .setContentTitle("Muuta suunnitelmaa")
			    .setContentText("Kosketa suunnittelmia muuttaaksesi niitä.")
			    .hideOnTouchOutside()
			    .setStyle(R.style.ShowcaseView)
			    .singleShot(EDIT_EVENT_HELP)
			    .build();
	    	
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
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle(getString(R.string.cancel_event) + "?")
							.setMessage(getString(R.string.event_will_be_deleted))
							.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							mEventDataHandler.deleteEvent(eventId);
							
							View toastLayout = (View) getLayoutInflater().inflate(R.layout.element_mood_toast, null);
							TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
							toastText.setText(getString(R.string.event_canceled));
							toastText.setTextColor(Color.WHITE);
							

							Toast canceled = new Toast(context);
							canceled.setDuration(Toast.LENGTH_SHORT);
							canceled.setView(toastLayout);
							canceled.show();
							parentActivity.finish();
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
		    
		    startTimeTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.super);
					
					Question whenToGo = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_TIME_TO_GO);
					final String[] answers = whenToGo.getAnswers();
					builder.setTitle(whenToGo.getQuestion())
							.setSingleChoiceItems(answers, mEvent.getStartTimeAnswer() - 1, null);
					final AlertDialog alertDialog = builder.create();
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mEvent.setStartTimeAnswer(alertDialog.getListView().getCheckedItemPosition() + 1);
							startTimeTextView.setText(mEvent.getStartTimeAsText(EventDetailsActivity.super));
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
		    
		    endTimeTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.super);
					
					Question endTime = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_TIME_TO_COME_BACK);
					final String[] answers = endTime.getAnswers();
					builder.setTitle(endTime.getQuestion())
							.setSingleChoiceItems(answers, mEvent.getEndTimeAnswer() - 1, null);
					final AlertDialog alertDialog = builder.create();
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mEvent.setEndTimeAnswer(alertDialog.getListView().getCheckedItemPosition() + 1);
							endTimeTextView.setText(mEvent.getEndTimeAsText(EventDetailsActivity.super));
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
		    
		    withWhoTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.super);
					
					Question withWho = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_WITH_WHO);
					final String[] answers = withWho.getAnswers();
					builder.setTitle(withWho.getQuestion())
							.setSingleChoiceItems(answers, mEvent.getWithWhoAnswer() - 1, null);
					final AlertDialog alertDialog = builder.create();
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mEvent.setWithWhoAnswer(alertDialog.getListView().getCheckedItemPosition() + 1);
							withWhoTextView.setText(mEvent.getWithWho(EventDetailsActivity.super));
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
		    
		    plannedDrinksTextView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder builder = new AlertDialog.Builder(EventDetailsActivity.super);
					
					Question plannedDrinks = mEventDataHandler.getQuestion(EventDataHandler.QUESTION_ID_HOW_MUCH);
					final String[] answers = plannedDrinks.getAnswers();
					builder.setTitle(plannedDrinks.getQuestion())
							.setSingleChoiceItems(answers, mEvent.getPlannedDrinks(), null);
					final AlertDialog alertDialog = builder.create();
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mEvent.setPlannedDrinks(alertDialog.getListView().getCheckedItemPosition());
							plannedDrinksTextView.setText(mEvent.getPlannedDrinksAsText(context));
							plannedDrinksTextView.setTextColor(mRes.getColor(R.color.purple));
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
	    }
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
	    
	    textToAdd = mEvent.getPlannedDrinksAsText(this);
	    if (textToAdd.length() > 0) {
		    plannedDrinksTextView.setText(textToAdd);
		    plannedDrinksTextView.setTextColor(mRes.getColor(R.color.dark_gray));
	    }
	    
	}
}
