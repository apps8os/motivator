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
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.data.Mood;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
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
		mDay.setEvents();
		DayDataHandler dayDataHandler = new DayDataHandler(getActivity());
		int todaysDrinks = dayDataHandler.getDailyDrinkAmount(mDay.getDateInMillis());
		
		if (todaysDrinks < 0) {
			todaysDrinks = dayDataHandler.getClickedDrinksForDay(mDay.getDateInMillis());
		}
		
		TextView title = (TextView)  rootView.findViewById(R.id.mood_history_fragment_title);
		TextView commentView = (TextView)  rootView.findViewById(R.id.mood_history_fragment_comment);
		if (todaysDrinks > 0) {
			((TextView) rootView.findViewById(R.id.mood_history_fragment_drink_text)).setText(getString(R.string.you_drank) + " " +
					todaysDrinks + " " + getString(R.string.drinks_in_total));
			((ImageView) rootView.findViewById(R.id.mood_history_fragment_drink_image)).setImageResource(R.drawable.drink_icon);
		}
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
			
		} else {
			// DUMMY
			commentView.setText(R.string.last_mood);
			commentView.setGravity(Gravity.CENTER);
		}
		ImageView energyImage = (ImageView) mainMoodImage.findViewById(R.id.mood_history_fragment_mood_image_energy);
		ImageView moodImage = (ImageView) mainMoodImage.findViewById(R.id.mood_history_fragment_mood_image_mood);
		if (mDay.getAvgMoodLevel() == 0) {
			energyImage.setImageDrawable(mRes.getDrawable(R.drawable.energy_no_data));
			moodImage.setImageDrawable(mRes.getDrawable(R.drawable.mood_no_data));
			commentView.setText(mRes.getString(R.string.no_added_moods));
		} else {
			Mood firstMoodOfTheDay = mDay.getFirstMoodOfTheDay();
			title.setText(getString(R.string.your_mood) + ", " + firstMoodOfTheDay.getTimeAsString(getActivity()));
			energyImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("energy" + firstMoodOfTheDay.getEnergy(), "drawable", getActivity().getPackageName())));
			moodImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("mood" + firstMoodOfTheDay.getMood(), "drawable", getActivity().getPackageName())));
			final String comment = firstMoodOfTheDay.getComment();
			if (comment.length() > 0) {
				commentView.setText("\"" + comment + "\"");
			} else {
				commentView.setText(Html.fromHtml(getString(R.string.energy) + ": " + getString(Mood.ENERGY_TITLES[firstMoodOfTheDay.getEnergy() - 1])
						+ "<br>" + getString(R.string.mood) + ": " + getString(Mood.MOOD_TITLES[firstMoodOfTheDay.getMood() - 1])));
			}
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
		if (moods.size() > 0) {
			moods.remove(0);
		}
		if (moods.size() > 0) {
			for (Mood mood : moods) {
				LinearLayout moodView = (LinearLayout) mInflater.inflate(R.layout.element_mood_history_moodlist_mood, rootLayout, false);
				((TextView) moodView.findViewById(R.id.moodlist_mood_time)).setText(mood.getTimeAsString(getActivity()));
				if (mood.getComment().length() < 1) {
					((TextView) moodView.findViewById(R.id.moodlist_mood_comment)).setVisibility(View.GONE);
				} else {
					((TextView) moodView.findViewById(R.id.moodlist_mood_comment)).setText("\"" + mood.getComment() + "\"");
				}
				LinearLayout moodImageRoot = (LinearLayout) moodView.findViewById(R.id.mood_image_root);
				ImageView energyImage = (ImageView) moodImageRoot.getChildAt(0);
				energyImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("energy" + mood.getEnergy(), "drawable", getActivity().getPackageName())));
				ImageView moodImage = (ImageView) moodImageRoot.getChildAt(1);
				moodImage.setImageDrawable(mRes.getDrawable(mRes.getIdentifier("mood" + mood.getMood(), "drawable", getActivity().getPackageName())));

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
