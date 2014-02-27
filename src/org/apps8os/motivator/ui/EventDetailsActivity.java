package org.apps8os.motivator.ui;

import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.Question;

import org.apps8os.motivator.R;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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
	    mEventId = extras.getInt(KEY_EVENT_ID);
	    mDataHandler = new EventDataHandler(this);
	    mDataHandler.open();
	    Cursor eventData = mDataHandler.getEventWithId(mEventId);
	    eventData.moveToFirst();
	    
	    Question question = mDataHandler.getQuestion(eventData.getInt(0));
	    TextView title = (TextView) findViewById(R.id.event_detail_title);
	    title.setText(question.getAnswer(eventData.getInt(1)));
	    eventData.moveToNext();
	    mDataHandler.close();
	    
	    Button cancelButton = (Button) findViewById(R.id.event_detail_cancel_button);
	    cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mDataHandler.deleteEvent(mEventId);
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
