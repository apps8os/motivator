package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Information activity where the user is given relevant information about the application
 * @author Toni Järvinen
 *
 */
public class InfoActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_info);
	    ((TextView) findViewById(R.id.top_paragraph)).setText(Html.fromHtml(getString(R.string.info_first_section)));
	    ((TextView) findViewById(R.id.middle_paragraph)).setText(Html.fromHtml(getString(R.string.info_second_section)));
	    ((TextView) findViewById(R.id.bottom_paragraph)).setText(Html.fromHtml(getString(R.string.info_third_section)));
	    Button feedbackButton = (Button) findViewById(R.id.feedback_button);
	    final Context context = this;
	    
	    feedbackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Set up a dialog with info on where to get help if user answers everything is not ok
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				LinearLayout helpDialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.element_feedback_popup, null);
				builder.setTitle("Anna palautetta");
				builder.setView(helpDialogLayout);
				builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog,
							int which) {
					}
				});
				Dialog helpDialog = builder.create();
				helpDialog.show();
			}
	    });
	    
	    PackageInfo pInfo = null;
	    String version = "";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (pInfo != null) {
			version = pInfo.versionName;
		}
		
		((TextView) findViewById(R.id.version_number)).setText(getString(R.string.app_version_number) + " " + version);
	}
	
	/**
	 * Opens the feedback webpage
	 * @param view
	 */
	public void sendFeedback(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.webropolsurveys.com/S/2F46C816879B346B.par"));
		startActivity(browserIntent);
	}
	
	/**
	 * Opens the usability questionnaire webpage
	 * @param view
	 */
	public void susForm(View view) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://bit.ly/SQeCm1"));
		startActivity(browserIntent);
	}

}
