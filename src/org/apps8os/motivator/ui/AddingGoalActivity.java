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
import org.apps8os.motivator.data.GoalDataHandler;
import org.apps8os.motivator.data.MotivatorDatabaseHelper;
import org.apps8os.motivator.data.Question;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Represents the adding goal activity
 * @author Toni Järvinen
 *
 */

public class AddingGoalActivity extends Activity {
	
	private GoalDataHandler mDataHandler;
	private int mQuestionId;
	private TextView mQuestionTextView;
	private RadioGroup mAnswerGroupView;
	private TextView mPromptMessageTextView;
	private int mNumberOfQuestions = 2;
	private LayoutInflater mInflater;
	
	private int mAnswerId;
	private ProgressBar mProgressBar;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionnaire);
		
		mInflater = getLayoutInflater();
		mDataHandler = new GoalDataHandler(this);
		
		mAnswerGroupView = (RadioGroup) findViewById(R.id.questionnaire_answers_group);
		mQuestionTextView = (TextView) findViewById(R.id.questionnaire_question);
		mPromptMessageTextView = (TextView) findViewById(R.id.questionnaire_prompt_message);
		
		mProgressBar = (ProgressBar) findViewById(R.id.questionnaire_progress);
		
		Button nextButton = (Button) findViewById(R.id.questionnaire_next_button);
		nextButton.setOnClickListener(new NextButtonOnClickListener());
		
		mAnswerId = incrementAnswersId();
		mQuestionId = 0;
		mProgressBar.setMax(mNumberOfQuestions);
		mProgressBar.setProgress(1);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle(R.string.add_goal);
		
		incrementQuestion(true);
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
	
	/**
	 * Increments question forward or backwards depending on the boolean.
	 * @param forward
	 */
	private void incrementQuestion(boolean forward) {
		if (forward) {
			mQuestionId += 1;
			mNumberOfQuestions -= 1;
		} else {
			mQuestionId -= 1;
			mNumberOfQuestions += 1;
		}
		
		Question question = mDataHandler.getQuestion(mQuestionId);
		mQuestionTextView.setText(question.getQuestion());
		
		// Clear previous views and selections
		mAnswerGroupView.removeAllViews();
		mAnswerGroupView.clearCheck();
		
		// Insert possible answers to the RadioGroup by looping through parsedAnswers[]
		for (int i = 0; i < question.getAnswerCount(); i++) {
			RadioButton radioButton = (RadioButton) mInflater.inflate(R.layout.element_questionnaire_radiobutton, mAnswerGroupView, false);
			radioButton.setId(i);
			radioButton.setText(question.getAnswer(i));
			mAnswerGroupView.addView(radioButton);
		}
		
		// Remove the prompt text
		mPromptMessageTextView.setText("");
	}
	
	/**
	 * Enables going back on questions with the android back button. Default action goes to previous activity, this overrides this
	 * by going back on questions if the user has already answered to a question. If the user has not answered, do default action.
	 */
	@Override
	public void onBackPressed() {
		if (mQuestionId > 1) {
			incrementQuestion(false);
			mProgressBar.incrementProgressBy(-1);
			mDataHandler.deleteLastRow();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        mDataHandler.deleteRowsWithAnsweringId(mAnswerId);
	        return super.onOptionsItemSelected(item);
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Inner class for handling next button clicks. Inserts the answer and gets the next question if there is one.
	 * @author Toni Järvinen
	 *
	 */
	private class NextButtonOnClickListener implements OnClickListener {
		
		public NextButtonOnClickListener() {
			super();
		}

		@Override
		public void onClick(View v) {
			
			int answer = mAnswerGroupView.getCheckedRadioButtonId();
			
			// Check if the user has selected an answer
			if (mAnswerGroupView.getCheckedRadioButtonId() != -1) {

				mDataHandler.insertAnswer(answer, mQuestionId, mAnswerId, 010);

				
				// Determine if we have already asked enough questions
				if (mNumberOfQuestions > 0) {
					incrementQuestion(true);
					mProgressBar.incrementProgressBy(1);
				} else {								
					// Questionnaire is done
					
					finish();
				}
			} else {
				// Message the user that he has to select something
				mPromptMessageTextView.setText(R.string.prompt_selection);
			}
			
		}
		
	}
	

}
