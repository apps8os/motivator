package org.apps8os.motivator.ui;

import java.util.ArrayList;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Sprint;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EventHistoryActivity extends Activity {
	
	private EventDataHandler mEventDataHandler;
	private Sprint mSelectedSprint;
	private LinearLayout mEventLayout;
	private Resources mRes;

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
				return mEventDataHandler.getEventsBetween(mSelectedSprint.getStartTime(), System.currentTimeMillis());
			}
			return mEventDataHandler.getEventsBetween(mSelectedSprint.getStartTime(), mSelectedSprint.getEndTime());
		}
		
		/**
		 * Creates buttons for the upcoming events provided by doInBackground.
		 */
		@Override
		protected void onPostExecute(ArrayList<MotivatorEvent> result) {
			mEventLayout.removeAllViews();
			
			int resultSize = result.size();
			LayoutInflater inflater = getLayoutInflater();
			MotivatorEvent event;
			if (resultSize == 0) {
				LinearLayout noEventsText = (LinearLayout) inflater.inflate(R.layout.element_no_events, mEventLayout, false);
				mEventLayout.addView(noEventsText);
			}
			// Create buttons for the result set.
			for (int i = 0; i < resultSize; i ++) {
				event = result.get(i);
				event.setEventDateAsText(getString(R.string.today));
				final LinearLayout eventButton = (LinearLayout) inflater.inflate(R.layout.element_main_activity_card_button, mEventLayout, false);
				LinearLayout buttonTextLayout = (LinearLayout) eventButton.getChildAt(0);
				String eventName = event.getName();
				long eventStartTime = event.getStartTime();
				String eventDate = UtilityMethods.getDateAsString(eventStartTime, mContext);
				((TextView) buttonTextLayout.getChildAt(0)).setText(eventDate);
				if (eventName.length() > 0) {
					((TextView) buttonTextLayout.getChildAt(1)).setText(eventName + ", " + event.getPlannedDrinks() + " " + getString(R.string.drinks));
				} else {
					((TextView) buttonTextLayout.getChildAt(1)).setText(event.getPlannedDrinks() + " " + getString(R.string.drinks));
				}
				((TextView) buttonTextLayout.getChildAt(0)).setTextColor(mRes.getColor(R.color.medium_gray));
				((TextView) buttonTextLayout.getChildAt(1)).setTextColor(mRes.getColor(R.color.medium_gray));
				((ImageView) eventButton.getChildAt(2)).setImageResource(R.drawable.calendar_past_icon);
				eventButton.setOnClickListener(new OpenEventDetailViewOnClickListener(event, mContext, MotivatorEvent.HISTORY));
				mEventLayout.addView(eventButton);
			}
		}
	}

}
