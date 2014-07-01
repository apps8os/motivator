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

import java.util.Calendar;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.GoalDataHandler;
import org.apps8os.motivator.data.Question;
import org.apps8os.motivator.utils.UtilityMethods;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

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
	private int mXAmount = 0;
	private String mPreviousX = "X";
	private Calendar mCalendar;
	private int mInput = 4;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mQuestion = getArguments().getParcelable(Question.QUESTION);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		final LayoutInflater layoutInflater = inflater;
		LinearLayout rootView = (LinearLayout) layoutInflater.inflate(R.layout.fragment_question, viewGroup, false);
		mAnswerGroupView = (RadioGroup) rootView.findViewById(R.id.questionnaire_answers_group);
		mQuestionTextView = (TextView) rootView.findViewById(R.id.questionnaire_question);
		mRequiredTextView = (TextView) rootView.findViewById(R.id.questionnaire_required);
		mCalendar = Calendar.getInstance();
		
		if (mQuestion.isRequired()) {
			mRequiredTextView.setText("*");
			mRequiredTextView.setTextColor(getResources().getColor(R.color.red));
		}
		
		
		mQuestionTextView.setText(mQuestion.getQuestion());
		// Insert possible answers to the RadioGroup by looping through parsedAnswers[]
		for (int i = 0; i < mQuestion.getAnswerCount(); i++) {
			RadioButton radioButton = (RadioButton) layoutInflater.inflate(R.layout.element_questionnaire_radiobutton, mAnswerGroupView, false);
			radioButton.setId(i);
			radioButton.setText(mQuestion.getAnswer(i + 1));
			mAnswerGroupView.addView(radioButton);
		}
		
		mAnswerGroupView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			private int mPreviousId = -1;

			@Override
			public void onCheckedChanged(RadioGroup rGroup, int checkedID) {
				final RadioGroup group = rGroup;
				final int checkedId = checkedID;
				((QuestionnaireActivityInterface) getActivity()).setChecked(mQuestion.getId());
				
				// Adding goals alert dialog, goals not yet implemented
				if (mQuestion.getId() == GoalDataHandler.QUESTION_ID_WHAT_TO_ACHIEVE && (checkedId == 0 || checkedId == 1)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					final LinearLayout xAmountLayout = (LinearLayout) layoutInflater.inflate(R.layout.element_number_entry, null);
					builder.setView(xAmountLayout);
					builder.setTitle(mQuestion.getAnswer(checkedId))
						.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					});
					final AlertDialog helpDialog = builder.create();
					
					helpDialog.setOnShowListener(new DialogInterface.OnShowListener() {
						@Override
						public void onShow(DialogInterface dialog) {
							Button button = helpDialog.getButton(DialogInterface.BUTTON_POSITIVE);
							button.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									String xAnswer = ((EditText) xAmountLayout.findViewById(R.id.number_entry_edit_text)).getText().toString();
									if (xAnswer.length() > 0) {
										mXAmount = Integer.parseInt(xAnswer);
										helpDialog.dismiss();
										RadioButton radioButton = (RadioButton) group.getChildAt(checkedId);
										String radioText = (String) radioButton.getText();
										radioText = radioText.replaceAll(mPreviousX, "" + mXAmount);
										mPreviousX = "" + mXAmount;
										radioButton.setText(radioText);
									} else {
										//((TextView) xAmountLayout.findViewById(R.id.number_entry_title)).setTextColor(res.getColor(R.color.red));
									}
								}
							});
						}
					});
					helpDialog.show();
				} else 
				

				// Third answer for the when question is selecting a day, create a date picker dialog if user selects it
				if (mQuestion.getId() == EventDataHandler.QUESTION_ID_WHEN && checkedId == 2) {
					Calendar calendar = Calendar.getInstance();
					DatePickerDialog dialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							mCalendar.set(Calendar.YEAR, year);
							mCalendar.set(Calendar.MONTH, monthOfYear);
							mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
							RadioButton radioButton = (RadioButton) group.getChildAt(checkedId);
							radioButton.setText(UtilityMethods.getDateAsString(mCalendar.getTimeInMillis(), getActivity()));
							
						}
					}, mCalendar.get(Calendar.YEAR),mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DAY_OF_MONTH));
					dialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
					dialog.show();
				} else 
					
					// If user selects four or more drinks, create dialog for setting the amount
					if (mQuestion.getId() == EventDataHandler.QUESTION_ID_HOW_MUCH && checkedId == 4 && checkedId != mPreviousId) {

					final LinearLayout amountLayout = (LinearLayout) layoutInflater.inflate(R.layout.element_number_entry, null);
					final EditText input = (EditText) amountLayout.findViewById(R.id.number_entry_edit_text);
					input.setHint(getString(R.string.amount_of_drinks));
					final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
						    .setTitle("Syötä määrä")
						    .setView(amountLayout)
						    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						        public void onClick(DialogInterface dialog, int whichButton) {
						        }
						    })
						    .show();
					Button theButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
					theButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							String value = input.getText().toString();
							if (value.length() < 1) {
								Toast.makeText(getActivity(), "Syötä määrä", Toast.LENGTH_SHORT).show();
							} else {
								alertDialog.dismiss();
								mInput = Integer.parseInt(value);
								RadioButton radioButton = (RadioButton) group.getChildAt(checkedId);
								radioButton.setText("" + mInput);
							}
						}
					});
				}
				
				mPreviousId = checkedID;
			}
			
		});
		
		return rootView;
	}
	
	/**
	 * Get the selected answer in this fragment.
	 * @return
	 */
	public int getSelectedAnswer() {
		if (mQuestion.getId() == EventDataHandler.QUESTION_ID_HOW_MUCH && mAnswerGroupView.getCheckedRadioButtonId() == 4) {
			return mInput + 1;
		}
		return mAnswerGroupView.getCheckedRadioButtonId() + 1;
	}
	
	public long getSelectedDate() {
		return mCalendar.getTimeInMillis();
	}
	
	/**
	 * Get the question id of the question represented by this fragment.
	 * @return
	 */
	public int getQuestionId() {
		return mQuestion.getId();
	}
	
	public int getXAmount() {
		if (mQuestion.getId() == GoalDataHandler.QUESTION_ID_WHAT_TO_ACHIEVE &&  getSelectedAnswer() < 2 && getSelectedAnswer() > -1) {
			return mXAmount;
		} else {
			return -1;
		}
	}

}
