package com.danandrei.facebookbirthdays;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class About extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about);

		Button rate = (Button) findViewById(R.id.button_rate);

		rate.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {

				Toast.makeText(getApplicationContext(), "Choose internet", Toast.LENGTH_SHORT).show();
				
				Intent browse = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("https://market.android.com/details?id=com.danandrei.facebookbirthdays"));
				startActivity(browse);

			}
		});

		Button back = (Button) findViewById(R.id.button_back);

		back.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});

	}

}
