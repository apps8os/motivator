package org.apps8os.motivator.services;

import org.apps8os.motivator.R;
import org.apps8os.motivator.ui.MoodQuestionActivity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Represents a simple notification service for sending notifications.
 * @author Toni Järvinen
 *
 */
public class NotificationService extends Service {
	
	public final static int NOTIFICATION_ID_MOOD = 10;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_STICKY;
	}

	
	private void handleCommand(Intent intent) {
		// Set up the notification with a builder
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.today_screen_mood));
		builder.setSmallIcon(R.drawable.ic_launcher);
		builder.setTicker(getString(R.string.today_screen_mood));
		// Remove the notification when the user clicks it.
		builder.setAutoCancel(true);
		
		// Where to go when user clicks the notification
		Intent resultIntent = new Intent(this, MoodQuestionActivity.class);
		// Preserve the normal navigation of the app by adding the parent stack of the result activity
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MoodQuestionActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent pendingResultIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingResultIntent);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(NOTIFICATION_ID_MOOD, builder.build());
		
		// Stop the service after the notification has been sent
		stopSelf();
	}

}
