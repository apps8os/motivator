package org.apps8os.motivator.ui;

import java.util.ArrayList;

import org.apps8os.motivator.R;
import org.apps8os.motivator.data.GoalDataHandler;
import org.apps8os.motivator.data.MotivatorDatabaseHelper;
import org.apps8os.motivator.data.Question;

import com.viewpagerindicator.LinePageIndicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class AddGoalActivity extends Activity implements QuestionnaireActivityInterface {
	
	private ViewPager mViewPager;
	private GoalDataHandler mDataHandler;
	private int mNumberOfQuestions;
	private String mName = "";
	private int mQuestionId;
	private int mAnswersId;
	private int mRequiredIds[] = {3000, 3001};
	private ArrayList<Integer> mCheckedIds = new ArrayList<Integer>();
	private Button mCompleteButton;

	private QuestionsPagerAdapter mQuestionsPagerAdapter;
	private LinePageIndicator titleIndicator;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_questions);
	    
	    mDataHandler = new GoalDataHandler(this);
	    mNumberOfQuestions = mDataHandler.getAmountOfQuestions();
		mQuestionId = mDataHandler.getFirstQuestionId() - 1;
		
		getActionBar().setTitle(getString(R.string.add_goal));
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.action_bar_blue));
		
	    mViewPager = (ViewPager) findViewById(R.id.question_activity_pager);
	    mQuestionsPagerAdapter = new QuestionsPagerAdapter(
	    		getFragmentManager(), mNumberOfQuestions);
	    mViewPager.setAdapter(mQuestionsPagerAdapter);
	    mViewPager.setOffscreenPageLimit(10);
	    
	    mAnswersId = incrementAnswersId();
	    
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
		return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Actions for the menu items.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
			}
		});
		
		final Button previousButton = (Button) findViewById(R.id.questions_previous_button);
		previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
			}
		});
		mCompleteButton = (Button) findViewById(R.id.questions_complete_button);
		mCompleteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int answers[]  = new int[mNumberOfQuestions];
				for (int i = 0; i < mNumberOfQuestions; i++) {
					answers[i] = mQuestionsPagerAdapter.getFragment(i).getAnswer();
				}
				//TODO: Insert goal
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
	 * Incrementing the running id for an answering instance.
	 * @return
	 */
	private int incrementAnswersId() {
		// Use SharedPreferences to store the answers id so that it can be incremented even if the app is killed
		SharedPreferences answerIdIncrement = getSharedPreferences(MotivatorDatabaseHelper.ANSWER_ID_INCREMENT_PREFS, 0);
		int answerId = answerIdIncrement.getInt(MotivatorDatabaseHelper.ANSWER_ID, 1);
		SharedPreferences.Editor editor = answerIdIncrement.edit();
		editor.putInt(MotivatorDatabaseHelper.ANSWER_ID, answerId + 1);
		editor.commit();
		return answerId;
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

		@Override
		public Fragment getItem(int arg0) {
			Fragment questionFragment = new QuestionFragment();
			Bundle bundle = new Bundle();
			bundle.putParcelable(Question.QUESTION, mDataHandler.getQuestion(3000 + arg0));
			questionFragment.setArguments(bundle);
			return questionFragment;
		}
		
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
