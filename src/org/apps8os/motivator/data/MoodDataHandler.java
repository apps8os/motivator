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
import org.apps8os.motivator.utils.MotivatorConstants;
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
	private Context mContext;
	
	public MoodDataHandler(Context context) {
		super(context);
		mContext = context;
		
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
		open();
		super.insertAnswer(answer, questionId, answersId, content, TABLE_NAME);
		close();
	}
	
	public void deleteLastRow() {
		open();
    	super.deleteLastRow(TABLE_NAME);
    	close();
    }
	
	public void deleteRowsWithAnsweringId(int answerId) {
		open();
		super.deleteRowsWithAnsweringId(TABLE_NAME, answerId);
		close();
	}
	
    /**
     * Inserting a mood to the database
     * @param energyLevel
     * @param moodLevel
     */
    public void insertMood(int energyLevel, int moodLevel) {
    	open();
    	ContentValues values = new ContentValues();
    	values.put(KEY_ENERGYLEVEL, energyLevel);
    	values.put(KEY_MOODLEVEL, moodLevel);
    	values.put(KEY_TIMESTAMP, System.currentTimeMillis());
    	mDb.insert(TABLE_NAME_MOOD, null, values);
    	close();
    }
    
    /**
     * Inserting a mood to the database
     * @param energyLevel
     * @param moodLevel
     */
    public void insertMood(int energyLevel, int moodLevel, String comment) {
    	open();
    	ContentValues values = new ContentValues();
    	values.put(KEY_ENERGYLEVEL, energyLevel);
    	values.put(KEY_MOODLEVEL, moodLevel);
    	values.put(KEY_CONTENT, comment);
    	values.put(KEY_TIMESTAMP, System.currentTimeMillis());
    	mDb.insert(TABLE_NAME_MOOD, null, values);
    	close();
    }
    
    /**
     * Used to get a DayInHistory representing the day given as milliseconds.
     * @param dayInMillis
     * @return
     */
    public DayInHistory getDayInHistory(long dayInMillis) {
    	open();
    	Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(dayInMillis);
    	Cursor query = null;
    	long[] boundaries = UtilityMethods.getDayInMillis(calendar);
    	
    	String selection = KEY_TIMESTAMP + " < " + boundaries[1] + " AND " + KEY_TIMESTAMP +  " > " + boundaries[0];
    	String columns[] = {KEY_MOODLEVEL, KEY_ENERGYLEVEL, KEY_TIMESTAMP, KEY_CONTENT};
    	query = mDb.query(TABLE_NAME_MOOD, columns, selection, null, null, null, null);
    	
    	query.moveToFirst();
    	// Initialize the DayInHistory object with the first timestamp on the constructor.
	    DayInHistory result = new DayInHistory(dayInMillis, mContext);
	    // Add all moods to the DayInHistory object.
	    while (query.getCount() > 0 && !query.isClosed()) {
	    	result.addMoodLevel(query.getInt(0));
	    	result.addEnergyLevel(query.getInt(1));
    		String comment = query.getString(3);
    		if (comment != MotivatorConstants.NO_COMMENT) {
    			result.addComment(comment);
	    	}
	    	if (query.isLast()) {
	    		query.close();
	    	} else {
	    		query.moveToNext();
	    	}
	    }
	    close();
	    return result;
    }
    
    /**
     * Used to get an array of days from given timestamp.
     * @param fromInMillis from timestamp
     * @param amountOfDays how many days
     * @return
     */
    public DayInHistory[] getDaysAfter(long fromInMillis, int amountOfDays) {
    	DayInHistory[] days = new DayInHistory[amountOfDays];
    	GregorianCalendar calendar = new GregorianCalendar();
    	calendar.setTimeInMillis(fromInMillis);
    	for (int i = 0; i < amountOfDays; i++) {
    		days[i] = getDayInHistory(calendar.getTimeInMillis());
    		calendar.add(Calendar.DATE, 1);
    	}
    	return days;
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
