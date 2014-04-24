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

import java.util.ArrayList;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.Mood;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Represents a day in the mood history
 * @author Toni Järvinen
 *
 */
public class MoodHistoryDayFragment extends Fragment {
	
	private static DayInHistory mDay;
	private LayoutInflater mInflater;
	private Resources mRes;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		mDay = bundle.getParcelable(DayInHistory.DAY_IN_HISTORY);
		mInflater = inflater;
		View rootView = mInflater.inflate(
				R.layout.fragment_mood_history_day, viewGroup, false);
		mRes = getActivity().getResources();
		
		TextView title = (TextView)  rootView.findViewById(R.id.mood_history_fragment_title);
		TextView commentView = (TextView)  rootView.findViewById(R.id.mood_history_fragment_comment);
		final LinearLayout mainMoodImage = (LinearLayout) rootView.findViewById(R.id.mood_history_fragment_mood_image);
		mainMoodImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mainMoodImage.animate().rotationBy(360).setDuration(1000);
			}
		});
		// Set default page if the day is null or set the content from day object if it exists.
		if (mDay != null) {
			title.setText(R.string.your_mood);
			// DUMMY
			
		} else {
			// DUMMY
			commentView.setText(R.string.last_mood);
			commentView.setGravity(Gravity.CENTER);
		}
		
		if (mDay.getAvgMoodLevel() == 0) {
			//mainMoodImage.setImageDrawable(res.getDrawable(R.drawable.temp_emoticon_bw));
			commentView.setText(mRes.getString(R.string.no_added_moods));
		} else {
			Mood firstMoodOfTheDay = mDay.getFirstMoodOfTheDay();
			ImageView energyImage = (ImageView) mainMoodImage.findViewById(R.id.mood_history_fragment_mood_image_energy);
			energyImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("energy" + firstMoodOfTheDay.getEnergy(), "drawable", getActivity().getPackageName())));
			ImageView moodImage = (ImageView) mainMoodImage.findViewById(R.id.mood_history_fragment_mood_image_mood);
			moodImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("mood" + firstMoodOfTheDay.getMood(), "drawable", getActivity().getPackageName())));
			String comment = firstMoodOfTheDay.getComment();
			commentView.setText(comment);
		}
		
		loadMoods((LinearLayout) rootView.findViewById(R.id.mood_history_day_moodlist));
		
		return rootView;
	}
	
	/**
	 * Load all the moods for the day.
	 * @param rootLayout
	 */
	private void loadMoods(LinearLayout rootLayout) {
		ArrayList<Mood> moods = mDay.getMoods();
		if (moods.size() > 1) {
			for (int i = 1; i < moods.size(); i++) {
				LinearLayout moodView = (LinearLayout) mInflater.inflate(R.layout.element_mood_history_moodlist_mood, rootLayout, false);
				((TextView) moodView.findViewById(R.id.moodlist_mood_time)).setText(moods.get(i).getTimeAsString(getActivity()));
				((TextView) moodView.findViewById(R.id.moodlist_mood_comment)).setText(moods.get(i).getComment());
				LinearLayout moodImageRoot = (LinearLayout) moodView.findViewById(R.id.mood_image_root);
				ImageView energyImage = (ImageView) moodImageRoot.getChildAt(0);
				energyImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("energy" + moods.get(i).getEnergy(), "drawable", getActivity().getPackageName())));
				ImageView moodImage = (ImageView) moodImageRoot.getChildAt(1);
				moodImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("mood" + moods.get(i).getMood(), "drawable", getActivity().getPackageName())));

				rootLayout.addView(moodView);
			}
		}
	}
	
	/**
	 * Save the day to the savedInstanceState.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(DayInHistory.DAY_IN_HISTORY, mDay);
	}

}
