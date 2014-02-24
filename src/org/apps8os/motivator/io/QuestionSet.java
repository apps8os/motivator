package org.apps8os.motivator.io;

import java.util.Arrays;

import org.apps8os.motivator.R;

import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;

/**
 * A class for holding all the questions as Question objects. Implemented as a singleton because we want
 * to share the same instance across activities and the constructor is relatively expensive.
 * @author Toni JŠrvinen
 *
 */

public class QuestionSet {
	
	// The sets of questions as SparseArrays, which allow fetching with int id.
	private SparseArray<Question> mMoodQuestions;
	private SparseArray<Question> mEventQuestions;
	private SparseArray<Question> mGoalQuestions;
	
	private static QuestionSet mQuestionSet;
	
	private QuestionSet(Context context) {
		mMoodQuestions = new SparseArray<Question>();
		mEventQuestions = new SparseArray<Question>();
		mGoalQuestions = new SparseArray<Question>();
		Resources res = context.getResources();
		
		// The limit for the loops, which is the amount of questions.
		int limit = res.getInteger(R.integer.mood_question_amount);
		
		// Inserting the questions to the SpareArrays.
		for (int i = 1; i <= limit; i++) {
			// String array of questions
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier("mood_question" + i, "array" , context.getPackageName()));
			int id = i;
			
			// Creation of new Question object and inserting it to the array.
			Question question = new Question(id, questionAndAnswers[0], Arrays.copyOfRange(questionAndAnswers, 1, questionAndAnswers.length));
			mMoodQuestions.put(id, question);
		}
		
		limit = res.getInteger(R.integer.adding_event_amount);
		for (int i = 1; i <= limit; i++) {
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier("adding_event" + i, "array" , context.getPackageName()));
			int id = i;
			Question question = new Question(id, questionAndAnswers[0], Arrays.copyOfRange(questionAndAnswers, 1, questionAndAnswers.length));
			mEventQuestions.put(id, question);
		}
		
		limit = res.getInteger(R.integer.adding_goal_amount);
		for (int i = 1; i <= limit; i++) {
			String[] questionAndAnswers = res.getStringArray(res.getIdentifier("adding_goal" + i, "array" , context.getPackageName()));
			int id = i;
			Question question = new Question(id, questionAndAnswers[0], Arrays.copyOfRange(questionAndAnswers, 1, questionAndAnswers.length));
			mGoalQuestions.put(id, question);
		}
	}
	
	public static QuestionSet getInstance(Context context) {
		if (mQuestionSet == null) {
			mQuestionSet = new QuestionSet(context);
		}
		return mQuestionSet;
	}

	public Question getMoodQuestion(int id) {
		return mMoodQuestions.get(id);
	}

	public Question getEventQuestion(int id) {
		return mEventQuestions.get(id);
	}

	public Question getGoalQuestion(int id) {
		return mGoalQuestions.get(id);
	}

}
