package org.apps8os.motivator.ui;

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
	
	private int mId;
	private Context mContext;
	
	public OpenEventDetailViewOnClickListener(int id, Context context) {
		super();
		mId = id;
		mContext = context;
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(mContext, EventDetailsActivity.class);
		intent.putExtra(EventDetailsActivity.KEY_EVENT_ID, mId);
		mContext.startActivity(intent);
	}

}
