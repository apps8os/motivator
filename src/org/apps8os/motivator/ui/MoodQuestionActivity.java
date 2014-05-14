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

import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.services.NotificationService;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Represents the mood question activity.
 * @author Toni Järvinen
 *
 */

public class MoodQuestionActivity extends Activity {
	
	private DayDataHandler mDataHandler;					// Database access object
	private ViewPager mCardsViewPagerEnergy;				// Upper half of the carousel
	private ViewPager mCardsViewPagerMood;				// Lower half of the carousel
	private TextView mEnergyLevelText;
	private TextView mMoodLevelText;
	
	private int[] mImages1 = {
            R.drawable.energy1, R.drawable.energy2,
            R.drawable.energy3, R.drawable.energy4,
            R.drawable.energy5
            };
	
	private int[] mImages2 = {
	    		R.drawable.mood1, R.drawable.mood2,
	            R.drawable.mood3, R.drawable.mood4,
	            R.drawable.mood5
	            };
	 
	private int[] mTitles1 = {
	    		R.string.energy_level1, R.string.energy_level2, 
	    		R.string.energy_level3, R.string.energy_level4,
	    		R.string.energy_level5
	    		};
	    
	private int[] mTitles2 = {
	    		R.string.mood_level1, R.string.mood_level2,
	    		R.string.mood_level3, R.string.mood_level4,
	    		R.string.mood_level5
	    		};
	
	private static final int MARGIN_DP = 80;
	private static final int DEFAULT_MOOD_SELECTION = 2;
	public static final String GOODMOOD = "good_mood";
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mood_question);
		mDataHandler = new DayDataHandler(this);

		mCardsViewPagerEnergy = (ViewPager) findViewById(R.id.mood_question_viewpager_cards);
        mCardsViewPagerEnergy.setAdapter(new ImagesPagerAdapter(mImages1, mTitles1, this));
        
        setViewPager(mCardsViewPagerEnergy);
        
        mEnergyLevelText = (TextView) findViewById(R.id.mood_question_energylevel_textview);
        mEnergyLevelText.setText(mCardsViewPagerEnergy.getAdapter().getPageTitle(DEFAULT_MOOD_SELECTION));
        
        // Set an OnPageChangeListener to the ViewPager, change the text when a page is selected
        mCardsViewPagerEnergy.setOnPageChangeListener(new ViewPageChangeListener(mCardsViewPagerEnergy, mEnergyLevelText));
       
        mCardsViewPagerMood = (ViewPager) findViewById(R.id.mood_question_viewpager_cards2);
        mCardsViewPagerMood.setAdapter(new ImagesPagerAdapter(mImages2, mTitles2, this));
        
        setViewPager(mCardsViewPagerMood);
        
        // Get the text field for energy level
        mMoodLevelText = (TextView) findViewById(R.id.mood_question_moodlevel_textview);
        mMoodLevelText.setText(mCardsViewPagerMood.getAdapter().getPageTitle(DEFAULT_MOOD_SELECTION));
        
        // Set an OnPageChangeListener to the ViewPager, change the text when a page is selected
        mCardsViewPagerMood.setOnPageChangeListener(new ViewPageChangeListener(mCardsViewPagerMood, mMoodLevelText));
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout buttons = (LinearLayout) findViewById(R.id.mood_question_buttons);
        final Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean(EventDataHandler.EVENTS_TO_CHECK, false)) {
            Button nextButton = (Button) inflater.inflate(R.layout.element_ok_button, buttons, false);
            nextButton.setText(getString(R.string.next));
            nextButton.setOnClickListener(new OnClickListener() {
    			@Override
    			public void onClick(View v) {
    				saveMood(v);
    				Intent intent = new Intent(MoodQuestionActivity.this, CheckEventsActivity.class);
    				intent.putExtra(MotivatorEvent.YESTERDAYS_EVENTS, extras.getParcelableArrayList(MotivatorEvent.YESTERDAYS_EVENTS));
    				startActivity(intent);
    				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    		        notificationManager.cancel(NotificationService.NOTIFICATION_ID_MOOD);
    				finish();
    			}
            	
            });
            buttons.addView(nextButton);
        } else {
	        
	        Button okButton = (Button) inflater.inflate(R.layout.element_ok_button, buttons, false);
	        okButton.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View v) {
					saveMood(v);
					
					String toastMsg;
					if ( mCardsViewPagerMood.getCurrentItem() > 1 ) {
						toastMsg = getString(R.string.questionnaire_done_toast_good_mood);
					} else {
						toastMsg = getString(R.string.questionnaire_done_toast_bad_mood);
					}
					LayoutInflater inflater = getLayoutInflater();
					View toastLayout = (View) inflater.inflate(R.layout.element_mood_toast, (ViewGroup) findViewById(R.id.mood_toast_layout));
					TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
					toastText.setText(toastMsg);
					toastText.setTextColor(Color.WHITE);
					
					Toast questionnaireDone = new Toast(getApplicationContext());
					questionnaireDone.setDuration(Toast.LENGTH_SHORT);
					questionnaireDone.setView(toastLayout);
					questionnaireDone.show();
					
					NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			        notificationManager.cancel(NotificationService.NOTIFICATION_ID_MOOD);
			        
					finish();
				}
	        	
	        });
	        buttons.addView(okButton);
        }
	}
	
	/**
	 * Set up for the viewpager.
	 * @param viewPager
	 */
	private void setViewPager(ViewPager viewPager) {
		
		// Convert the margin from dp to px
        final float scale = getResources().getDisplayMetrics().density;
        int margin = (int) (MARGIN_DP * scale + 0.5f);
        // Set the page margin to negative to show pages next to the selected on the screen
        viewPager.setPageMargin(-margin);
        viewPager.setOffscreenPageLimit(3);
        // Set default item
        viewPager.setCurrentItem(DEFAULT_MOOD_SELECTION);
	}

	// Saves the mood to the database.
	public void saveMood(View v) {
		int mood = mCardsViewPagerMood.getCurrentItem() + 1;
		EditText commentText = (EditText) findViewById(R.id.mood_comment_edit_text);
		if (commentText.getText().length() != 0) {
			mDataHandler.insertMood(mCardsViewPagerEnergy.getCurrentItem() + 1, mood, commentText.getText().toString());
		} else {
			mDataHandler.insertMood(mCardsViewPagerEnergy.getCurrentItem() + 1, mood, DayDataHandler.NO_COMMENT);
		}
	}
	
	/**
	 * Inner class for changing text when the user scrolls the viewpager.
	 * @author Toni Järvinen
	 *
	 */
	private class ViewPageChangeListener implements OnPageChangeListener {
		
		private ViewPager mViewPager;
		private TextView mTextView;
		
		public ViewPageChangeListener(ViewPager viewPager, TextView textView) {
			mViewPager = viewPager;
			mTextView = textView;
		}
		
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
			mTextView.setText(mViewPager.getAdapter().getPageTitle(arg0));
		}
		
	}
	
}
