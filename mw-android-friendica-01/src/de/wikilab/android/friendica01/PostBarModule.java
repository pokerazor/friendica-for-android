package de.wikilab.android.friendica01;

import java.io.File;
import java.io.IOException;

import org.osmdroid.util.GeoPoint;

import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Tools;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon.TimelineEventMapActivity;
import de.wikilab.android.friendica01.R.id;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PostBarModule {
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

	void sendMessage() {

		if (this.fileToUpload != null) { // an image has been attached
			Intent uploadIntent = new Intent(parentActivity, FileUploadService.class);
			Bundle b = new Bundle();

			b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);

			uploadIntent.putExtras(b);

			Log.i("Andfrnd/UploadFile", "before startService");
			parentActivity.startService(uploadIntent);
			Log.i("Andfrnd/UploadFile", "after startService");
		}

		final ProgressDialog pd = ProgressDialog.show(parentActivity, "Posting status...", "Please wait", true, false);

		final TwAjax t = new TwAjax(parentActivity, true, true);
		t.addPostData("title", txtStatusTitle.getText().toString());
		t.addPostData("status", txtStatusBody.getText().toString());

		t.addPostData("source", getString(R.string.app_name));
		if (geoTgl.isChecked() || location != null) {
			if (location == null) {
				Toast.makeText(parentActivity, "Unable to get location info - please try again.", Toast.LENGTH_LONG).show();
				pd.dismiss();
				return;
			}
			Double longitude = location.getLongitude();
			Double latitude = location.getLatitude();

			t.addPostData("lat", String.valueOf(latitude));
			t.addPostData("long", String.valueOf(longitude));
		}
		t.postData(Max.getServer(parentActivity) + "/api/statuses/update.json", new Runnable() {
			@Override
			public void run() {
				pd.dismiss();
				if (getActivity() instanceof FragmentParentListener) {
					((FragmentParentListener) getActivity()).OnFragmentMessage("Finished", null, null);
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
			// TODO Auto-generated catch block
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
}