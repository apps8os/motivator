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

import org.apps8os.motivator.utils.UtilityMethods;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents a mood.
 * @author Toni Järvinen
 *
 */
public class Mood implements Parcelable {
	
	private int mMood;
	private int mEnergy;
	private long mTimestamp;
	private String mComment;

	/**
	 * @return the mComment
	 */
	public String getComment() {
		return mComment;
	}

	public Mood(int mood, int energy, long timestamp, String comment) {
		mMood = mood;
		mEnergy = energy;
		mTimestamp = timestamp;
		mComment = comment;
	}
	
	private Mood(Parcel source) {
		mMood = source.readInt();
		mEnergy  = source.readInt();
		mTimestamp = source.readLong();
		mComment = source.readString();
	}
	
	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mMood);
		dest.writeInt(mEnergy);
		dest.writeLong(mTimestamp);
		dest.writeString(mComment);
	}
	
	public static final Parcelable.Creator<Mood> CREATOR = new Parcelable.Creator<Mood>() {
		@Override
		public Mood createFromParcel(Parcel source) {
			return new Mood(source);
		}

		@Override
		public Mood[] newArray(int size) {
			return new Mood[size];
		}
	};

	public int getEnergy() {
		return mEnergy;
	}

	public void setEnergy(int mEnergy) {
		this.mEnergy = mEnergy;
	}

	public int getMood() {
		return mMood;
	}

	public void setMood(int mMood) {
		this.mMood = mMood;
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public void setTimestamp(long mTimestamp) {
		this.mTimestamp = mTimestamp;
	}
	
	public String getTimeAsString(Context context) {
		return UtilityMethods.getTimeAsString(mTimestamp, context);
	}

}
