package org.apps8os.motivator.ui;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayDataHandler;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mEvent = getArguments().getParcelable(MotivatorEvent.EVENT);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_event_to_check, viewGroup, false);
		
		
		Spinner drinks = (Spinner) rootView.findViewById(R.id.planned_drinks_spinner);
		Spinner startTime = (Spinner) rootView.findViewById(R.id.start_time_spinner);
		Spinner endTime = (Spinner) rootView.findViewById(R.id.end_time_spinner);
		ArrayAdapter<CharSequence> drinkAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.how_much_answers, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		drinkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		drinks.setAdapter(drinkAdapter);
		
		drinks.setSelection(mEvent.getPlannedDrinks() + 1);
		
		ArrayAdapter<CharSequence> startAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.start_answers, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		startAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		startTime.setAdapter(startAdapter);
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(mEvent.getStartTime());
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		if (hourOfDay < 5) {
		} else if (hourOfDay < 18) {
			startTime.setSelection(1);
		} else if (hourOfDay < 21) {
			startTime.setSelection(2);
		} else {
			startTime.setSelection(3);
		}
		
		ArrayAdapter<CharSequence> endAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.end_answers, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		endAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		endTime.setAdapter(endAdapter);
		
		if (mEvent.getEndTime() == 0) {
		} else {
			calendar.setTimeInMillis(mEvent.getEndTime());
			hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
			if (hourOfDay < 21) {
				endTime.setSelection(1);
			} else if (hourOfDay < 24) {
				endTime.setSelection(2);
			} else {
				endTime.setSelection(3);
			}
		}
		DayDataHandler dayHandler = new DayDataHandler(getActivity());
		((TextView) rootView.findViewById(R.id.amount_of_drinks_yesterday)).setText(getString(R.string.you_drank) + " " + 
				dayHandler.getDrinksForDay(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) + " " + 
				getString(R.string.drinks_yesterday_in_total));
		
		if (mEvent.getName().length() > 0) {
			((TextView) rootView.findViewById(R.id.name)).setText(mEvent.getName());
		}
		return rootView;
	}
	
}
