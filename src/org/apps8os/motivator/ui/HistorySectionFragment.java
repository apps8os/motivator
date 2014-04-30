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
import org.apps8os.motivator.data.Sprint;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Represents the history section in the UI.
 * TODO: Rewrite to do correct behavior
 */
public class HistorySectionFragment extends Fragment {
	
	public HistorySectionFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final Sprint currentSprint = getArguments().getParcelable(Sprint.CURRENT_SPRINT);
		
		View rootView = inflater.inflate(
				R.layout.fragment_main_activity_history_section, container, false);
		LinearLayout buttonLayout = (LinearLayout) rootView.findViewById(R.id.main_activity_fragmenet_layout);
		
		LinearLayout moodButton = (LinearLayout) rootView.findViewById(R.id.main_activity_mood_history_button);
		moodButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MoodHistoryActivity.class);
				intent.putExtra(Sprint.CURRENT_SPRINT, currentSprint);
				startActivity(intent);
			}
		});
		
		LinearLayout moodRelButton = (LinearLayout) rootView.findViewById(R.id.main_activity_relation_history_button);
		moodRelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MoodRelationHistoryActivity.class);
				intent.putExtra(Sprint.CURRENT_SPRINT, currentSprint);
				startActivity(intent);
			}
			
		});
		
		LinearLayout eventHistoryButton = (LinearLayout) rootView.findViewById(R.id.main_activity_event_history_button);
		eventHistoryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), EventHistoryActivity.class);
				intent.putExtra(Sprint.CURRENT_SPRINT, currentSprint);
				startActivity(intent);
			}
			
		});
		
		return rootView;
	}
}
