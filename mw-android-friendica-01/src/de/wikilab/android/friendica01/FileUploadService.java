package de.wikilab.android.friendica01;

/* This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;

public class FileUploadService extends IntentService {
	/** Target file name */
	public static final String EXTRA_DESCTEXT = "net.teamwiki.clip.FileUploadService.EXTRA_DESCTEXT";

	private static final int UPLOAD_SUCCESS_ID = 1;
	private static final int UPLOAD_FAILED_ID = 2;
	private static final int UPLOAD_PROGRESS_ID = 3;

	private String descText, subject;
	private Uri fileToUpload;

	private Friendica friendicaAbstraction = null;

	public FileUploadService() {
		super("Andfrnd_FileUploadService");
		friendicaAbstraction = new Friendica(this);
	}

	private void showFailMsg(Context ctx, String txt) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Instantiate the Notification:
		CharSequence tickerText = "Upload failed, please retry!";
		Notification notification = new Notification(R.drawable.arrow_up, tickerText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		CharSequence contentTitle = "Upload failed, click to retry!";
		CharSequence contentText = txt;
		Intent notificationIntent = new Intent(this, FriendicaImgUploadActivity.class);
		Bundle b = new Bundle();
		b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);
		b.putString(Intent.EXTRA_SUBJECT, subject);
		b.putString(EXTRA_DESCTEXT, descText);

		notificationIntent.putExtras(b);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		// Pass the Notification to the NotificationManager:
		mNotificationManager.notify(UPLOAD_FAILED_ID, notification);
	}

	private void showSuccessMsg(Context ctx) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Instantiate the Notification:
		Integer icon = R.drawable.arrow_up;
		CharSequence tickerText = "Upload succeeded!";
		Long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		PendingIntent nullIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notification.setLatestEventInfo(context, tickerText, "Click to dismiss", nullIntent);

		// Pass the Notification to the NotificationManager:

		mNotificationManager.notify(UPLOAD_SUCCESS_ID, notification);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// Instantiate the Notification:
		CharSequence tickerText = "Uploading...";
		Notification notification = new Notification(R.drawable.arrow_up, tickerText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		// Define the Notification's expanded message and Intent:
		Context context = getApplicationContext();
		PendingIntent nullIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notification.setLatestEventInfo(context, "Upload in progress...", "You are notified here when it completes", nullIntent);

		// Pass the Notification to the NotificationManager:
		mNotificationManager.notify(UPLOAD_PROGRESS_ID, notification);

		Bundle intentPara = intent.getExtras();
		descText = intentPara.getString(EXTRA_DESCTEXT);
		subject = intentPara.getString(Intent.EXTRA_SUBJECT);

		HashMap<String, String> postData = new HashMap<String, String>();
		postData.put("title", "subject");
		postData.put("status", "descText");

		fileToUpload = (Uri) intentPara.getParcelable(Intent.EXTRA_STREAM);
		ArrayList<Uri> files = new ArrayList<Uri>();
		files.add(fileToUpload);

		friendicaAbstraction.postPost(subject, postData, files, new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				TwAjax uploader = result.getProcessor();
				JSONObject jsonResult = null;
				jsonResult = result.getResult().get(0);
				mNotificationManager.cancel(UPLOAD_PROGRESS_ID);
				if (uploader.isSuccess() && uploader.getError() == null) {
					try {
						String postedText = jsonResult.getString("text");
						showSuccessMsg(FileUploadService.this);

					} catch (Exception e) {
						String errMes = e.getMessage() + " | " + uploader.getResult();
						if (jsonResult != null)
							try {
								errMes = jsonResult.getString("error");
							} catch (JSONException je) {
							}

						showFailMsg(FileUploadService.this, errMes);

						e.printStackTrace();
					}
				} else if (uploader.getError() != null) {
					showFailMsg(FileUploadService.this, uploader.getError().toString());
				} else {
					showFailMsg(FileUploadService.this, uploader.getResult());
				}
			}
		});

	}
}