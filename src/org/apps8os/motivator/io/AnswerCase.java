package org.apps8os.motivator.io;

/**
 * Represents an answering case. Used to draw buttons in UI. Only holds answering id for recognizing the instance
 * and the text to be written to the button.
 * @author Toni JŠrvinen
 *
 */
public class AnswerCase {
	
	private int mAnsweringId;
	private String mButtonText;
	
	public AnswerCase(int answeringId, String buttonText) {
		mAnsweringId = answeringId;
		mButtonText = buttonText;
	}
	
	public int getAnsweringId() {
		return mAnsweringId;
	}
	
	public String getButtonText() {
		return mButtonText;
	}


}
