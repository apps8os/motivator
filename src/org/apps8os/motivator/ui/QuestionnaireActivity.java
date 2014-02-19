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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Represents a questionnaire activity
 * @author Toni JŠrvinen
 *
 */

public class QuestionnaireActivity extends Activity {
	
	private MotivatorDatabase mDatabase;
	private int mQuestionId;
	private TextView mQuestion;
	private RadioGroup mAnswerGroup;
	private Button mNextButton;
	private TextView mPromptMessage;
	private int mNumberOfQuestions = 2;
	private LayoutInflater mInflater;
	
	private int mAnswerId;
	private static final String ANSWER_ID_INCREMENT_PREFS = "incrementing_prefs";
	
	private static final String ANSWER_ID = "incrementing_id";
	
	private int incrementAnswersId() {
		// Use SharedPreferences to store the answers id so that it can be incremented even if the app is killed
		SharedPreferences answerIdIncrement = getSharedPreferences(ANSWER_ID_INCREMENT_PREFS, 0);
		int answerId = answerIdIncrement.getInt(ANSWER_ID, 1);
		SharedPreferences.Editor editor = answerIdIncrement.edit();
		editor.putInt(ANSWER_ID, answerId + 1);
		editor.commit();
		return answerId;
	}
	
	private void incrementQuestion(boolean forward) {
		if (forward) {
			mQuestionId += 1;
			mNumberOfQuestions -= 1;
		} else {
			mQuestionId -= 1;
			mNumberOfQuestions += 1;
		}
		
		// Fetching the question from the database
		mDatabase.open();
		mQuestion.setText(mDatabase.getQuestion(mQuestionId));
		String answers = mDatabase.getAnswers(mQuestionId);
		mDatabase.close();
		
		// Clear previous views and selections
		mAnswerGroup.removeAllViews();
		mAnswerGroup.clearCheck();
		
		String parsedAnswers[] = answers.split("; ");
		
		// Insert possible answers to the RadioGroup by looping through parsedAnswers[]
		for (int i = 0; i < parsedAnswers.length; i++) {
			RadioButton radioButton = (RadioButton) mInflater.inflate(R.layout.element_questionnaire_radiobutton, mAnswerGroup, false);
			radioButton.setId(i);
			radioButton.setText(parsedAnswers[i]);
			mAnswerGroup.addView(radioButton);
		}
		
		// Remove the prompt text
		mPromptMessage.setText("");
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_questionnaire);
		
		mInflater = getLayoutInflater();
		mDatabase = MotivatorDatabase.getInstance(this);
		mAnswerGroup = (RadioGroup) findViewById(R.id.questionnaire_answers_group);
		mQuestion = (TextView) findViewById(R.id.questionnaire_question);
		mNextButton = (Button) findViewById(R.id.questionnaire_next_button);
		mPromptMessage = (TextView) findViewById(R.id.questionnaire_prompt_message);
		
		mNextButton.setOnClickListener(new NextButtonOnClickListener(this));
		
		mAnswerId = incrementAnswersId();
		mQuestionId = 0;
		incrementQuestion(true);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && mQuestionId > 1) {
			incrementQuestion(false);
			mDatabase.open();
			mDatabase.deleteLastAnswer();
			return true;
		}
		return super.onKeyDown(keyCode, event);
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
				mDatabase.insertAnswer(answer, mQuestionId, mAnswerId);
				
				// Determine if we have already asked enough questions
				if (mNumberOfQuestions > 0) {
					incrementQuestion(true);
				} else {								
					// Questionnaire is done
					Intent intent = new Intent(mContext, MainActivity.class);
					startActivity(intent);
					finish();
				}
			} else {
				// Message the user that he has to select something
				mPromptMessage.setText(R.string.prompt_selection);
			}
			
		}
		
	}
	

}
