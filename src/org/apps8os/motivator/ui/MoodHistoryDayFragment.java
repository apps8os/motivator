package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.DayInHistory;
import org.apps8os.motivator.utils.MotivatorConstants;

import android.support.v4.app.Fragment;
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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Get the DayInHistory from the arguments.
		Bundle bundle = getArguments();
		mDay = bundle.getParcelable(MotivatorConstants.DAY_IN_HISTORY);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_mood_history_day, viewGroup, false);
		TextView title = (TextView)  rootView.findViewById(R.id.mood_history_fragment_title);
		TextView comment = (TextView)  rootView.findViewById(R.id.mood_history_fragment_comment);
		ImageView image = (ImageView) rootView.findViewById(R.id.mood_history_fragment_mood_image);
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
			image.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.temp_emoticon_bw));
			comment.setText("No added moods.");
		} else {
			comment.setText("Best Day EVER!");
		}

		
		return rootView;
	}
	
	
	

}
