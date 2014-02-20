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





import android.content.ContentValues;
import android.content.Context;
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

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "MotivatorDB";
	private static final String KEY_ENERGYLEVEL = "energylevel";
	private static final String KEY_MOODLEVEL = "moodlevel";
	private static final String TABLE_NAME_MOOD_LEVELS = "mood_table";
 	
	//represents a single answering instance, same for all answers in the same instance of a questionnaire.
	private static final String KEY_ID_ANSWERS = "answers_id";
	private static final String KEY_ID_QUESTION = "question_id"; // The question which the answers belongs to.
	private static final String KEY_ANSWER = "answer";
	
	public static final String TABLE_NAME_QUESTIONNAIRE_ANSWERS = "mood_answers";
	public static final String TABLE_NAME_EVENT_ANSWERS = "adding_event_answers";
	public static final String TABLE_NAME_GOAL_ANSWERS = "adding_goal_answers";
	
	private static final String TABLE_CREATE_QUESTIONNAIRE_ANSWERS =
				"CREATE TABLE " + TABLE_NAME_QUESTIONNAIRE_ANSWERS + " (" +
				"id INTEGER PRIMARY KEY, " +
				KEY_ID_ANSWERS + " INTEGER, " +
				KEY_ID_QUESTION + " INTEGER, " +
				KEY_ANSWER + " INTEGER, " +
				"timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
	
	private static final String TABLE_CREATE_EVENT_ANSWERS =
			"CREATE TABLE " + TABLE_NAME_EVENT_ANSWERS + " (" +
			"id INTEGER PRIMARY KEY, " +
			KEY_ID_ANSWERS + " INTEGER, " +
			KEY_ID_QUESTION + " INTEGER, " +
			KEY_ANSWER + " INTEGER, " +
			"timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
	
	private static final String TABLE_CREATE_GOAL_ANSWERS =
			"CREATE TABLE " + TABLE_NAME_GOAL_ANSWERS + " (" +
			"id INTEGER PRIMARY KEY, " +
			KEY_ID_ANSWERS + " INTEGER, " +
			KEY_ID_QUESTION + " INTEGER, " +
			KEY_ANSWER + " INTEGER, " +
			"timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
	
	private static final String TABLE_CREATE_MOOD_LEVELS =
			"CREATE TABLE " + TABLE_NAME_MOOD_LEVELS + " (" +
			"id INTEGER PRIMARY KEY, " +
			KEY_ENERGYLEVEL + " INTEGER, " +
			KEY_MOODLEVEL + " INTEGER, " +
			"timestamp DATETIME DEFAULT CURRENT_TIMESTAMP);";
	
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
			db.execSQL(TABLE_CREATE_QUESTIONNAIRE_ANSWERS);
			db.execSQL(TABLE_CREATE_EVENT_ANSWERS);
			db.execSQL(TABLE_CREATE_GOAL_ANSWERS);
			db.execSQL(TABLE_CREATE_MOOD_LEVELS);
		}
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MOOD_LEVELS);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_QUESTIONNAIRE_ANSWERS);
			
			this.onCreate(db);
		}
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	/**
	 * 
	 * @param tableName
	 */
	public void deleteLastAnswer(String tableName) {
    	mDb.delete(tableName, "id = (SELECT MAX(id) FROM " + tableName + ")", null);
    }
    
    /**
     * Inserts the answer to the database.
     * @param answer		 answer in integer form, determined from the possible answers for the question
     * @param questionId	 represents the question id, used to determine for which question the answer belongs to
     * @param answersId		 represents one answering instance
     * @param tableName		 the table to insert the answer to, use static names from this class
     */
    
    public void insertAnswer(int answer, int questionId, int answersId, String tableName) {
    	ContentValues values = new ContentValues();
    	values.put(KEY_ANSWER, answer);
    	values.put(KEY_ID_QUESTION, questionId);
    	values.put(KEY_ID_ANSWERS, answersId);
    	mDb.insert(tableName, null, values);
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
    	mDb.insert(TABLE_NAME_MOOD_LEVELS, null, values);
    	
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
