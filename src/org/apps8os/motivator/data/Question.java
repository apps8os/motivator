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

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Represents a Question. Used to store the question information during runtime.
 * @author Toni Järvinen
 *
 */
public class Question implements Parcelable{
	
	public static final String QUESTION = "question";
	
	private int mId;
	private String mQuestion;
	private String[] mAnswers;
	private boolean mRequired;
	
	public Question(int id, String question, String[] answers, boolean required) {
		mId = id;
		mQuestion = question;
		mAnswers = answers;
		mRequired = required;
	}

	public Question(Parcel source) {
		mId = source.readInt();
		mQuestion = source.readString();
		mAnswers = source.createStringArray();
	}

	public static final Parcelable.Creator<Question> CREATOR = new Parcelable.Creator<Question>() {
		@Override
		public Question createFromParcel(Parcel source) {
			return new Question(source);
		}

		@Override
		public Question[] newArray(int size) {
			return new Question[size];
		}
	};
	
	
	public int getId() {
		return mId;
	}

	public String getQuestion() {
		return mQuestion;
	}

	public String getAnswer(int number) {
		return mAnswers[number - 1];
	}
	
	public int getAnswerCount() {
		return mAnswers.length;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeString(mQuestion);
		dest.writeStringArray(mAnswers);
	}

	public boolean isRequired() {
		return mRequired;
	}
	
}
