package com.thalmichackd.myowhip;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class NextScreen extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.next_activity);
		
		// receive the arguments from the previous Activity
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			return;
		}

		// assign the values to string-arguments
		String punch = extras.getString("punch");

		TextView punchText = (TextView) findViewById(R.id.punchText);
		punchText.setText(punch);

		/*
		// assign the values to string-arguments
		String pass = extras.getString("pass");
		String phone = extras.getString("phone");

		TextView passText = (TextView) findViewById(R.id.passText);
		passText.setText(pass);

		TextView phoneText = (TextView) findViewById(R.id.phoneText);
		phoneText.setText(phone);
		 */

	}
	
}
