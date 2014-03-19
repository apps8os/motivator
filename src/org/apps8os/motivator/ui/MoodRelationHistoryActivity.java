package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class MoodRelationHistoryActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    SpinnerAdapter adapter = ArrayAdapter.createFromResource(this, R.array.time_frames, android.R.layout.simple_spinner_dropdown_item);
	    
	    ActionBar bar = getActionBar();
	    bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	    
	    OnNavigationListener listener = new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition,
					long itemId) {
				return false;
			}
	    	
	    };
	    
	    bar.setDisplayShowTitleEnabled(false);
	    bar.setListNavigationCallbacks(adapter, listener);
	    
	    
	}

}
