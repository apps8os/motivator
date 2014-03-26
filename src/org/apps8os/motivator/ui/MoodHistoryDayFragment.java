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
package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.utils.MotivatorConstants;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Represents a day in the mood history
 * @author Toni Järvinen
 *
 */
public class MoodHistoryDayFragment extends Fragment {
	
	private static DayInHistory mDay;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		mDay = bundle.getParcelable(MotivatorConstants.DAY_IN_HISTORY);
		View rootView = inflater.inflate(
				R.layout.fragment_mood_history_day, viewGroup, false);
		TextView title = (TextView)  rootView.findViewById(R.id.mood_history_fragment_title);
		TextView comment = (TextView)  rootView.findViewById(R.id.mood_history_fragment_comment);
		ImageView image = (ImageView) rootView.findViewById(R.id.mood_history_fragment_mood_image);
		Resources res = getActivity().getResources();
		// Set default page if the day is null or set the content from day object if it exists.
		if (mDay != null) {
			title.setText(R.string.your_mood);
			// DUMMY
			
		} else {
			// DUMMY
			comment.setText(R.string.last_mood);
			comment.setGravity(Gravity.CENTER);
		}
		
		if (mDay.getAvgMoodLevel() == 0) {
			image.setImageDrawable(res.getDrawable(R.drawable.temp_emoticon_bw));
			comment.setText(res.getString(R.string.no_added_moods));
		} else {
			comment.setText("Best Day EVER!");
		}

		
		return rootView;
	}
	
	/**
	 * Save the day to the savedInstanceState.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(MotivatorConstants.DAY_IN_HISTORY, mDay);
	}
	
	
	

}
