package com.danandrei.facebookbirthdays;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.apache.http.util.ByteArrayBuffer;
import com.google.ads.AdRequest;
import com.google.ads.AdView;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender.OnFinished;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.os.AsyncTask;

public class Main extends Activity {

	private String birthdays_url = "";
	private String birthday_source = "";
	private EditText et_birthdays_url;
	private CheckBox chk_reminder, ckc_oneday;
	private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		chk_reminder = (CheckBox) findViewById(R.id.checkBox_reminder);
		ckc_oneday = (CheckBox) findViewById(R.id.checkBox_oneday);

		et_birthdays_url = (EditText) findViewById(R.id.editText_birthdays_url);

		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

		Button btn_process = (Button) findViewById(R.id.button_process);
		btn_process.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				process();
			}
		});

		Button btn_step1 = (Button) findViewById(R.id.button_step1);

		btn_step1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(Main.this, Stepone.class);
				startActivity(i);
			}
		});

		Button btn_undo = (Button) findViewById(R.id.button_revert);

		btn_undo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				new UndoOperation().execute("");

		

			}
		});

	}

	protected void process() {

		new ProcessOperation().execute("");
		

	}

	public String DownloadFromUrl(String fileURL, String fileName) { // this is
																		// the
																		// downloader
																		// method
		try {
			URL url = new URL(fileURL);

			String PATH = "/sdcard/";
			new File(PATH + fileName);

			System.currentTimeMillis();
			Log.d("fbbirthdays", "download begining");
			Log.d("fbbirthdays", "download url:" + url);
			Log.d("fbbirthdays", "downloaded file name:" + fileName + " to "
					+ PATH + fileName);
			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(50);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			String birthday_source = new String(baf.toByteArray());

			return birthday_source;

			/*
			 * //no need to save it FileOutputStream fos = new
			 * FileOutputStream(file); fos.write(baf.toByteArray());
			 * fos.close(); Log.d("fbbirthdays", "download ready in " +
			 * ((System.currentTimeMillis() - startTime) / 1000) + " sec");
			 */

		} catch (IOException e) {
			Log.d("fbbirthdays", "Error: " + e);
			Toast.makeText(
					getApplicationContext(),
					"There's been an error. Are you sure you followed the exact steps ? Are you connected to the internet ?",
					Toast.LENGTH_LONG).show();
			return "";
		}

	}

	private class ProcessOperation extends AsyncTask<String, Void, String> {

		public ProgressDialog dialog;

		@Override
		protected String doInBackground(String... params) {

			birthdays_url = et_birthdays_url.getText().toString();
			birthdays_url = birthdays_url.replaceAll("webcal://", "http://");

			birthday_source = DownloadFromUrl(birthdays_url,
					"facebook_birthdays.txt");

			if (birthday_source != "") {

				// Log.v("source",birthday_source);

				String split[] = birthday_source.split("\n");

				int i = 2;
				while (i < split.length - 11) {

					i = i + 7;

					String dt_start = split[i].replaceAll("DTSTART", "");

					String year = dt_start.substring(1, 5);
					String month = dt_start.substring(5, 7);
					String day = dt_start.substring(7, 9);

					long start_event_milis = 0;
					SimpleDateFormat formatter = new SimpleDateFormat(
							"dd LL yyyy");
					try {
						String temp = day + " " + month + " " + year;
						Date start_event = formatter.parse(temp);
						Calendar c = Calendar.getInstance();
						c.setTime(start_event);

						start_event_milis = c.getTimeInMillis() + 86400000; // +1
																			// day
																			// ajustment
						if (ckc_oneday.isChecked())
							start_event_milis = c.getTimeInMillis();

					} catch (Exception e) {
						e.printStackTrace();
					}

					String dt_summary = split[i + 1].replaceAll("SUMMARY:", "");
					String dt_uid = split[i + 4].replaceAll("UID:b", "");
					dt_uid = dt_uid.replaceAll("@facebook.com", "");
					// Log.v("i",dt_start + " : " + dt_summary + " : "+ dt_uid);

					try {

						Uri eventsUri;
						if (android.os.Build.VERSION.SDK_INT <= 7) {

							eventsUri = Uri.parse("content://calendar/events");
						} else {

							eventsUri = Uri
									.parse("content://com.android.calendar/events");
						}

						Calendar.getInstance();
						ContentValues event = new ContentValues();
						event.put("calendar_id", 1);
						event.put("title", dt_summary);
						event.put("description",
								"BIRTHDAY http://www.facebook.com/profile.php?id="
										+ dt_uid);
						event.put("dtstart", start_event_milis);
						event.put("rrule", "FREQ=YEARLY");
						event.put("allDay", 1); // 0 for false, 1 for true
						event.put("eventStatus", 1);

						if (chk_reminder.isChecked()) {
							event.put("hasAlarm", 1);
						} else
							event.put("hasAlarm", 0);
						event.put("duration", "P3600S");
						Uri url = getContentResolver().insert(eventsUri, event);

						if (chk_reminder.isChecked()) {

							// reminder insert
							Uri REMINDERS_URI = Uri
									.parse("content://com.android.calendar/reminders");
							ContentValues values = new ContentValues();
							values.put("event_id", url.getLastPathSegment()
									.toLowerCase());
							// Log.v("event id is",String.valueOf(url.getLastPathSegment().toLowerCase()));
							values.put("method", 1);
							values.put("minutes", 10);
							getContentResolver().insert(REMINDERS_URI, values);

						}

					} catch (Exception ex) {
						Log.e("Exception", ex.toString());
					}
				}

			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			Toast.makeText(getApplicationContext(),
					"Birthdays added successfully", Toast.LENGTH_SHORT)
					.show();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {

			dialog = ProgressDialog.show(Main.this, "",
					"Processing. Please wait...", true);
			dialog.show();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			// Things to be done while execution of l ong running operation is in
			// progress. For example updating ProgessDialog
		}
	}

	private class UndoOperation extends AsyncTask<String, Void, String> {

		public ProgressDialog dialog;

		@Override
		protected String doInBackground(String... params) {

			birthdays_url = et_birthdays_url.getText().toString();
			birthdays_url = birthdays_url.replaceAll("webcal://", "http://");

			birthday_source = DownloadFromUrl(birthdays_url,
					"facebook_birthdays.txt");

			if (birthday_source != "") {

				String split[] = birthday_source.split("\n");

				int i = 2;
				while (i < split.length - 11) {

					i = i + 7;

					split[i + 1].replaceAll("SUMMARY:", "");

					String dt_uid = split[i + 4].replaceAll("UID:b", "");
					dt_uid = dt_uid.replaceAll("@facebook.com", "");

					// Log.v("url to delete is: ",String.valueOf(1)
					// +"BIRTHDAY http://facebook.com/profile.php?id=" +
					// dt_uid);

					getContentResolver().delete(
							Uri.parse("content://com.android.calendar/events"),
							"description LIKE ?", new String[] { "BIRTHDAY%" });
				}

			}

			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			
			Toast.makeText(getApplicationContext(),
					"Birthdays deleted successfully", Toast.LENGTH_SHORT)
					.show();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {

			dialog = ProgressDialog.show(Main.this, "",
					"Deleting. Please wait...", true);
			dialog.show();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Void... values) {
			// Things to be done while execution of long running operation is in
			// progress. For example updating ProgessDialog
		}
	}

}
