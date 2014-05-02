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

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Represents a details activity for an event.
 * TODO: Only a dummy version, needs to be redesigned
 * @author Toni Järvinen
 *
 */
public class EventDetailsActivity extends Activity {
	
	private MotivatorEvent mEvent;
	private EventDataHandler mDataHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_event_details);
	    Bundle extras = getIntent().getExtras();
	    // is represented as answers id in the database
	    mEvent = extras.getParcelable(MotivatorEvent.EVENT);
	    int section = extras.getInt(MotivatorEvent.SECTION);
	    mDataHandler = new EventDataHandler(this);
	    
	    TextView titleView = (TextView) findViewById(R.id.event_detail_title);
	    String title; 
	    final EventDetailsActivity parentActivity = this;
	    final int eventId = mEvent.getId();
	    if (section == MotivatorEvent.HISTORY) {
	    	title = UtilityMethods.getDateAsString(mEvent.getStartTime(), this);
	    	getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_orange));
	    	titleView.setTextColor(getResources().getColor(R.color.orange));
	    	Button okButton = (Button) findViewById(R.id.event_detail_cancel_button);
	    	okButton.setText(getString(R.string.ok));
		    okButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// Delete the answers with the event/answers id
					parentActivity.finish();
				}
		    	
		    });
	    } else {
	    	title = mEvent.getEventDateAsText();
	    	getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_blue));
	    	Button cancelButton = (Button) findViewById(R.id.event_detail_cancel_button);
		    cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					// Delete the answers with the event/answers id
					mDataHandler.deleteEvent(eventId);
					parentActivity.finish();
				}
		    	
		    });
	    }
	    String eventName = mEvent.getName();
	    if (eventName.length() > 0) {
	    	titleView.setText(Html.fromHtml(title + "<br><small>" + eventName));
	    } else {
	    	titleView.setText(title);
	    }
	    
	    TextView startTimeText = (TextView) findViewById(R.id.event_time_to_go_entry);
	    String textToAdd = mEvent.getStartTimeAsText();
	    startTimeText.setText(Html.fromHtml(textToAdd));
	    
	    TextView endTimeText = (TextView) findViewById(R.id.event_end_time_entry);
	    textToAdd = mEvent.getEndTimeAsText();
	    endTimeText.setText(mEvent.getEndTimeAsText());
	    
	    TextView withWhoText = (TextView) findViewById(R.id.event_with_who_entry);
	    withWhoText.setText(mEvent.getWithWho());
	    
	    ((TextView) findViewById(R.id.event_amount_of_drinks_entry)).setText("" + mEvent.getPlannedDrinks());
	}

}
