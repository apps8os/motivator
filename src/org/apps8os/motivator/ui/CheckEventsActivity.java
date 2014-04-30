package org.apps8os.motivator.ui;

import java.util.ArrayList;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;

import com.viewpagerindicator.LinePageIndicator;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class CheckEventsActivity extends Activity {

	private EventDataHandler mDataHandler;
	private ViewPager mViewPager;
	private EventsPagerAdapter mEventsPagerAdapter;
	private ArrayList<MotivatorEvent> mEvents;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_questions);
	    
	    mEvents = getIntent().getExtras().getParcelableArrayList(MotivatorEvent.YESTERDAYS_EVENTS);
	    mDataHandler = new EventDataHandler(this);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_blue));
		
	    mViewPager = (ViewPager) findViewById(R.id.question_activity_pager);
	    mEventsPagerAdapter = new EventsPagerAdapter(
	    		getFragmentManager(), mEvents.size());
	    mViewPager.setAdapter(mEventsPagerAdapter);
	    mViewPager.setOffscreenPageLimit(10);
	    
	    LinePageIndicator titleIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setViewPager(mViewPager);
	}
	
	
	private class EventsPagerAdapter extends FragmentPagerAdapter {
		
		private int mSize;

		public EventsPagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
		}

		@Override
		public Fragment getItem(int arg0) {
			EventToCheckFragment fragment = new EventToCheckFragment();
			Bundle b = new Bundle();
			b.putParcelable(MotivatorEvent.EVENT, mEvents.get(arg0));
			fragment.setArguments(b);
			return fragment;
		}

		@Override
		public int getCount() {
			return mSize;
		}
	}

}
