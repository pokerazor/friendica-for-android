package de.wikilab.android.friendica01;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.JsonFinishReaction;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica.ResultObject;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Tools;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon.TimelineEventMapActivity;
import de.wikilab.android.friendica01.R.id;

public class PostBarModule {

	private static final int UPLOAD_SUCCESS_ID = 1;
	private static final int UPLOAD_FAILED_ID = 2;
	private static final int UPLOAD_PROGRESS_ID = 3;

	protected Object parent = null;
	protected Activity parentActivity = null;
	protected ContentFragment parentFragment = null;

	protected ImageButton submitBtn = null;
	protected ImageButton pickPhotoBtn = null;
	protected ImageButton takePhotoBtn = null;

	protected EditText txtStatusTitle = null;
	protected EditText txtStatusBody = null;

	protected ToggleButton geoTgl = null;

	protected ImageView previwImg = null;

	protected Boolean locationListenerAttached = false;
	protected TextView viewLatLon = null;
	protected View myView = null;

	protected LocationManager locationManager = null;
	protected Location location = null;

	public File takePhotoTarget = null;

	private NotificationManager mNotificationManager = null;

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			PostBarModule.this.location = location;
			displayLocation();
		}

		@Override
		public void onProviderDisabled(String provider) {
			viewLatLon.setText(getString(R.string.viewLatLon) + "\nDisabled");
		}

		@Override
		public void onProviderEnabled(String provider) {
			viewLatLon.setText(getString(R.string.viewLatLon) + "\nEnabled");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			viewLatLon.setText(getString(R.string.viewLatLon) + "\nStatus=" + String.valueOf(status));
		}
	};
	private Uri fileToUpload;

	public PostBarModule(Object parent) {
		this.parent = parent;
		if (this.parent instanceof Activity) {
			parentActivity = (Activity) parent;
		} else if (this.parent instanceof ContentFragment) {
			parentFragment = (ContentFragment) parent;
			parentActivity = parentFragment.getActivity();
		}
	}

	public void initialize(View myView) {
		this.myView = myView;

		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		submitBtn = (ImageButton) myView.findViewById(R.id.btn_submit);
		pickPhotoBtn = (ImageButton) myView.findViewById(R.id.btn_open_image);
		takePhotoBtn = (ImageButton) myView.findViewById(R.id.btn_take_photo);
		viewLatLon = (TextView) myView.findViewById(R.id.viewLatLon);

		previwImg = ((ImageView) myView.findViewById(R.id.image_preview));

		txtStatusTitle = (EditText) myView.findViewById(id.text_status_title);
		txtStatusBody = (EditText) myView.findViewById(id.text_status_body);

		geoTgl = (ToggleButton) myView.findViewById(id.sendLatLon);

		submitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
				clearFields();
			}
		});

		pickPhotoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent in = new Intent(Intent.ACTION_PICK);
				in.setType("image/*");
				parentActivity.startActivityForResult(in, HomeActivity.RQ_SELECT_PHOTO);
			}
		});

		takePhotoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent in = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				takePhotoTarget = Max.getTempFile();
				in.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takePhotoTarget));
				parentActivity.startActivityForResult(in, HomeActivity.RQ_TAKE_PHOTO);
			}
		});

		ToggleButton sendLatLon = (ToggleButton) myView.findViewById(R.id.sendLatLon);
		sendLatLon.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (!locationListenerAttached) {
						location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

						viewLatLon.setText(getString(R.string.viewLatLon) + "\n" + "Loading...");
						locationListenerAttached = true;
					}
				} else {
					detachLocationListener();
				}
			}
		});
	}

	protected void clearFields() {
		txtStatusTitle.setText("");
		previwImg.setImageURI(null);
	}

	public void setLocation(GeoPoint location) {
		Toast.makeText(parentActivity, "setLocation(GeoPoint location) " + location, Toast.LENGTH_SHORT).show();

		this.location = new Location("empty");
		this.location.setLatitude(location.getLatitudeE6() / 1e6);
		this.location.setLongitude(location.getLongitudeE6() / 1e6);
		displayLocation();
	}

	public void displayLocation() {
		viewLatLon.setText(getString(R.string.viewLatLon) + "\n" + "Lat=" + location.getLatitude() + "  Long=" + location.getLongitude());
	}

	private Context getActivity() {
		return parentActivity;
	}

	protected String getString(Integer stringId) {
		return getActivity().getString(stringId);
	}

	void detachLocationListener() {
		if (locationListenerAttached) {
			locationManager.removeUpdates(locationListener);
			location = null;
			this.locationListenerAttached = false;
		}
	}

	private void showStartNotification() {
		mNotificationManager = (NotificationManager) parentActivity.getSystemService(Context.NOTIFICATION_SERVICE);

		// Instantiate the Notification:
		CharSequence tickerText = "Uploading...";
		Notification notification = new Notification(R.drawable.arrow_up, tickerText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		// Define the Notification's expanded message and Intent:
		Context context = parentActivity;
		PendingIntent nullIntent = PendingIntent.getActivity(context, 0, new Intent(), 0);
		notification.setLatestEventInfo(context, "Upload in progress...", "You are notified here when it completes", nullIntent);

		// Pass the Notification to the NotificationManager:
		mNotificationManager.notify(UPLOAD_PROGRESS_ID, notification);
	}

	void sendMessage() {

		final ProgressDialog pd = ProgressDialog.show(parentActivity, "Posting status...", "Please wait", true, false);

		HashMap<String, String> postData = new HashMap<String, String>();

		postData.put("title", txtStatusTitle.getText().toString());

		if (geoTgl.isChecked() || location != null) {
			if (location == null) {
				Toast.makeText(parentActivity, "Unable to get location info - please try again.", Toast.LENGTH_LONG).show();
				pd.dismiss();
				return;
			}
			Double longitude = location.getLongitude();
			Double latitude = location.getLatitude();

			postData.put("lat", String.valueOf(latitude));
			postData.put("long", String.valueOf(longitude));
		}

		ArrayList<Uri> files = new ArrayList<Uri>();

		if (this.fileToUpload != null) { // an image has been attached
			files.add(this.fileToUpload);
			showStartNotification();
		}

// getFriendicaAbstraction().postPost(txtStatusBody.getText().toString(), postData, files,null);

		getFriendicaAbstraction().postPost(txtStatusBody.getText().toString(), postData, files, new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				ArrayList<JSONObject> cameBack = result.getResult();

				pd.dismiss();
				if (getActivity() instanceof FragmentParentListener) {
					((FragmentParentListener) getActivity()).OnFragmentMessage("Finished", null, null);
				}

				TwAjax uploader = result.getProcessor();
				JSONObject jsonResult = null;
				if (result.getResult().size() > 0) {
					jsonResult = result.getResult().get(0);
					mNotificationManager.cancel(UPLOAD_PROGRESS_ID);
					if (uploader.isSuccess() && uploader.getError() == null) {
						try {
							String postedText = jsonResult.getString("text");
							showSuccessNotification(parentActivity);

						} catch (Exception e) {
							String errMes = e.getMessage() + " | " + uploader.getResult();
							if (jsonResult != null)
								try {
									errMes = jsonResult.getString("error");
								} catch (JSONException je) {
								}

							showFailNotification(parentActivity, errMes);

							e.printStackTrace();
						}
					} else if (uploader.getError() != null) {
						showFailNotification(parentActivity, uploader.getError().toString());
					} else {
						showFailNotification(parentActivity, uploader.getResult());
					}

				}
			}
		});
	}

	public void setImage(Uri fileToUpload) {
		this.fileToUpload = fileToUpload;
		String fileSpec = Max.getRealPathFromURI(parentActivity, this.fileToUpload);
		try {
			ExifInterface exif = new ExifInterface(fileSpec);
			float[] exifLatLon = new float[2];
			if (exif.getLatLong(exifLatLon)) {
				GeoPoint exifLocation = new GeoPoint(exifLatLon[0], exifLatLon[1]);
				setLocation(exifLocation);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		previwImg.setImageBitmap(Tools.loadResizedBitmap(fileSpec, 500, 300, false));
	}

	public static void startImageUpload(Context context, Uri uri) {
		Intent in = new Intent(context, FriendicaImgUploadActivity.class);
		in.putExtra(Intent.EXTRA_STREAM, uri);
		context.startActivity(in);
	}

	public Friendica getFriendicaAbstraction() {
		if (parentActivity instanceof TimelineEventMapActivity) {
			return ((TimelineEventMapActivity) parentActivity).getFriendicaAbstraction();
		} else if (parentFragment instanceof PostListFragment) {
			return ((PostListFragment) parentFragment).getFriendicaAbstraction();
		} else {
			return null;
		}
	}

	private void showFailNotification(Context ctx, String txt) {
		// Instantiate the Notification:
		CharSequence tickerText = "Upload failed, please retry!";
		Notification notification = new Notification(R.drawable.arrow_up, tickerText, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Define the Notification's expanded message and Intent:
		Context context = parentActivity;
		CharSequence contentTitle = "Upload failed, click to retry!";
		CharSequence contentText = txt;
		Intent notificationIntent = new Intent(parentActivity, FriendicaImgUploadActivity.class);
		Bundle b = new Bundle();
		b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);
		/*
		b.putString(Intent.EXTRA_SUBJECT, subject);
		b.putString(EXTRA_DESCTEXT, descText);
		*/

		notificationIntent.putExtras(b);
		PendingIntent contentIntent = PendingIntent.getActivity(parentActivity, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		// Pass the Notification to the NotificationManager:
		mNotificationManager.notify(UPLOAD_FAILED_ID, notification);
	}

	private void showSuccessNotification(Context ctx) {
		// Instantiate the Notification:
		Integer icon = R.drawable.arrow_up;
		CharSequence tickerText = "Upload succeeded!";
		Long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Define the Notification's expanded message and Intent:

		PendingIntent nullIntent = PendingIntent.getActivity(parentActivity, 0, new Intent(), 0);
		notification.setLatestEventInfo(parentActivity, tickerText, "Click to dismiss", nullIntent);

		// Pass the Notification to the NotificationManager:

		mNotificationManager.notify(UPLOAD_SUCCESS_ID, notification);
	}
}