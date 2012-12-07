package com.danandrei.facebookbirthdays;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Stepone extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.stepone);

		Button btn_back = (Button) findViewById(R.id.button_back);

		btn_back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				finish();

			}
		});

	}

}
