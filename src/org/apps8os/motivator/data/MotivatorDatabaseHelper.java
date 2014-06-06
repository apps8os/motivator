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





import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Represents the base class for database access. Here are implemented generally applicable methods for all tables.
 * Subclasses should specially handle the operations specific to a table.
 * @author Toni Järvinen
 *
 */

public class MotivatorDatabaseHelper extends SQLiteOpenHelper {
	
	public static final String ANSWER_ID_INCREMENT_PREFS = "incrementing_prefs";
	public static final String ANSWER_ID = "incrementing_id";
	public static final int COMMENT_ID = 1901;

	private static final int DATABASE_VERSION = 1;
	private static final int NUMBER_OF_TABLES = 3;						// Number of tables with the same questionnaire format.
	private static final String DATABASE_NAME = "MotivatorDB";
	
	protected static final String KEY_ENERGYLEVEL = "energylevel";
	protected static final String KEY_MOODLEVEL = "moodlevel";
	
	protected static final String KEY_SPRINT_START = "sprint_start";
	protected static final String KEY_SPRINT_DAYS = "sprint_days";
	protected static final String KEY_SPRINT_END = "sprint_end";
 	
	protected static final String KEY_ID = "id";
	protected static final String KEY_ID_ANSWERS = "answers_id";		// Represents a single answering instance, same for all answers in the same instance of a questionnaire.
	protected static final String KEY_ID_QUESTION = "question_id"; 		// The question which the answers belongs to.
	protected static final String KEY_ANSWER = "answer";				// The answer to the question
	protected static final String KEY_TIMESTAMP = "timestamp";			// Timestamp of when the record was created.
	protected static final String KEY_CONTENT = "specific_content";		// A special column, which is specific for the different tables. Handled differently on subclasses.
	protected static final String KEY_SPRINT_TITLE = "sprint_title";
	protected static final String KEY_COMMENT = "comment";
	
	protected static final String TABLE_NAME_QUESTIONNAIRE = "mood_answers";
	protected static final String TABLE_NAME_EVENTS = "event_answers";
	protected static final String TABLE_NAME_GOALS = "goal_answers";
	protected static final String TABLE_NAME_MOOD_LEVELS = "mood_table";
	protected static final String TABLE_NAME_SPRINTS = "sprint_table";
	protected static final String TABLE_NAME_DRINKS = "drink_table";
	
	private static final String[] TABLE_NAMES = {TABLE_NAME_QUESTIONNAIRE, TABLE_NAME_EVENTS, TABLE_NAME_GOALS};
	private static final String TABLE_CREATE_SPRINTS =
			"CREATE TABLE " + TABLE_NAME_SPRINTS + " (" +
			KEY_ID + " INTEGER PRIMARY KEY, " +
			KEY_SPRINT_START + " INTEGER, " +
			KEY_SPRINT_DAYS + " INTEGER, " +
			KEY_SPRINT_END + " INTEGER, " +
			KEY_SPRINT_TITLE + " TEXT);";
	private static final String TABLE_CREATE_MOOD_LEVELS =
			"CREATE TABLE " + TABLE_NAME_MOOD_LEVELS + " (" +
			KEY_ID + " INTEGER PRIMARY KEY, " +
			KEY_ENERGYLEVEL + " INTEGER, " +
			KEY_MOODLEVEL + " INTEGER, " +
			KEY_CONTENT + " TEXT, " +
			KEY_TIMESTAMP + " INTEGER);";
	private static final String TABLE_CREATE_DRINKS =
			"CREATE TABLE " + TABLE_NAME_DRINKS + " (" +
	        KEY_ID + " INTEGER PRIMARY KEY, " +
			KEY_TIMESTAMP + " INTEGER);";
	
	protected SQLiteDatabase mDb;
	
	protected MotivatorDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(EventDataHandler.CREATE_EVENT_TABLE);
		db.execSQL(GoalDataHandler.CREATE_GOAL_TABLE);
		db.execSQL(TABLE_CREATE_MOOD_LEVELS);
		db.execSQL(TABLE_CREATE_SPRINTS);
		db.execSQL(TABLE_CREATE_DRINKS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MOOD_LEVELS);
		for (int i = 0; i < NUMBER_OF_TABLES; i++) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMES[i]);
		}
		this.onCreate(db);
	}
	
	public SQLiteDatabase open(){
		mDb = this.getWritableDatabase();
		return mDb;
	}
	
	public boolean isOpen() {
		return mDb.isOpen();
	}
	
	/**
	 * Delete last row from the given table. Call this from subclass.
	 * @param tableName
	 */
	protected void deleteLastRow(String tableName) {
    	mDb.delete(tableName, "id = (SELECT MAX(id) FROM " + tableName + ")", null);
    }
	
	protected void deleteRowsWithAnsweringId(String tableName, int answerId) {
		mDb.delete(tableName, KEY_ID_ANSWERS + " = " + answerId, null);
	}

}
