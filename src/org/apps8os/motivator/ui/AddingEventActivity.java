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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Represents the questionnaire activity
 * @author Toni JŠrvinen
 *
 */

public class AddingEventActivity extends Activity {
	
	private MotivatorDatabase mDatabase;
	private int mQuestionId;
	private TextView mQuestion;
	private RadioGroup mAnswerGroup;
	private TextView mPromptMessage;
	private int mNumberOfQuestions = 2;
	private LayoutInflater mInflater;
	
	private int mAnswerId;
	private static final String ANSWER_ID_INCREMENT_PREFS = "incrementing_prefs";
	
	private static final String ANSWER_ID = "incrementing_id";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionnaire);
		
		mInflater = getLayoutInflater();
		mDatabase = MotivatorDatabase.getInstance(this);
		mAnswerGroup = (RadioGroup) findViewById(R.id.questionnaire_answers_group);
		mQuestion = (TextView) findViewById(R.id.questionnaire_question);
		mPromptMessage = (TextView) findViewById(R.id.questionnaire_prompt_message);
		
		Button nextButton = (Button) findViewById(R.id.questionnaire_next_button);
		nextButton.setOnClickListener(new NextButtonOnClickListener(this));
		
		mAnswerId = incrementAnswersId();
		mQuestionId = 0;
		incrementQuestion(true);
	}
	
	private int incrementAnswersId() {
		// Use SharedPreferences to store the answers id so that it can be incremented even if the app is killed
		SharedPreferences answerIdIncrement = getSharedPreferences(ANSWER_ID_INCREMENT_PREFS, 0);
		int answerId = answerIdIncrement.getInt(ANSWER_ID, 1);
		SharedPreferences.Editor editor = answerIdIncrement.edit();
		editor.putInt(ANSWER_ID, answerId + 1);
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
		
		// Fetching the question from resources
		Resources res = getResources();
		String[] question = res.getStringArray(res.getIdentifier("adding_event" + mQuestionId, "array" , this.getPackageName()));
		mQuestion.setText(question[0]);
		
		// Clear previous views and selections
		mAnswerGroup.removeAllViews();
		mAnswerGroup.clearCheck();
		
		// Insert possible answers to the RadioGroup by looping through parsedAnswers[]
		for (int i = 1; i < question.length; i++) {
			RadioButton radioButton = (RadioButton) mInflater.inflate(R.layout.element_questionnaire_radiobutton, mAnswerGroup, false);
			radioButton.setId(i);
			radioButton.setText(question[i]);
			mAnswerGroup.addView(radioButton);
		}
		
		// Remove the prompt text
		mPromptMessage.setText("");
	}
	
	/**
	 * Enables going back on questions with the android back button. Default action goes to previous activity, this overrides this
	 * by going back on questions if the user has already answered to a question. If the user has not answered, do default action.
	 */
	@Override
	public void onBackPressed() {
		if (mQuestionId > 1) {
			incrementQuestion(false);
			mDatabase.open();
			mDatabase.deleteLastAnswer(MotivatorDatabase.TABLE_NAME_EVENT_ANSWERS);
			mDatabase.close();
		} else {
			super.onBackPressed();
		}
	}
	
	/**
	 * Inner class for handling next button clicks.
	 * @author Toni JŠrvinen
	 *
	 */
	private class NextButtonOnClickListener implements OnClickListener {
		
		private Context mContext;
		
		public NextButtonOnClickListener(Context mContext) {
			super();
			this.mContext = mContext;
		}

		@Override
		public void onClick(View v) {
			int answer = mAnswerGroup.getCheckedRadioButtonId();
			
			// Check if the user has selected an answer
			if (mAnswerGroup.getCheckedRadioButtonId() != -1) {
				mDatabase.open();
				mDatabase.insertAnswer(answer, mQuestionId, mAnswerId, MotivatorDatabase.TABLE_NAME_EVENT_ANSWERS);
				mDatabase.close();
				// Determine if we have already asked enough questions
				if (mNumberOfQuestions > 0) {
					incrementQuestion(true);
				} else {								
					// Questionnaire is done
					finish();
				}
			} else {
				// Message the user that he has to select something
				mPromptMessage.setText(R.string.prompt_selection);
			}
			
		}
		
	}
	

}
