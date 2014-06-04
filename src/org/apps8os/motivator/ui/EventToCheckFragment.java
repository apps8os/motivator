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

import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;


public class EventToCheckFragment extends Fragment {

	private MotivatorEvent mEvent;
	private Spinner mDrinksSpinner;
	private Spinner mStartTimeSpinner;
	private Spinner mEndTimeSpinner;
	private Spinner mWithWhoSpinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEvent = getArguments().getParcelable(MotivatorEvent.EVENT);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_event_to_check, viewGroup, false);
		
		EventDataHandler eventHandler = new EventDataHandler(getActivity());
		mDrinksSpinner = (Spinner) rootView.findViewById(R.id.planned_drinks_spinner);
		mStartTimeSpinner = (Spinner) rootView.findViewById(R.id.start_time_spinner);
		mEndTimeSpinner = (Spinner) rootView.findViewById(R.id.end_time_spinner);
		mWithWhoSpinner = (Spinner) rootView.findViewById(R.id.with_who_spinner);
		
		
		ArrayAdapter<CharSequence> drinkAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.how_much_answers, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		drinkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mDrinksSpinner.setAdapter(drinkAdapter);
		mDrinksSpinner.setSelection(eventHandler.getRawFieldUnchecked(mEvent.getId(), EventDataHandler.KEY_PLANNED_AMOUNT_OF_DRINKS) + 1);
		
		ArrayAdapter<CharSequence> startAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.start_answers, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mStartTimeSpinner.setAdapter(startAdapter);
		mStartTimeSpinner.setSelection(eventHandler.getRawFieldUnchecked(mEvent.getId(), EventDataHandler.KEY_START_TIME_ANSWER));

		ArrayAdapter<CharSequence> endAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.end_answers, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mEndTimeSpinner.setAdapter(endAdapter);
		mEndTimeSpinner.setSelection(eventHandler.getRawFieldUnchecked(mEvent.getId(), EventDataHandler.KEY_END_TIME_ANSWER));
		
		ArrayAdapter<CharSequence> withWhoAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.with_who_answers, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		withWhoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mWithWhoSpinner.setAdapter(withWhoAdapter);
		mWithWhoSpinner.setSelection(eventHandler.getRawFieldUnchecked(mEvent.getId(), EventDataHandler.KEY_WITH_WHO));
		
		DayDataHandler dayHandler = new DayDataHandler(getActivity());
		((TextView) rootView.findViewById(R.id.amount_of_drinks_yesterday)).setText(getString(R.string.you_marked) + " " + 
				dayHandler.getDrinksForDay(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) + " " + 
				getString(R.string.drinks_yesterday_in_total));
		
		if (mEvent.getName().length() > 0) {
			((TextView) rootView.findViewById(R.id.name)).setText(mEvent.getName());
		} else {
			((TextView) rootView.findViewById(R.id.name)).setVisibility(View.GONE);
		}
		return rootView;
	}
	
	/**
	 * 
	 * @return the answers for this checked event.
	 */
	public int[] getAnswers() {
		int[] answers = {mDrinksSpinner.getSelectedItemPosition() + 1, mStartTimeSpinner.getSelectedItemPosition(), 
				mEndTimeSpinner.getSelectedItemPosition(), mWithWhoSpinner.getSelectedItemPosition()};
		return answers;
	}
	
	/**
	 * 
	 * @return the event that this fragment represents.
	 */
	public MotivatorEvent getEvent() {
		return mEvent;
	}
	
}
