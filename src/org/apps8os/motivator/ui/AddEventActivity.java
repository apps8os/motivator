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

import java.util.ArrayList;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.EventDataHandler;
import org.apps8os.motivator.data.MotivatorEvent;
import org.apps8os.motivator.data.Question;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionItemTarget;
import com.viewpagerindicator.LinePageIndicator;

/**
 * An activity for adding an event. The questions are implemented as QuestionFragments.
 * @author Toni Järvinen
 *
 */
public class AddEventActivity extends Activity implements QuestionnaireActivityInterface {
	
	public static final String EVENT_ADDED = "event_added";
	public static final long ADD_NAME_HELP_ID = 10000;
	
	private ViewPager mViewPager;
	private EventDataHandler mEventDataHandler;
	private int mNumberOfQuestions;
	private String mName = "";
	private int mRequiredIds[] = {1000, 1001};
	private ArrayList<Integer> mCheckedIds = new ArrayList<Integer>();
	private Button mCompleteButton;

	private QuestionsPagerAdapter mQuestionsPagerAdapter;
	private LinePageIndicator titleIndicator;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_questions);
	    
	    mEventDataHandler = new EventDataHandler(this);
	    mNumberOfQuestions = mEventDataHandler.getAmountOfQuestions();
		
		getActionBar().setTitle(getString(R.string.add_event));
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_blue));
		
	    mViewPager = (ViewPager) findViewById(R.id.question_activity_pager);
	    mQuestionsPagerAdapter = new QuestionsPagerAdapter(
	    		getFragmentManager(), mNumberOfQuestions);
	    mViewPager.setAdapter(mQuestionsPagerAdapter);
	    mViewPager.setOffscreenPageLimit(10);
	    
	    titleIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		titleIndicator.setViewPager(mViewPager);
		setButtons();
	}
	
	/**
	 * Loading the action bar menu
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.questions, menu);
	    new ShowcaseView.Builder(this, true)
	    .setTarget(new ActionItemTarget(this, R.id.questions_add_comment))
	    .setContentTitle("Lisää suunnitelmalle nimi")
	    .setContentText("Täältä voit lisätä suunnitelmallesi nimen, josta tunnistat sen.")
	    .hideOnTouchOutside()
	    .setStyle(R.style.ShowcaseView)
	    .singleShot(ADD_NAME_HELP_ID)
	    .build();
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Actions for the menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Adding the name for the event/plan
		if (item.getItemId() == R.id.questions_add_comment) {
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(getString(R.string.name_the_event));
			// Create TextView
			LinearLayout rootElement = (LinearLayout)  getLayoutInflater().inflate(R.layout.element_comment_edit_text, null);
			final EditText input = (EditText) rootElement.getChildAt(0);
			input.setText(mName);
			alert.setView(rootElement);

			alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				    mName = input.getText().toString();
				    getActionBar().setTitle(mName);
				  }
				});
	
				  alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
				  public void onClick(DialogInterface dialog, int whichButton) {
				      // Canceled.
				  }
			});
			alert.show();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Sets up the listeners for the buttons.
	 */
	private void setButtons() {
		final Button nextButton = (Button) findViewById(R.id.questions_next_button);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mViewPager.getCurrentItem() == 1 && mQuestionsPagerAdapter.getFragment(1).getSelectedAnswer() == 1) {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 3);
				} else {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
				}
			}
		});
		
		final Button previousButton = (Button) findViewById(R.id.questions_previous_button);
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mViewPager.getCurrentItem() == 4 && mQuestionsPagerAdapter.getFragment(1).getSelectedAnswer() == 1) {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 3);
				} else {
					mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
				}
			}
		});
		mCompleteButton = (Button) findViewById(R.id.questions_complete_button);
		mCompleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int answers[]  = new int[mNumberOfQuestions];
				long date = 0L;
				for (int i = 0; i < mNumberOfQuestions; i++) {
					answers[i] = mQuestionsPagerAdapter.getFragment(i).getSelectedAnswer();
					if (i == 0) {
						if (answers[i] == 3) {
							date = mQuestionsPagerAdapter.getFragment(i).getSelectedDate();
						}
					}
				}
				mEventDataHandler.insertEvent(answers[0], answers[1], answers[2], answers[3], answers[4], mName, date);
				
				View toastLayout = (View) getLayoutInflater().inflate(R.layout.element_mood_toast, (ViewGroup) findViewById(R.id.mood_toast_layout));
				TextView toastText = (TextView) toastLayout.findViewById(R.id.mood_toast_text);
				toastText.setText(getString(R.string.event_added));
				toastText.setTextColor(Color.WHITE);
				
				Toast questionnaireDone = new Toast(getApplicationContext());
				questionnaireDone.setDuration(Toast.LENGTH_SHORT);
				questionnaireDone.setView(toastLayout);
				questionnaireDone.show();
				
				//Add a flag for which section the event was added to.
				SharedPreferences motivatorPrefs = getSharedPreferences(MainActivity.MOTIVATOR_PREFS, 0);
				Editor editor = motivatorPrefs.edit();
				if (answers[0] == 1) {
					editor.putInt(EVENT_ADDED, MotivatorEvent.TODAY);
				} else {
					editor.putInt(EVENT_ADDED, MotivatorEvent.PLAN);
				}
				editor.commit();
				finish();
			}
		});
		// Disable these buttons at start.
		mCompleteButton.setEnabled(false);
		previousButton.setEnabled(false);
		
		// Set up a page change listener to enable and disable buttons.
		titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			@Override
			public void onPageSelected(int arg0) {
				if (arg0 == mNumberOfQuestions - 1) {
					nextButton.setEnabled(false);
				} else {
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
	
	/**
	 * Method for the fragments to inform that the user has selected something on question with the given id.
	 */
	@Override
	public void setChecked(int id) {
		mCheckedIds.add(id);
		boolean allRequiredChecked = true;
		for (int i = 0; i < mRequiredIds.length; i++) {
			if (!mCheckedIds.contains(mRequiredIds[i])) {
				allRequiredChecked = false;
			}
		}
		if (allRequiredChecked) {
			mCompleteButton.setEnabled(true);
		}
	}
	
	/**
	 * Represents a pager adapter for the questions. The fragments are saved in a SparseArray so that we can
	 * reference the specific fragments.
	 * @author Toni Järvinen
	 *
	 */
	private class QuestionsPagerAdapter extends FragmentPagerAdapter {
		
		private SparseArray<QuestionFragment> fragments = new SparseArray<QuestionFragment>();
		private int mQuestions;
		
		public QuestionsPagerAdapter(FragmentManager fm, int numberOfQuestions) {
			super(fm);
			mQuestions = numberOfQuestions;
		}

		/**
		 * Instantiates the item at the given position.
		 */
		@Override
		public Fragment getItem(int arg0) {
			Fragment questionFragment = new QuestionFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable(Question.QUESTION, mEventDataHandler.getQuestion(1000 + arg0));
			questionFragment.setArguments(bundle);
			return questionFragment;
		}
		/**
		 * Instantiates the question and adds it to the SparseArray.
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			QuestionFragment fragment = (QuestionFragment) super.instantiateItem(container, position);
			fragments.put(position, fragment);
			return fragment;
		}
		
		@Override
	    public void destroyItem(ViewGroup container, int position, Object object) {
	        fragments.remove(position);
	        super.destroyItem(container, position, object);
	    }

		@Override
		public int getCount() {
			return mQuestions;
		}
		
		/**
		 * Get the fragment in given position.
		 * @param position
		 * @return
		 */
		public QuestionFragment getFragment(int position) {
			return fragments.get(position);
		}
	}

}
