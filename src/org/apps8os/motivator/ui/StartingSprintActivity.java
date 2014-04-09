package org.apps8os.motivator.ui;

import java.util.concurrent.TimeUnit;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.MoodDataHandler;
import org.apps8os.motivator.utils.MotivatorConstants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class StartingSprintActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_starting_sprint);
		
		Button button = (Button) findViewById(R.id.start_sprint_button);
		final Activity activity = this;
		
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				long startDayInMillis = System.currentTimeMillis();
				EditText numberOfDays = (EditText) findViewById(R.id.number_of_days_edit);
				EditText sprintTitle = (EditText) findViewById(R.id.name_of_sprint_edit);
				MoodDataHandler mDataHandler = new MoodDataHandler(activity);
				mDataHandler.insertSprint(startDayInMillis, Integer.parseInt(numberOfDays.getText().toString()), sprintTitle.getText().toString());
				
				SharedPreferences motivatorPrefs = getSharedPreferences(MotivatorConstants.MOTIVATOR_PREFS, 0);
				SharedPreferences.Editor editor = motivatorPrefs.edit();
				editor.putBoolean(MotivatorConstants.FIRST_SPRINT_SET, true);
				editor.commit();
				
				Intent intent = new Intent(activity, MainActivity.class);
				startActivity(intent);
				activity.finish();
			}
			
		});
	}

}
