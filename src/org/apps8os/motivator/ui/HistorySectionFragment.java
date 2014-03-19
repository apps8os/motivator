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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
		View rootView = inflater.inflate(
				R.layout.fragment_main_activity, container, false);
		LinearLayout buttonLayout = (LinearLayout) rootView.findViewById(R.id.main_activity_fragmenet_layout);
		
		Button moodButton = (Button) inflater.inflate(R.layout.element_main_activity_show_mood_history_button, buttonLayout, false);
		buttonLayout.addView(moodButton);
		moodButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MoodHistoryActivity.class);
				startActivity(intent);
			}
		});
		
		View separator = inflater.inflate(R.layout.element_main_activity_button_separator, buttonLayout, false);
		buttonLayout.addView(separator);
		
		Button moodRelButton = (Button) inflater.inflate(R.layout.element_main_activity_button, buttonLayout, false);
		moodRelButton.setText("Drinking history");
		moodRelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), MoodRelationHistoryActivity.class);
				startActivity(intent);
			}
			
		});
		buttonLayout.addView(moodRelButton);
		
		
		View separator2 = inflater.inflate(R.layout.element_main_activity_button_separator, buttonLayout, false);
		buttonLayout.addView(separator2);
		
		Button achievementButton = (Button) inflater.inflate(R.layout.element_main_activity_button, buttonLayout, false);
		achievementButton.setTextColor(getActivity().getResources().getColor(R.color.blue));
		achievementButton.setShadowLayer(2, 1, 1, Color.CYAN);
		Drawable star = getActivity().getResources().getDrawable(R.drawable.star1_small);
		achievementButton.setCompoundDrawablesWithIntrinsicBounds(star, null, null, null);
		achievementButton.setText(Html.fromHtml("Achievement<br><small>Do not drink for 2 weeks</small>"));
		buttonLayout.addView(achievementButton);
		
		return rootView;
	}
}
