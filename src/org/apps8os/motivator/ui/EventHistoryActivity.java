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
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.data.SprintDataHandler;
import org.apps8os.motivator.ui.MoodHistoryActivity.DatePickerFragment;
import org.apps8os.motivator.utils.UtilityMethods;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventHistoryActivity extends Activity {
	
	private EventDataHandler mEventDataHandler;
	private Sprint mSelectedSprint;
	private LinearLayout mEventLayout;
	private Resources mRes;
	
	public static final int CHANGE_SPRINT_HELP = 10010;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_event_history);
	    mSelectedSprint = (Sprint) getIntent().getExtras().get(Sprint.CURRENT_SPRINT);
	    mEventLayout = (LinearLayout) findViewById(R.id.events_layout);
	    mRes = getResources();
	    ActionBar actionBar = getActionBar();
	    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_orange));
	    actionBar.setTitle(getString(R.string.event_history));
	    
	    new LoadPlansTask(this).execute();
	}
	
	/**
	 * Loading the action bar menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.event_history, menu);
	    new ShowcaseView.Builder(this, true)
	    .setTarget(new ActionViewTarget(this, ActionViewTarget.Type.OVERFLOW))
	    .setContentTitle("Vaihda jaksoa")
	    .setContentText("Täältä voit vaihtaa jakson jonka suunnitelmat näytetään.")
	    .hideOnTouchOutside()
	    .setStyle(R.style.ShowcaseView)
	    .singleShot(CHANGE_SPRINT_HELP)
	    .build();
	    
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Actions for the menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.event_history_change_sprint:
			// Spawn a dialog where the user can select the sprint depicted in this history.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			final Sprint[] sprints = new SprintDataHandler(this).getSprints();
			
			// Get the string representations of the sprints.
			String[] sprintsAsString = new String[sprints.length];
			for (int i = 0; i < sprints.length; i++) {
				sprintsAsString[i] = sprints[i].getSprintTitle() + " " + sprints[i].getStartTimeInString(this);
			}
			builder.setTitle(getString(R.string.select_sprint))
					.setSingleChoiceItems(sprintsAsString, sprints.length-1, null);
			final AlertDialog alertDialog = builder.create();
			final Activity thisActivity = this;
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// Start this activity again with the selected sprint as the passed Parcelable Sprint.
					// This is done so that the activity can update itself to the selected sprint.
					mSelectedSprint = sprints[alertDialog.getListView().getCheckedItemPosition()];
					Intent intent = new Intent (thisActivity, EventHistoryActivity.class);
					intent.putExtra(Sprint.CURRENT_SPRINT, mSelectedSprint);
					startActivity(intent);
					alertDialog.dismiss();
					thisActivity.finish();
				}
			});
			alertDialog.show();
		default:
			return super.onOptionsItemSelected(item);
		}
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
			mEventDataHandler = new EventDataHandler(context);
		}

		/**
		 * Load the events that are planned in background to an ArrayList of MotivatorEvent objects.
		 */
		@Override
		protected ArrayList<MotivatorEvent> doInBackground(Void... arg0) {
			if (mSelectedSprint.getEndTime() > System.currentTimeMillis()) {
				return mEventDataHandler.getUncheckedEventsBetween(mSelectedSprint.getStartTime(), System.currentTimeMillis());
			}
			return mEventDataHandler.getUncheckedEventsBetween(mSelectedSprint.getStartTime(), mSelectedSprint.getEndTime());
		}
		
		/**
		 * Creates buttons for the upcoming events provided by doInBackground.
		 */
		@Override
		protected void onPostExecute(ArrayList<MotivatorEvent> result) {
			mEventLayout.removeAllViews();
			
			final int resultSize = result.size();
			LayoutInflater inflater = getLayoutInflater();
			if (resultSize == 0) {
				LinearLayout noEventsText = (LinearLayout) inflater.inflate(R.layout.element_no_events, mEventLayout, false);
				mEventLayout.addView(noEventsText);
			}
			// Create buttons for the result set.
			for (MotivatorEvent event : result) {
				event.setEventDateAsText(getString(R.string.today));
				final LinearLayout eventButton = (LinearLayout) inflater.inflate(R.layout.element_main_activity_card_button, mEventLayout, false);
				LinearLayout buttonTextLayout = (LinearLayout) eventButton.getChildAt(0);
				final String eventName = event.getName();
				long eventStartTime = event.getStartTime();
				((TextView) buttonTextLayout.findViewById(R.id.card_button_top_text))
						.setText(UtilityMethods.getDateAsString(eventStartTime, mContext));
				
				MotivatorEvent checked = mEventDataHandler.getCheckedEvent(event.getId());
				
				if (checked != null) {
					Drawable checkMark = mRes.getDrawable(R.drawable.check_mark);
					((TextView) buttonTextLayout.findViewById(R.id.card_button_top_text))
							.setCompoundDrawablesWithIntrinsicBounds(null, null, checkMark, null);
					if (eventName.length() > 0) {
						((TextView) buttonTextLayout.findViewById(R.id.card_button_bottom_text))
								.setText(eventName + " \u25A0 " + checked.getPlannedDrinks() + " " + getString(R.string.drinks));
					} else {
						((TextView) buttonTextLayout.findViewById(R.id.card_button_bottom_text))
								.setText(checked.getPlannedDrinks() + " " + getString(R.string.drinks));
					}
				} else {
				
					if (eventName.length() > 0) {
						((TextView) buttonTextLayout.findViewById(R.id.card_button_bottom_text))
								.setText(eventName + " \u25A0 " + event.getPlannedDrinks() + " " + getString(R.string.drinks));
					} else {
						((TextView) buttonTextLayout.findViewById(R.id.card_button_bottom_text))
								.setText(event.getPlannedDrinks() + " " + getString(R.string.drinks));
					}
				}
				
				((TextView) buttonTextLayout.findViewById(R.id.card_button_top_text))
						.setTextColor(mRes.getColor(R.color.actionbar_orange));
				((TextView) buttonTextLayout.findViewById(R.id.card_button_bottom_text))
						.setTextColor(mRes.getColor(R.color.medium_gray));
				((ImageView) eventButton.findViewById(R.id.card_button_right_image))
						.setImageResource(R.drawable.calendar_past_icon);
				
				eventButton.setOnClickListener(new OpenEventDetailViewOnClickListener(event, mContext, MotivatorEvent.HISTORY));
				mEventLayout.addView(eventButton);
			}
		}
	}

}
