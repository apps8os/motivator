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

import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.Question;

import org.apps8os.motivator.R;
import android.app.Activity;
import android.database.Cursor;
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
	
	public final static String KEY_EVENT_ID = "event_id";
	private int mEventId;
	private EventDataHandler mDataHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_event_details);
	    Bundle extras = getIntent().getExtras();
	    // is represented as answers id in the database
	    mEventId = extras.getInt(KEY_EVENT_ID);
	    mDataHandler = new EventDataHandler(this);
	    mDataHandler.open();
	    
	    // Get the event answers with the id
	    Cursor eventData = mDataHandler.getEventWithId(mEventId);
	    eventData.moveToFirst();
	    
	    Question question = mDataHandler.getQuestion(eventData.getInt(0));
	    TextView title = (TextView) findViewById(R.id.event_detail_title);
	    title.setText(question.getAnswer(eventData.getInt(1)));
	    eventData.moveToNext();
	    
	    // Next answers
	    TextView text = (TextView) findViewById(R.id.event_detail_text);
	    question = mDataHandler.getQuestion(eventData.getInt(0));
	    String textToAdd = question.getAnswer(eventData.getInt(1));
	    eventData.moveToNext();
	    question = mDataHandler.getQuestion(eventData.getInt(0));
	    textToAdd = textToAdd + " <br> " + question.getAnswer(eventData.getInt(1));
	    text.setText(Html.fromHtml(textToAdd));
	    
	    
	    
	    eventData.close();
	    final EventDetailsActivity parentActivity = this;
	    Button cancelButton = (Button) findViewById(R.id.event_detail_cancel_button);
	    cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Delete the answers with the event/answers id
				mDataHandler.deleteEvent(mEventId);
				parentActivity.finish();
			}
	    	
	    });
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mDataHandler.open();
	}
	
	public void onStop() {
		super.onStart();
		mDataHandler.close();
	}

}
