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
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apps8os.motivator.R;
import org.apps8os.motivator.utils.UtilityMethods;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
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
	
	public void deleteRowsWithAnsweringId(int answerId) {
		super.deleteRowsWithAnsweringId(TABLE_NAME, answerId);
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
    	mDb.insert(TABLE_NAME_MOOD, null, values);
    }
    
    
    /**
     * Give the date, return cursor over moods from the previous date that had moods. Note: Not yesterday if it did
     * not have any mood data recorded.
     * @return
     */
    public Cursor getPreviousMoodFrom(long timeStamp) {
    	Cursor query = null;
    	
    	// Get the time stamp for the next mood data from the given timestamp.
    	long nextMood = getNextMoodTimestamp(timeStamp);
    	
    	// If one was not found, return null
    	if (nextMood == 0) {
    		return null;
    	}
    	// Create a Calendar object for the nextMood timestamp.
    	Calendar date = new GregorianCalendar();
    	date.setTimeInMillis(nextMood);
    	// Get the day boundaries for the day.
    	long[] boundaries = UtilityMethods.getDayInMillis(date);
    	
    	String selection = KEY_TIMESTAMP + " < " + boundaries[1] + " AND " + KEY_TIMESTAMP +  " > " + boundaries[0];
    	String columns[] = {KEY_MOODLEVEL, KEY_ENERGYLEVEL, KEY_TIMESTAMP};
    	query = mDb.query(TABLE_NAME_MOOD, columns, selection, null, null, null, null);
    	
    	if (query.moveToFirst()) {
    		return query;
    	} else {
    		return null;
    	}
    }
    
    public Cursor getMoodsForDay(Calendar day) {
    	Cursor query = null;
    	long[] boundaries = UtilityMethods.getDayInMillis(day);
    	
    	String selection = KEY_TIMESTAMP + " < " + boundaries[1] + " AND " + KEY_TIMESTAMP +  " > " + boundaries[0];
    	String columns[] = {KEY_MOODLEVEL, KEY_ENERGYLEVEL, KEY_TIMESTAMP};
    	query = mDb.query(TABLE_NAME_MOOD, columns, selection, null, null, null, null);
    	
    	if (query.moveToFirst()) {
    		return query;
    	} else {
    		return null;
    	}
    }
    
    /**
     * Gets the next latest mood data from the given timestamp.
     * @param fromTimestamp
     * @return
     */
    public long getNextMoodTimestamp(long fromTimestamp) {
    	String selection = KEY_TIMESTAMP + " < " + fromTimestamp;
    	String columns[] = {KEY_TIMESTAMP};
    	Cursor query = mDb.query(TABLE_NAME_MOOD, columns, selection, null, null, null, KEY_TIMESTAMP);
    	long result;
    	if (query.moveToLast()) {
    		result = query.getLong(0);
    	} else {
    		result = 0;
    	}
    	query.close();
    	return result;
    }
    
    /**
     * Gets the size of the mood table.
     * @return
     */
    public long getMoodTableSize() {
    	String columns[] = {KEY_TIMESTAMP};
    	Cursor query = mDb.query(TABLE_NAME_MOOD, columns, null, null, null, null, null);
    	long size = query.getCount();
    	query.close();
    	return size;
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
