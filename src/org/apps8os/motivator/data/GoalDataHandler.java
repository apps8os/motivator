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
package org.apps8os.motivator.data;


import java.util.Arrays;

import org.apps8os.motivator.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;

/**
 * Handles the access for goal data. The specific content in goal database is...
 * open() before using and close() after done
 * @author Toni Järvinen
 *
 */
public class GoalDataHandler extends MotivatorDatabaseHelper {
	
	private static final String TABLE_NAME = TABLE_NAME_GOALS;
	private SparseArray<Question> mQuestions;
	
	public GoalDataHandler(Context context) {
		super(context);
		
		mQuestions = new SparseArray<Question>();
		Resources res = context.getResources();
		// Inserting the questions to the SpareArrays.
		for (int i = 1; i <= res.getInteger(R.integer.adding_goal_amount); i++) {
			// String array of questions
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier("adding_goal" + i, "array" , context.getPackageName()));
			int id = i;
			// Creation of new Question object and inserting it to the array.
			Question question = new Question(id, questionAndAnswers[0], Arrays.copyOfRange(questionAndAnswers, 1, questionAndAnswers.length));
			mQuestions.put(id, question);
		}
	}
	
	public void insertAnswer(int answer, int questionId, int answersId, long content) {
		super.insertAnswer(answer, questionId, answersId, content, TABLE_NAME);
	}
	
	public void deleteLastRow() {
    	super.deleteLastRow(TABLE_NAME);
	}
	
	public Question getQuestion(int id) {
		return mQuestions.get(id);
	}
}
