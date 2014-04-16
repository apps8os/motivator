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
import org.apps8os.motivator.data.MoodDataHandler;

import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class QuestionsActivity extends Activity {
	
	public static final String QUESTION = "question";

	private ViewPager mViewPager;
	private MoodDataHandler mDataHandler;
	private int mNumberOfQuestions;
	private int mQuestionId;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_questions);
	    
	    mDataHandler = new MoodDataHandler(this);
	    mNumberOfQuestions = mDataHandler.getAmountOfQuestions();
		mQuestionId = mDataHandler.getFirstQuestionId() - 1;
		
	    mViewPager = (ViewPager) findViewById(R.id.question_activity_pager);
	    mViewPager.setAdapter(new QuestionsPagerAdapter(
	    		getFragmentManager(), mNumberOfQuestions));
	    
	    LinePageIndicator titleIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setViewPager(mViewPager);
		
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
		
		previousButton.setEnabled(false);
		
		titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == mNumberOfQuestions - 1) {
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
	
	
	
	private class QuestionsPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
		private final int[] ICONS = new int[] {
			R.drawable.questionnaire_icon_1,
			R.drawable.questionnaire_icon_2,
			R.drawable.questionnaire_icon_3
		};
		private int mQuestions;
		
		public QuestionsPagerAdapter(FragmentManager fm, int numberOfQuestions) {
			super(fm);
			mQuestions = numberOfQuestions;
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment questionFragment = new QuestionFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable(QUESTION, mDataHandler.getQuestion(2000 + arg0));
			questionFragment.setArguments(bundle);
			return questionFragment;
		}

		@Override
		public int getCount() {
			return mQuestions;
		}

		@Override
		public int getIconResId(int index) {
			return ICONS[index];
		}
	}

}
