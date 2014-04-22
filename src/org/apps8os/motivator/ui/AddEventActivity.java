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
import org.apps8os.motivator.data.MoodDataHandler;
import org.apps8os.motivator.data.MotivatorDatabaseHelper;

import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class AddEventActivity extends Activity {
	
	public static final String QUESTION = "question";

	private ViewPager mViewPager;
	private EventDataHandler mDataHandler;
	private int mNumberOfQuestions;
	private int mQuestionId;
	private int mAnswersId;

	private QuestionsPagerAdapter mQuestionsPagerAdapter;

	private LinePageIndicator titleIndicator;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_questions);
	    
	    mDataHandler = new EventDataHandler(this);
	    mNumberOfQuestions = mDataHandler.getAmountOfQuestions();
		mQuestionId = mDataHandler.getFirstQuestionId() - 1;
		
		getActionBar().setTitle(getString(R.string.add_event));
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_blue));
		
	    mViewPager = (ViewPager) findViewById(R.id.question_activity_pager);
	    mQuestionsPagerAdapter = new QuestionsPagerAdapter(
	    		getFragmentManager(), mNumberOfQuestions);
	    mViewPager.setAdapter(mQuestionsPagerAdapter);
	    mViewPager.setOffscreenPageLimit(10);
	    
	    mAnswersId = incrementAnswersId();
	    
	    titleIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setViewPager(mViewPager);
		
		setButtons();
	    
	}
	
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
		
		final Button completeButton = (Button) findViewById(R.id.questions_complete_button);
		completeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				for (int i = 0; i < mNumberOfQuestions; i++) {
					int answer = mQuestionsPagerAdapter.getFragment(i).getAnswer();
					if (answer > -1) {
						int questionId = mQuestionsPagerAdapter.getFragment(i).getQuestionId();
						mDataHandler.insertAnswer(answer, questionId, mAnswersId);
					}
				}
				finish();
			}
		});
		completeButton.setEnabled(false);
		
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
				
				if (arg0 == 2 && mQuestionsPagerAdapter.getFragment(0).getAnswer() != -1 && mQuestionsPagerAdapter.getFragment(1).getAnswer() != -1) {
					completeButton.setEnabled(true);
				}
			}
		});
	}
	
	/**
	 * Incrementing the running id for an answering instance.
	 * @return
	 */
	private int incrementAnswersId() {
		// Use SharedPreferences to store the answers id so that it can be incremented even if the app is killed
		SharedPreferences answerIdIncrement = getSharedPreferences(MotivatorDatabaseHelper.ANSWER_ID_INCREMENT_PREFS, 0);
		int answerId = answerIdIncrement.getInt(MotivatorDatabaseHelper.ANSWER_ID, 1);
		SharedPreferences.Editor editor = answerIdIncrement.edit();
		editor.putInt(MotivatorDatabaseHelper.ANSWER_ID, answerId + 1);
		editor.commit();
		return answerId;
	}
	
	
	
	private class QuestionsPagerAdapter extends FragmentPagerAdapter {
		
		private SparseArray<QuestionFragment> fragments = new SparseArray<QuestionFragment>();
		private int mQuestions;
		
		public QuestionsPagerAdapter(FragmentManager fm, int numberOfQuestions) {
			super(fm);
			mQuestions = numberOfQuestions;
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment questionFragment = new QuestionFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable(QUESTION, mDataHandler.getQuestion(1000 + arg0));
			questionFragment.setArguments(bundle);
			return questionFragment;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			QuestionFragment fragment = (QuestionFragment) super.instantiateItem(container, position);
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
			return mQuestions;
		}
		
		public QuestionFragment getFragment(int position) {
			return fragments.get(position);
		}
	}

}
