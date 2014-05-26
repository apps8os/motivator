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

import java.util.concurrent.TimeUnit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


/**
 * Handles the sprint data in the application
 * @author Toni Järvinen
 *
 */
public class SprintDataHandler extends MotivatorDatabaseHelper {

	public SprintDataHandler(Context context) {
		super(context);
	}
	
	/**
	 * Inserting a sprint to the database
	 * @param startTime
	 * @param days
	 * @param sprintTitle
	 */
	public void insertSprint(long startTime, int days, String sprintTitle) {
		open();
		endCurrentSprint();
		ContentValues values = new ContentValues();
		values.put(KEY_SPRINT_START, startTime);
		values.put(KEY_SPRINT_DAYS, days);
		values.put(KEY_SPRINT_TITLE, sprintTitle);
		long endTime = startTime + TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS);
		values.put(KEY_SPRINT_END, endTime);
		mDb.insert(TABLE_NAME_SPRINTS, null, values);
		close();
	}
	
	/**
	 * Ending the current sprint.
	 */
	private void endCurrentSprint() {
		Sprint current = getCurrentSprintPrivate();
		if (current != null) {
			ContentValues values = new ContentValues();
			values.put(KEY_SPRINT_END, System.currentTimeMillis());
			mDb.update(TABLE_NAME_SPRINTS, values, KEY_ID + " = " + current.getId(), null);
		}
	}
	
	public Sprint getSprint(int id) {
		open();
		String selection = KEY_ID + " = " + id;
		String columns[] = {KEY_ID, KEY_SPRINT_START, KEY_SPRINT_DAYS, KEY_SPRINT_END, KEY_SPRINT_TITLE};
		Cursor cursor = mDb.query(TABLE_NAME_SPRINTS, columns, selection, null, null, null, null);
		if (cursor.moveToFirst()) {
			Sprint sprint = new Sprint(cursor.getLong(1));
			sprint.setId(cursor.getInt(0));
			sprint.setDaysInSprint(cursor.getInt(2));
			sprint.setEndTime(cursor.getLong(3));
			sprint.setSprintTitle(cursor.getString(4));
			cursor.close();
			close();
			return sprint;
		}
		cursor.close();
		close();
		return null;
	}
	
	/**
	 * Gets all sprints.
	 * @return
	 */
	public Sprint[] getSprints() {
		open();
		String columns[] = {KEY_ID, KEY_SPRINT_START, KEY_SPRINT_DAYS, KEY_SPRINT_END, KEY_SPRINT_TITLE};
		Cursor cursor = mDb.query(TABLE_NAME_SPRINTS, columns, null, null, null, null, null);
		Sprint[] sprints = new Sprint[cursor.getCount()];
		if (cursor.moveToFirst()) {
			for (int i = 0; i < cursor.getCount(); i++) {
				sprints[i] = new Sprint(cursor.getLong(1));
				sprints[i].setId(cursor.getInt(0));
				sprints[i].setDaysInSprint(cursor.getInt(2));
				sprints[i].setEndTime(cursor.getLong(3));
				sprints[i].setSprintTitle(cursor.getString(4));
				cursor.moveToNext();
			}
			cursor.close();
			close();
			return sprints;
		}
		cursor.close();
		close();
		return null;
	}
	
	private Sprint getCurrentSprintInner() {
		String selection = KEY_SPRINT_START + " < " + System.currentTimeMillis() + " AND " + KEY_SPRINT_END + " > " + System.currentTimeMillis();
		String columns[] = {KEY_ID, KEY_SPRINT_START, KEY_SPRINT_DAYS, KEY_SPRINT_END, KEY_SPRINT_TITLE};
		Cursor cursor = mDb.query(TABLE_NAME_SPRINTS, columns, selection, null, null, null, KEY_SPRINT_START);
		cursor.moveToFirst();
		if (cursor.getCount() > 1) {
			while (!cursor.isLast()) {
				cursor.moveToNext();
			}
		}
		Sprint current = null;
		if (cursor.getCount() > 0) {
			current = new Sprint(cursor.getLong(1));
			current.setId(cursor.getInt(0));
			current.setDaysInSprint(cursor.getInt(2));
			current.setEndTime(cursor.getLong(3));
			current.setSprintTitle(cursor.getString(4));
		}
		cursor.close();
		return current;
	}
	
	/**
	 * Gets the current sprint.
	 * @return
	 */
	public Sprint getCurrentSprint() {
		open();
		Sprint current = getCurrentSprintInner();
		close();
		return current;
	}
	
	private Sprint getCurrentSprintPrivate() {
		Sprint current = getCurrentSprintInner();
		return current;
	}
	
	/**
	 * Gets the latest sprint that has ended.
	 * @return
	 */
	public Sprint getLatestEndedSprint() {
		open();
		String selection = KEY_SPRINT_END + " < " + System.currentTimeMillis();
		String columns[] = {KEY_ID, KEY_SPRINT_START, KEY_SPRINT_DAYS, KEY_SPRINT_END, KEY_SPRINT_TITLE};
		Cursor cursor = mDb.query(TABLE_NAME_SPRINTS, columns, selection, null, null, null, KEY_SPRINT_END);
		if(cursor.moveToLast()) {
			Sprint latest = new Sprint(cursor.getLong(1));
			latest.setId(cursor.getInt(0));
			latest.setDaysInSprint(cursor.getInt(2));
			latest.setEndTime(cursor.getLong(3));
			latest.setSprintTitle(cursor.getString(4));
			close();
			cursor.close();
			return latest;
		}
		cursor.close();
		close();
		return null;
	}
	
	/**
	 * Gets the start time of the first sprint.
	 * @return
	 */
	public long getFirstSprintStart() {
		open();
		String selection = "SELECT MIN(" + KEY_SPRINT_START + ") FROM " + TABLE_NAME_SPRINTS;
		Cursor query = mDb.rawQuery(selection, null);
		query.moveToFirst();
		long result = query.getLong(0);
		query.close();
		close();
		return result;
	}

}
