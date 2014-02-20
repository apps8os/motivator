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
package org.apps8os.motivator.io;



import org.apps8os.motivator.R;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Represents a class for handling database access. Implemented as a singleton.
 * @author Toni JŠrvinen
 *
 */
public class MotivatorDatabase {


	private static final String DATABASE_NAME = "MotivatorDB";
	
	private static final int DATABASE_VERSION = 1;
	private static final String KEY_ENERGYLEVEL = "energylevel";

	private static final String KEY_MOODLEVEL = "moodlevel";
	private static final String MOOD_TABLE_NAME = "mood_table";
				
 	private static final String MOOD_TABLE_CREATE =
				"CREATE TABLE " + MOOD_TABLE_NAME + " (" +
				"id INTEGER PRIMARY KEY, " +
				KEY_ENERGYLEVEL + " INTEGER, " +
				KEY_MOODLEVEL + " INTEGER, " +
				"timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
	//represents a single answering instance, same for all answers in the same instance of a questionnaire.
	private static final String KEY_ID_ANSWERS = "answers_id";
	private static final String KEY_ID_QUESTION = "question_id"; // The question which the answers belongs to.
	
	private static final String KEY_ANSWER = "answer";
	private static final String QUESTIONNAIRE_TABLE_SHORT_NAME = "short_mood_answers";
	
	private static final String QUESTIONAIRE_SHORT_ANSWERS_TABLE_CREATE =
				"CREATE TABLE " + QUESTIONNAIRE_TABLE_SHORT_NAME + " (" +
				"id INTEGER PRIMARY KEY, " +
				KEY_ID_ANSWERS + " INTEGER, " +
				KEY_ID_QUESTION + " INTEGER, " +
				KEY_ANSWER + " INTEGER, " +
				"timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
	private static final String KEY_QUESTION = "question";  // questions
	
	private static final String KEY_ANSWERS = "answers";  // possible answers
	private static final String QUESTIONS_TABLE_NAME = "mood_questions";
	
	private static final String QUESTIONS_TABLE_CREATE = 
				"CREATE TABLE " + QUESTIONS_TABLE_NAME + " (" +
				"id INTEGER PRIMARY KEY, " +
				KEY_QUESTION + " TEXT, " +
				KEY_ANSWERS + " TEXT);";
	
	private static MotivatorDatabase mMyDatabase;
	public static MotivatorDatabase getInstance(Context context) {
		if (mMyDatabase == null) {
			mMyDatabase = new MotivatorDatabase(context.getApplicationContext());
		}
		return mMyDatabase;
	}
	private Context mContext;
	
	private MyDatabaseOpenHelper mDbHelper;
	
	private SQLiteDatabase mDb;
	
	private MotivatorDatabase(Context context) {
		// Use application context for the singleton
		mContext = context.getApplicationContext();
		mDbHelper = new MyDatabaseOpenHelper(context);
	}
	
	private class MyDatabaseOpenHelper extends SQLiteOpenHelper {

		public MyDatabaseOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
	
		@Override
		public void onCreate(SQLiteDatabase db) {
			// Create the tables.
			db.execSQL(QUESTIONAIRE_SHORT_ANSWERS_TABLE_CREATE);
			db.execSQL(QUESTIONS_TABLE_CREATE);
			db.execSQL(MOOD_TABLE_CREATE);
			
			
			ContentValues values = new ContentValues();
			Resources res = mContext.getResources();
			
			// Populate a table to the database with the questions.
			for (int i = 1; i <= res.getInteger(R.integer.mood_question_amount); i++) {
				try {
					String[] question = res.getStringArray(res.getIdentifier("mood_question" + i, "array" , mContext.getPackageName()));
					values.put(KEY_QUESTION, question[0]);
					
					String answers = "";
					for (int j = 1; j < question.length; j++) {
						answers = answers + question[j] + "; ";
					}
					values.put(KEY_ANSWERS, answers);
					db.insert(QUESTIONS_TABLE_NAME, null, values);
					values.clear();
				} catch (NotFoundException e) {
					break;
				}
			}
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	
			db.execSQL("DROP TABLE IF EXISTS " + MOOD_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + QUESTIONS_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + QUESTIONNAIRE_TABLE_SHORT_NAME);
			
			this.onCreate(db);
		}
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	public void deleteLastAnswer() {
    	mDb.delete(QUESTIONNAIRE_TABLE_SHORT_NAME, "id = (SELECT MAX(id) FROM " + QUESTIONNAIRE_TABLE_SHORT_NAME + ")", null);
    }
	
    /**
     * Gets the desired question from the question database.
     * @param questionId	 represents which question to get
     * @return				 return the question
     */
    public String getAnswers(int questionId) {
    	// Get a cursor to the desired row in database.
    	String[] columns = {KEY_ANSWERS};
    	Cursor cursor = mDb.query(QUESTIONS_TABLE_NAME, columns, "id = " + questionId, null, null, null, null);
    	
    	// Need to move it to first to get the desired row. Close the cursor before returning.
    	cursor.moveToFirst();
    	String answers = cursor.getString(0);
    	cursor.close();
    	
    	return answers;
    }
    
    /**
     * Gets the desired question from the question database.
     * @param questionId	 represents which question to get
     * @return				 return the question
     */
    public String getQuestion(int questionId) {
    	// Get a cursor to the desired row in database.
    	String[] columns = {KEY_QUESTION};
    	Cursor cursor = mDb.query(QUESTIONS_TABLE_NAME, columns, "id = " + questionId, null, null, null, null);
    	
    	// Need to move it to first to get the desired row. Close the cursor before returning.
    	cursor.moveToFirst();
    	String question = cursor.getString(0);
    	cursor.close();
    	
    	return question;
    }
    
    /**
     * Inserts the answer to the database.
     * @param answer		 answer in integer form, determined from the possible answers for the question
     * @param questionId	 represents the question id, used to determine for which question the answer belongs to
     * @param answersId		 represents one answering instance
     */
    
    public void insertAnswer(int answer, int questionId, int answersId) {
    	ContentValues values = new ContentValues();
    	values.put(KEY_ANSWER, answer);
    	values.put(KEY_ID_QUESTION, questionId);
    	values.put(KEY_ID_ANSWERS, answersId);
    	mDb.insert(QUESTIONNAIRE_TABLE_SHORT_NAME, null, values);
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
    	mDb.insert(MOOD_TABLE_NAME, null, values);
    	
    	Log.d("mood levels", ""+ energyLevel + moodLevel);
    }
    
    /**
	 * 
	 * @return This class
	 * @throws SQLException
	 */
	public MotivatorDatabase open() throws SQLException {
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

}
