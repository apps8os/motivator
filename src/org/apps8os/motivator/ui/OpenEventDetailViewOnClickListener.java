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

import org.apps8os.motivator.data.MotivatorEvent;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Opens event detail view activity with given event id.
 * @author Toni Järvinen
 *
 */
public class OpenEventDetailViewOnClickListener implements OnClickListener {
	
	private MotivatorEvent mEvent;
	private Context mContext;
	private int mSection;
	
	/**
	 * Create a listener for the event provided.
	 * @param event
	 * @param context
	 */
	public OpenEventDetailViewOnClickListener(MotivatorEvent event, Context context, int section) {
		super();
		mEvent =event;
		mContext = context;
		mSection = section;
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(mContext, EventDetailsActivity.class);
		intent.putExtra(MotivatorEvent.EVENT, mEvent);
		intent.putExtra(MotivatorEvent.SECTION, mSection);
		mContext.startActivity(intent);
	}

}
