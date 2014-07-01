package org.apps8os.motivator.ui;

import org.apps8os.motivator.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class IntroActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_intro);
	    
	    Button okButton = (Button) findViewById(R.id.ok_button);
	    
	    final Context context = this;
	    
	    okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, MainActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); 
				startActivity(intent);
			}
	    	
	    });
	}

}
