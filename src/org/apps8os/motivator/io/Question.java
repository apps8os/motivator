package org.apps8os.motivator.io;


/**
 * Represents a Question. Used to store the question information during runtime.
 * @author Toni JŠrvinen
 *
 */
public class Question {
	
	private int mId;
	private String mQuestion;
	private String[] mAnswers;
	
	public Question(int id, String question, String[] answers) {
		mId = id;
		mQuestion = question;
		mAnswers = answers;
	}

	public int getId() {
		return mId;
	}

	public String getQuestion() {
		return mQuestion;
	}

	public String getAnswer(int number) {
		return mAnswers[number];
	}
	
	public int getAnswerCount() {
		return mAnswers.length;
	}
	
}
