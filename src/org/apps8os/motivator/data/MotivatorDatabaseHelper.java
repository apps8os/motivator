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





import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Represents an abstract class for database access. Here are implemented generally applicable methods for all tables.
 * Subclasses should specially handle the operations specific to a table.
 * @author Toni Järvinen
 *
 */

public class MotivatorDatabaseHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final int NUMBER_OF_TABLES = 3;						// Number of tables with the same questionnaire format.
	private static final String DATABASE_NAME = "MotivatorDB";
	
	protected static final String KEY_ENERGYLEVEL = "energylevel";
	protected static final String KEY_MOODLEVEL = "moodlevel";
	protected static final String TABLE_NAME_MOOD_LEVELS = "mood_table";
 	
	protected static final String KEY_ID_ANSWERS = "answers_id";		// Represents a single answering instance, same for all answers in the same instance of a questionnaire.
	protected static final String KEY_ID_QUESTION = "question_id"; 		// The question which the answers belongs to.
	protected static final String KEY_ANSWER = "answer";				// The answer to the question
	protected static final String KEY_TIMESTAMP = "timestamp";			// Timestamp of when the record was created.
	protected static final String KEY_CONTENT = "specific_content";		// A special column, which is specific for the different tables. Handled differently on subclasses.
	
	protected static final String TABLE_NAME_QUESTIONNAIRE = "mood_answers";
	protected static final String TABLE_NAME_EVENTS = "event_answers";
	protected static final String TABLE_NAME_GOALS = "goal_answers";
	
	private static final String[] TABLE_NAMES = {TABLE_NAME_QUESTIONNAIRE, TABLE_NAME_EVENTS, TABLE_NAME_GOALS};
	
	private static final String TABLE_CREATE_MOOD_LEVELS =
			"CREATE TABLE " + TABLE_NAME_MOOD_LEVELS + " (" +
			"id INTEGER PRIMARY KEY, " +
			KEY_ENERGYLEVEL + " INTEGER, " +
			KEY_MOODLEVEL + " INTEGER, " +
			KEY_TIMESTAMP + " INTEGER);";
	
	protected SQLiteDatabase db;
	
	protected MotivatorDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Create the tables.
		for (int i = 0; i < NUMBER_OF_TABLES; i++) {
			String createTable = "CREATE TABLE " + TABLE_NAMES[i] + " (" +
									"id INTEGER PRIMARY KEY, " +
									KEY_ID_ANSWERS + " INTEGER, " +
									KEY_ID_QUESTION + " INTEGER, " +
									KEY_ANSWER + " INTEGER, " +
									KEY_TIMESTAMP + " INTEGER, " +
									KEY_CONTENT + " INTEGER);";
			db.execSQL(createTable);
		}
		db.execSQL(TABLE_CREATE_MOOD_LEVELS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_MOOD_LEVELS);
		for (int i = 0; i < NUMBER_OF_TABLES; i++) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAMES[i]);
		}
		this.onCreate(db);
	}
	
	public void open(){
		db = this.getWritableDatabase();
	}
	
	/**
	 * Delete last row from the given table. Call this from subclass.
	 * @param tableName
	 */
	protected void deleteLastRow(String tableName) {
    	db.delete(tableName, "id = (SELECT MAX(id) FROM " + tableName + ")", null);
    }
    
    /**
     * Inserts the answer to the database. Call this from subclass.
     * @param answer		 answer in integer form, determined from the possible answers for the question
     * @param questionId	 represents the question id, used to determine for which question the answer belongs to
     * @param answersId		 represents one answering instance
     * @param tableName		 the table to insert the answer to, use static names from this class
     */
    
    protected void insertAnswer(int answer, int questionId, int answersId, long content, String tableName) {
    	ContentValues values = new ContentValues();
    	values.put(KEY_ANSWER, answer);
    	values.put(KEY_ID_QUESTION, questionId);
    	values.put(KEY_ID_ANSWERS, answersId);
    	values.put(KEY_TIMESTAMP, System.currentTimeMillis());
    	values.put(KEY_CONTENT, content);
    	db.insert(tableName, null, values);
    }

}
