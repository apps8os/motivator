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
import org.apps8os.motivator.io.MoodDataHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * Represents the mood question activity.
 * @author Toni Järvinen
 *
 */

public class MoodQuestionActivity extends Activity {
	
	private MoodDataHandler mDataHandler;					// Database access object
	private ViewPager mCardsViewPagerEnergy;				// Upper half of the carousel
	private ViewPager mCardsViewPagerMood;				// Lower half of the carousel
	private TextView mEnergyLevelText;
	private TextView mMoodLevelText;
	
	private int[] mImages1 = {
            R.drawable.temp_emoticon_top, R.drawable.temp_emoticon_top,
            R.drawable.temp_emoticon_top 
            };
	
	 private int[] mImages2 = {
	    		R.drawable.temp_emoticon_bot, R.drawable.temp_emoticon_bot,
	            R.drawable.temp_emoticon_bot
	            };
	 
	 private int[] mTitles1 = {
	    		R.string.energy_level1, R.string.energy_level2, R.string.energy_level3
	    		};
	    
	    private int[] mTitles2 = {
	    		R.string.mood_level1, R.string.mood_level2, R.string.mood_level3
	    		};
	
	private static final int MARGIN_DP = 100;
	private static final int DEFAULT_MOOD_SELECTION = 1;
	public static final String GOODMOOD = "good_mood";
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mood_question);
		mDataHandler = new MoodDataHandler(this);
		mDataHandler.open();
		
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
        
        // Get the textfield for energy level
        mMoodLevelText = (TextView) findViewById(R.id.mood_question_moodlevel_textview);
        mMoodLevelText.setText(mCardsViewPagerMood.getAdapter().getPageTitle(DEFAULT_MOOD_SELECTION));
        
        // Set an OnPageChangeListener to the ViewPager, change the text when a page is selected
        mCardsViewPagerMood.setOnPageChangeListener(new ViewPageChangeListener(mCardsViewPagerMood, mMoodLevelText));
        
        // Set up the next button and save the mood
        Button nextButton = (Button) findViewById(R.id.mood_question_next_button);
        nextButton.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		saveMood(v);
        	}
        });
	}
	
	@Override
	public void onDestroy() {
		mDataHandler.close();
		super.onDestroy();
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
		int mood = mCardsViewPagerMood.getCurrentItem();
		mDataHandler.insertMood(mCardsViewPagerEnergy.getCurrentItem() + 1, mood);
		Intent intent = new Intent(this, QuestionnaireActivity.class);
		if (mood < 2) {
			intent.putExtra(GOODMOOD, false);
		} else {
			intent.putExtra(GOODMOOD, true);
		}
		startActivity(intent);
		finish();
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
