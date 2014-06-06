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
import org.apps8os.motivator.data.EventDataHandler;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Settings fragment for the SettingsActivity.
 * @author Toni Järvinen
 *
 */
public class SettingsFragment extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		Preference button = (Preference)findPreference("remove_all_data");
		button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		                @Override
		                public boolean onPreferenceClick(Preference arg0) { 
		                	
		                	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
							builder.setTitle(getString(R.string.delete_all_data))
								.setMessage(getString(R.string.delete_all_data_warning))
								.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
				                    EventDataHandler dataHandler = new EventDataHandler(getActivity());
				                    dataHandler.deleteAllData(getActivity());
				                    
				                    View toastLayout = (View) getActivity().getLayoutInflater()
				                    		.inflate(R.layout.element_mood_toast, (ViewGroup) getActivity().findViewById(R.id.mood_toast_layout));
									TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
									toastText.setText(getString(R.string.all_data_deleted));
									toastText.setTextColor(Color.WHITE);
									
									Toast dataDeleted = new Toast(getActivity());
									dataDeleted.setDuration(Toast.LENGTH_SHORT);
									dataDeleted.setView(toastLayout);
									dataDeleted.show();
									
								}
							}).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
							Dialog dialog = builder.create();
							dialog.show();
		                    return true;
		                }
		            });
	}

}
