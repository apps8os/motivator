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
import org.apps8os.motivator.io.MotivatorDatabase;

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
 * @author Toni JŠrvinen
 *
 */

public class MoodQuestionActivity extends Activity {
	
	private MotivatorDatabase mDatabase;					// Database access object
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
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mood_question);
		mDatabase = MotivatorDatabase.getInstance(this);
		
		mCardsViewPagerEnergy = (ViewPager) findViewById(R.id.mood_question_viewpager_cards);
        mCardsViewPagerEnergy.setAdapter(new ImagesPagerAdapter(mImages1, mTitles1, this));
        
        // Convert the margin from dp to px
        final float scale = getResources().getDisplayMetrics().density;
        int margin = (int) (MARGIN_DP * scale + 0.5f);
        // Set the page margin to negative to show pages next to the selected on the screen
        mCardsViewPagerEnergy.setPageMargin(-margin);
        mCardsViewPagerEnergy.setOffscreenPageLimit(3);
        // Set default item
        mCardsViewPagerEnergy.setCurrentItem(DEFAULT_MOOD_SELECTION);
        
        mEnergyLevelText = (TextView) findViewById(R.id.mood_question_energylevel_textview);
        mEnergyLevelText.setText(mCardsViewPagerEnergy.getAdapter().getPageTitle(DEFAULT_MOOD_SELECTION));
        
        // Set an OnPageChangeListener to the ViewPager, change the text when a page is selected
        mCardsViewPagerEnergy.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {	
			}
			@Override
			public void onPageSelected(int arg0) {
				mEnergyLevelText.setText(mCardsViewPagerEnergy.getAdapter().getPageTitle(arg0));
			}
        });
       
        
        mCardsViewPagerMood = (ViewPager) findViewById(R.id.mood_question_viewpager_cards2);
        mCardsViewPagerMood.setAdapter(new ImagesPagerAdapter(mImages2, mTitles2, this));
        
        // Set the page margin to negative to show pages next to the selected on the screen
        mCardsViewPagerMood.setPageMargin(-margin);
        mCardsViewPagerMood.setOffscreenPageLimit(3);
        // Set default item
        mCardsViewPagerMood.setCurrentItem(DEFAULT_MOOD_SELECTION);
        
        // Get the textfield for energy level
        mMoodLevelText = (TextView) findViewById(R.id.mood_question_moodlevel_textview);
        mMoodLevelText.setText(mCardsViewPagerMood.getAdapter().getPageTitle(DEFAULT_MOOD_SELECTION));
        
        // Set an OnPageChangeListener to the ViewPager, change the text when a page is selected
        mCardsViewPagerMood.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageScrollStateChanged(int arg0) {	
			}
			@Override
			public void onPageSelected(int arg0) {
				mMoodLevelText.setText(mCardsViewPagerMood.getAdapter().getPageTitle(arg0));
			}
        });
        
        // Set up the next button and save the mood
        Button nextButton = (Button) findViewById(R.id.mood_question_next_button);
        nextButton.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v) {
        		saveMood(v);
        	}
        });
        
        
	}

	// Saves the mood to the database.
	public void saveMood(View v) {
		mDatabase.open();
		mDatabase.insertMood(mCardsViewPagerEnergy.getCurrentItem(), mCardsViewPagerMood.getCurrentItem());
		mDatabase.close();
		Intent intent = new Intent(this, QuestionnaireActivity.class);
		startActivity(intent);
	}
	

}
