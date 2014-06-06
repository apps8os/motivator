package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.viewpagerindicator.LinePageIndicator;

public class StartGuideActivity extends Activity {

	public static final String GUIDE_PAGE = "guide_page";
	
	private ViewPager mViewPager;
	private LinePageIndicator mIndicator;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_start_guide);
	    
	    mViewPager = (ViewPager) findViewById(R.id.question_activity_pager);
	    GuidePagerAdapter pagerAdapter = new GuidePagerAdapter(
	    		getFragmentManager());
	    mViewPager.setAdapter(pagerAdapter);
	    mViewPager.setOffscreenPageLimit(10);
	    
	    mIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mViewPager);
		
		setButtons();
	}
	
	/**
	 * Sets up the listeners for the buttons.
	 */
	private void setButtons() {
		final Resources res = getResources();
		final Button nextButton = (Button) findViewById(R.id.guide_next_button);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mViewPager.getCurrentItem() == 2) {
					Intent intent = new Intent(StartGuideActivity.super, StartingSprintActivity.class);
					startActivity(intent);
					finish();
				} else {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				}
			}
		});
		
		final Button previousButton = (Button) findViewById(R.id.guide_previous_button);
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
			}
		});
		previousButton.setEnabled(false);
		
		// Set up a page change listener to enable and disable buttons.
		mIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == 2) {
					nextButton.setText(getString(R.string.start_a_sprint));
					nextButton.setTextColor(res.getColor(R.color.actionbar_green));
				} else {
					nextButton.setText(getString(R.string.next));
					nextButton.setTextColor(res.getColor(R.color.dark_gray));
					nextButton.setEnabled(true);
				}
				if (arg0 == 0) {
					previousButton.setEnabled(false);
				} else {
					previousButton.setEnabled(true);
				}
			}
		});
	}
	
	
	private class GuidePagerAdapter extends FragmentPagerAdapter {

		public GuidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment fragment = new GuidePageFragment();
			Bundle args = new Bundle();
			args.putInt(GUIDE_PAGE, arg0);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			
			return 3;
		}
		
	}

}
