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
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
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

/**
 * Activity for checking the events on the following day.
 * @author Toni Järvinen
 *
 */
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
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_orange));
		
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
		
		final Context context = this;
		mCompleteButton = (Button) findViewById(R.id.questions_complete_button);
		mCompleteButton.setEnabled(false);
		if (mEventsPagerAdapter.getCount() == 1) {
			nextButton.setEnabled(false);
			nextButton.setVisibility(View.GONE);
			previousButton.setVisibility(View.GONE);
			mCompleteButton.setEnabled(true);
		}
		mCompleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final int count = mEventsPagerAdapter.getCount();
				int checkedDrinksAmount = 0;
				final DayDataHandler dayHandler = new DayDataHandler(context);
				final int clickedDrinks = dayHandler.getClickedDrinksForDay(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
				for (int i = 0; i < count; i++) {
					int[] answers = mEventsPagerAdapter.getFragment(i).getAnswers();
					checkedDrinksAmount += answers[0];
					mEventDataHandler.insertCheckedEvent(mEvents.get(i).getId(), answers[0] + 1, answers[1], answers[2], answers[3], mEvents.get(i).getName());
				}
				final int checkedDrinks = checkedDrinksAmount;
				
				// If the user is confirming lower amount of drinks than he clicked on the previous day, confirm the amount with a dialog
				if (checkedDrinksAmount < clickedDrinks) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Suunnitelmissasi on vähemmän juomia kuin klikkasit eilen.")
						.setMessage("Eilen klikatut: " + clickedDrinks + " \r\n" + "Tarkistetut suunnitelmat: " + checkedDrinksAmount + " \r\n \r\n" + "Paljon joit eilenä yhteensä?")
						.setPositiveButton("" + checkedDrinksAmount, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dayHandler.insertDailyDrinkAmount(checkedDrinks, System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
							eventsChecked();
						}
					}).setNegativeButton("" + clickedDrinks, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dayHandler.insertDailyDrinkAmount(clickedDrinks, System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
							eventsChecked();
						}
					});
					Dialog dialog = builder.create();
					dialog.show();
				} else {
					dayHandler.insertDailyDrinkAmount(checkedDrinks, System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS));
					eventsChecked();
				}
				
			}
		});
		// Disable these buttons at start.
		previousButton.setEnabled(false);
		
		LinePageIndicator titleIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setSelectedColor(getResources().getColor(R.color.actionbar_orange));
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
					mCompleteButton.setEnabled(true);
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
	
	/**
	 * Displaying toast after checking events
	 */
	private void eventsChecked() {
		View toastLayout = (View) getLayoutInflater().inflate(R.layout.element_mood_toast, (ViewGroup) findViewById(R.id.mood_toast_layout));
		TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
		toastText.setText(getString(R.string.plan_checked));
		toastText.setTextColor(Color.WHITE);
		
		
		Toast planChecked = new Toast(getApplicationContext());
		planChecked.setDuration(Toast.LENGTH_SHORT);
		planChecked.setView(toastLayout);
		planChecked.show();
		
		finish();
	}
	
	
	/**
	 * Inner class for the fragment adapter.
	 * @author Toni Järvinen
	 *
	 */
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
