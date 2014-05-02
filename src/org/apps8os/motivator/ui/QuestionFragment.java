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
import org.apps8os.motivator.data.Question;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * Represents a question fragment.
 * @author Toni Järvinen
 *
 */
public class QuestionFragment extends Fragment {
	
	private Question mQuestion;
	private RadioGroup mAnswerGroupView;
	private TextView mQuestionTextView;
	private TextView mRequiredTextView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mQuestion = getArguments().getParcelable(Question.QUESTION);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_question, viewGroup, false);
		mAnswerGroupView = (RadioGroup) rootView.findViewById(R.id.questionnaire_answers_group);
		mQuestionTextView = (TextView) rootView.findViewById(R.id.questionnaire_question);
		mRequiredTextView = (TextView) rootView.findViewById(R.id.questionnaire_required);
		
		if (mQuestion.isRequired()) {
			mRequiredTextView.setText("*");
			mRequiredTextView.setTextColor(getResources().getColor(R.color.red));
		}
		
		mQuestionTextView.setText(mQuestion.getQuestion());
		// Insert possible answers to the RadioGroup by looping through parsedAnswers[]
		for (int i = 0; i < mQuestion.getAnswerCount(); i++) {
			RadioButton radioButton = (RadioButton) inflater.inflate(R.layout.element_questionnaire_radiobutton, mAnswerGroupView, false);
			radioButton.setId(i);
			radioButton.setText(mQuestion.getAnswer(i));
			mAnswerGroupView.addView(radioButton);
		}
		
		mAnswerGroupView.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				((QuestionnaireActivityInterface) getActivity()).setChecked(mQuestion.getId());
			}
			
		});
		return rootView;
	}
	
	/**
	 * Get the selected answer in this fragment.
	 * @return
	 */
	public int getAnswer() {
		return mAnswerGroupView.getCheckedRadioButtonId();
	}
	
	/**
	 * Get the question id of the question represented by this fragment.
	 * @return
	 */
	public int getQuestionId() {
		return mQuestion.getId();
	}

}
