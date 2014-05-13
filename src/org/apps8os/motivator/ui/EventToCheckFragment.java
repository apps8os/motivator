package org.apps8os.motivator.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayDataHandler;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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
		((TextView) rootView.findViewById(R.id.amount_of_drinks_yesterday)).setText(getString(R.string.you_drank) + " " + 
				dayHandler.getDrinksForDay(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) + " " + 
				getString(R.string.drinks_yesterday_in_total));
		
		if (mEvent.getName().length() > 0) {
			((TextView) rootView.findViewById(R.id.name)).setText(mEvent.getName());
		}
		return rootView;
	}
	
	public int[] getAnswers() {
		int[] answers = {mDrinksSpinner.getSelectedItemPosition() + 1, mStartTimeSpinner.getSelectedItemPosition(), mEndTimeSpinner.getSelectedItemPosition(), mWithWhoSpinner.getSelectedItemPosition()};
		return answers;
	}
	
	public MotivatorEvent getEvent() {
		return mEvent;
	}
	
}
