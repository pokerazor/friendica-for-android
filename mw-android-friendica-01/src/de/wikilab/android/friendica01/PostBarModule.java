package de.wikilab.android.friendica01;

import org.osmdroid.util.GeoPoint;

import de.wikilab.android.friendica01.R.id;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PostBarModule {
	private Object parent = null;
	private Activity parentActivity = null;
	private ContentFragment parentFragment = null;

	private ImageButton submitBtn = null;
	private Boolean locationListenerAttached = false;
	private TextView viewLatLon = null;
	private View myView = null;

	LocationManager locationManager = null;
	public Location location = null;

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
		submitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMessage();
			}
		});

		viewLatLon = (TextView) myView.findViewById(R.id.viewLatLon);

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
		EditText txt_status = (EditText) myView.findViewById(id.text_input);
		ToggleButton geo_en = (ToggleButton) myView.findViewById(id.sendLatLon);

		final ProgressDialog pd = ProgressDialog.show(parentActivity, "Posting status...", "Please wait", true, false);

		final TwAjax t = new TwAjax(parentActivity, true, true);
		t.addPostData("status", txt_status.getText().toString());
		t.addPostData("source", "<a href='http://andfrnd.wikilab.de'>Friendica for Android</a>");
		if (geo_en.isChecked() || location != null) {
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

}