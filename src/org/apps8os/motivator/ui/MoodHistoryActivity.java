package org.apps8os.motivator.ui;



import org.apps8os.motivator.R;
import org.apps8os.motivator.data.MoodDataHandler;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Represents the mood history of the user by day.
 * @author Toni Järvinen
 *
 */

public class MoodHistoryActivity extends Activity {
	
	private MoodDataHandler mDataHandler;
	private LayoutInflater mInflater;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_mood_history);
	    
	    mInflater = getLayoutInflater();
	    ViewPager viewPager = (ViewPager) findViewById(R.id.activity_mood_history_viewpager);
	    DatePagerAdapter pagerAdapter = new DatePagerAdapter();
	    
	    viewPager.setAdapter(pagerAdapter);
	    
	    viewPager.setCurrentItem(2);
	    
	    
	}
	
	private class DatePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 3;
		}
		
		@Override
		public Object instantiateItem(ViewGroup viewGroup, int position) {
			ScrollView dateLayout = (ScrollView) mInflater.inflate(R.layout.element_mood_history_day_view, null);
			LinearLayout innerLayout = (LinearLayout) dateLayout.getChildAt(0);
			TextView title = (TextView) innerLayout.getChildAt(0);
			title.setText("21.03.2013");
			TextView comment = (TextView) innerLayout.getChildAt(2);
			comment.setText("Paras päivä koskaan!");
			viewGroup.addView(dateLayout);
			return dateLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
		
		@Override
		public void destroyItem(ViewGroup viewGroup, int position, Object object) {
			viewGroup.removeView((View) object);
		}
		
	}

}
