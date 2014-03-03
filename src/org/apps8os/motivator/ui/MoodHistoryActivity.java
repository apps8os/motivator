package org.apps8os.motivator.ui;



import org.apps8os.motivator.R;
import org.apps8os.motivator.data.MoodDataHandler;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Represents the mood history of the user
 * @author Toni Järvinen
 *
 */

public class MoodHistoryActivity extends Activity {
	
	private MoodDataHandler mDataHandler;
	
	private static final int MAX_IMAGE_SIZE = 250;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_history);
	    Resources res = getResources();
	    
	    // TODO: Replace below with correct implementation
	    
	    mDataHandler = new MoodDataHandler(this);
	    mDataHandler.open();
	    float moodAmount = mDataHandler.getMoodAmount();
	    float goodMoodAmount = mDataHandler.getGoodMoodsAmount();
	    
	    if (moodAmount == 0) {
	    	moodAmount = 1;
	    }
	    
	    // Scale for the display
        final float scale = res.getDisplayMetrics().density;
        int maxSize = (int) (MAX_IMAGE_SIZE * scale + 0.5f);
        
        // Get a ration from 0.0 to 1.0 of how many times the user was in good mood.
        float goodMoodRatio = (goodMoodAmount / moodAmount);
        
        // Set the image width with the ratio
	    int imageWidth = (int) (goodMoodRatio * maxSize);
	    
	    ImageView mood1 = new ImageView(this);
	    mood1.setImageDrawable(res.getDrawable(R.drawable.temp_emoticon));
	    LayoutParams imageParams = new LayoutParams(imageWidth, imageWidth);
	    mood1.setLayoutParams(imageParams);
	    
	    // Scale the location based on the ratio
	    mood1.setX(50/goodMoodRatio * scale);
	    mood1.setY(100 * scale);
	    
	    RelativeLayout view = (RelativeLayout) findViewById(R.id.activity_mood_history);
	    view.addView(mood1);
	    
	    // Do the same as above but for not good moods.
	    imageWidth = (int) ((1 - goodMoodRatio) * maxSize);
	    ImageView mood2 = new ImageView(this);
	    mood2.setImageDrawable(res.getDrawable(R.drawable.ic_launcher));
	    imageParams = new LayoutParams(imageWidth, imageWidth);
	    mood2.setLayoutParams(imageParams);
	    mood2.setX(50/(1 - goodMoodRatio) * scale);
	    mood2.setY(300 * scale);
	   
	    view.addView(mood2);
	}
	
	public void onResume() {
		if (!mDataHandler.isOpen()) {
			mDataHandler.open();
		}
		super.onResume();
	}
	
	public void onStop() {
		mDataHandler.close();
		super.onStop();
	}

}
