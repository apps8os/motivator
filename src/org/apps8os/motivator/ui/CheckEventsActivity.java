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
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.LinePageIndicator;

public class CheckEventsActivity extends Activity {

	private EventDataHandler mEventDataHandler;
	private ViewPager mViewPager;
	private EventsPagerAdapter mEventsPagerAdapter;
	private ArrayList<MotivatorEvent> mEvents;
	private Button mCompleteButton;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_questions);
	    
	    mEvents = getIntent().getExtras().getParcelableArrayList(MotivatorEvent.YESTERDAYS_EVENTS);
	    mEventDataHandler = new EventDataHandler(this);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_blue));
		
	    mViewPager = (ViewPager) findViewById(R.id.question_activity_pager);
	    mEventsPagerAdapter = new EventsPagerAdapter(
	    		getFragmentManager(), mEvents.size());
	    mViewPager.setAdapter(mEventsPagerAdapter);
	    mViewPager.setOffscreenPageLimit(10);
	    setButtons();
	}
	
	/**
	 * Sets up the listeners for the buttons.
	 */
	private void setButtons() {
		final Button nextButton = (Button) findViewById(R.id.questions_next_button);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
			}
		});
		
		final Button previousButton = (Button) findViewById(R.id.questions_previous_button);
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
			}
		});
		if (mEventsPagerAdapter.getCount() == 1) {
			nextButton.setEnabled(false);
			nextButton.setVisibility(View.GONE);
			previousButton.setVisibility(View.GONE);
		}
		mCompleteButton = (Button) findViewById(R.id.questions_complete_button);
		mCompleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int count = mEventsPagerAdapter.getCount();
				for (int i = 0; i < count; i++) {
					int[] answers = mEventsPagerAdapter.getFragment(i).getAnswers();
					mEventDataHandler.insertCheckedEvent(mEvents.get(i).getId(), answers[0] - 1, answers[1], answers[2], answers[3], mEvents.get(i).getName());
				}
				View toastLayout = (View) getLayoutInflater().inflate(R.layout.element_mood_toast, (ViewGroup) findViewById(R.id.mood_toast_layout));
				TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
				toastText.setText(getString(R.string.plan_checked));
				toastText.setTextColor(Color.WHITE);
				
				
				Toast questionnaireDone = new Toast(getApplicationContext());
				questionnaireDone.setDuration(Toast.LENGTH_SHORT);
				questionnaireDone.setView(toastLayout);
				questionnaireDone.show();
				
				finish();
			}
		});
		// Disable these buttons at start.
		mCompleteButton.setEnabled(true);
		previousButton.setEnabled(false);
		
		LinePageIndicator titleIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setViewPager(mViewPager);
		
		// Set up a page change listener to enable and disable buttons.
		titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == mEventsPagerAdapter.getCount() - 1) {
					nextButton.setEnabled(false);
				} else {
					nextButton.setEnabled(true);
				}
				if (arg0 == 0) {
					previousButton.setEnabled(false);
				} else {
					previousButton.setEnabled(true);
				}
			}
		});
	}
	
	
	private class EventsPagerAdapter extends FragmentPagerAdapter {
		
		private SparseArray<EventToCheckFragment> fragments = new SparseArray<EventToCheckFragment>();
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
		public Object instantiateItem(ViewGroup container, int position) {
			EventToCheckFragment fragment = (EventToCheckFragment) super.instantiateItem(container, position);
			fragments.put(position, fragment);
			return fragment;
		}
		
		@Override
	    public void destroyItem(ViewGroup container, int position, Object object) {
	        fragments.remove(position);
	        super.destroyItem(container, position, object);
		}
		
		@Override
		public int getCount() {
			return mSize;
		}
		
		/**
		 * Get the fragment in given position.
		 * @param position
		 * @return
		 */
		public EventToCheckFragment getFragment(int position) {
			return fragments.get(position);
		}
	}

}
