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

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;

/**
 * Handles the access for event data. The specific content in mood database is...
 * open() before using and close() after done
 * @author Toni Järvinen
 *
 */
public class MoodDataHandler extends MotivatorDatabaseHelper {
	
	private static final String TABLE_NAME = TABLE_NAME_QUESTIONNAIRE;
	private static final String TABLE_NAME_MOOD = TABLE_NAME_MOOD_LEVELS;
	
	private SparseArray<Question> mQuestions;
	
	public MoodDataHandler(Context context) {
		super(context);
		
		mQuestions = new SparseArray<Question>();
		Resources res = context.getResources();
		// Inserting the questions to the SpareArrays.
		String[] moodQuestionIds =  res.getStringArray(R.array.mood_question_ids);
		for (int i = 0; i < moodQuestionIds.length; i++) {
			// String array of questions
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier(moodQuestionIds[i], "array", context.getPackageName()));
			// discard the "id" part of the question id
			int id = Integer.parseInt(moodQuestionIds[i].substring(2));
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
	
    /**
     * Inserting a mood to the database
     * @param energyLevel
     * @param moodLevel
     */
    public void insertMood(int energyLevel, int moodLevel) {
    	ContentValues values = new ContentValues();
    	values.put(KEY_ENERGYLEVEL, energyLevel);
    	values.put(KEY_MOODLEVEL, moodLevel);
    	values.put(KEY_TIMESTAMP, System.currentTimeMillis());
    	db.insert(TABLE_NAME_MOOD, null, values);
    }
    
	public Question getQuestion(int id) {
		return mQuestions.get(id);
	}
	
	public int getAmountOfQuestions() {
		return mQuestions.size();
	}
	
	public int getFirstQuestionId() {
		return mQuestions.keyAt(0);
	}
	
}
