package de.wikilab.android.friendica01;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class FriendicaImgUploadActivity extends Activity implements LoginListener {
	public final static Integer RQ_SELECT_CLIPBOARD = 1;

	Boolean deleteAfterUpload = false;
	Boolean uploadTextMode = false;
	private Boolean locationListenerAttached = false;
	private LocationManager lm = null;
	private Location location = null;

	String uploadCbName = "";
	String textToUpload = "";
	String fileExt = "";
	Uri fileToUpload = null;

	Integer uploadCbId = 0;

	TextView viewLatLon = null;
	ToggleButton sendLatLon = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.uploadfile);

		sendLatLon = (ToggleButton) findViewById(R.id.sendLatLonPhoto);
		sendLatLon.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				{

					if (!locationListenerAttached) {
						getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

						viewLatLon.setText(getString(R.string.viewLatLon) + "\n" + "Loading...");
						locationListenerAttached = true;
					} else {
						detachLocationListener();
					}

				}
			}
		});
		viewLatLon = (TextView) findViewById(R.id.viewLatLonPhoto);
		View btn_upload = (View) findViewById(R.id.btn_upload);
		btn_upload.setEnabled(false);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = prefs.getString("login_user", null);
		if (userName == null || userName.length() < 1) {
			Max.showLoginForm(this, null);
		} else {
			Max.tryLogin(this);
		}

		EditText tSubject = (EditText) findViewById(R.id.subject);
		EditText t = (EditText) findViewById(R.id.maintb);
		t.setText("File Uploader\n\nERR: Intent did not contain file!\n\nPress menu button for debug info !!!\n\n");

		Intent callingIntent = getIntent();

		if (callingIntent != null) {
			if (callingIntent.hasExtra(Intent.EXTRA_STREAM)) {
				fileToUpload = (Uri) callingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
				String fileSpec = Max.getRealPathFromURI(FriendicaImgUploadActivity.this, fileToUpload);

				ImageView gallerypic = ((ImageView) findViewById(R.id.preview));

				gallerypic.setImageBitmap(loadResizedBitmap(fileSpec, 500, 300, false));

				t.setText("Andfrnd Uploader Beta\n\n[b]URI:[/b] " + fileToUpload.toString() + "\n[b]File name:[/b] " + fileSpec);

				deleteAfterUpload = false;

				// restore data after failed upload:
				if (callingIntent.hasExtra(FileUploadService.EXTRA_DESCTEXT)) {
					t.setText(callingIntent.getStringExtra(FileUploadService.EXTRA_DESCTEXT));
				}

				if (callingIntent.hasExtra(Intent.EXTRA_SUBJECT)) {
					tSubject.setText(callingIntent.getStringExtra(Intent.EXTRA_SUBJECT));
				}

				uploadTextMode = false;
				btn_upload.setEnabled(true);

			}
		}

		btn_upload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				EditText txtSubject = (EditText) findViewById(R.id.subject);
				EditText txtDesc = (EditText) findViewById(R.id.maintb);

				Intent uploadIntent = new Intent(getApplicationContext(), FileUploadService.class);
				Bundle b = new Bundle();

				b.putString(FileUploadService.EXTRA_DESCTEXT, txtDesc.getText().toString());
				b.putString(Intent.EXTRA_SUBJECT, txtSubject.getText().toString());
				

				if (location != null) {
					b.putString(FileUploadService.EXTRA_LOCLAT, String.valueOf(location.getLatitude()));
					b.putString(FileUploadService.EXTRA_LOCLAN, String.valueOf(location.getLongitude()));
				}
				b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);
				System.out.println(b);

				uploadIntent.putExtras(b);

				Log.i("Andfrnd/UploadFile", "before startService");
				startService(uploadIntent);
				Log.i("Andfrnd/UploadFile", "after startService");

				finish();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.uploadfile_menu, menu);
		return true;
	}

	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			FriendicaImgUploadActivity.this.location = location;
			Double longitude = location.getLongitude();
			Double latitude = location.getLatitude();
			viewLatLon.setText(getString(R.string.viewLatLon) + "\n" + "Lat=" + String.valueOf(latitude) + "  Long=" + String.valueOf(longitude));
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

	@Override
	public void onLogin() {

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.view_debug:
			Intent callingIntent = getIntent();
			if (callingIntent != null) {
				Bundle e = callingIntent.getExtras();
				String[] val = new String[e.keySet().size()];
				String[] val2 = new String[e.keySet().size()];
				int i = 0;
				for (String key : e.keySet()) {
					val[i] = key + ": " + String.valueOf(e.get(key));
					val2[i++] = getTypeName(e.get(key)) + " " + key + ":\n" + String.valueOf(e.get(key));
				}
				final String[] values = val2;

				new AlertDialog.Builder(FriendicaImgUploadActivity.this).setItems(val, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new AlertDialog.Builder(FriendicaImgUploadActivity.this).setMessage(values[which]).show();
					}
				}).setTitle("Debug Info [File]").show();

			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public static Bitmap loadResizedBitmap(String filename, int width, int height, boolean exact) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filename, options);
		if (options.outHeight > 0 && options.outWidth > 0) {
			options.inJustDecodeBounds = false;
			options.inSampleSize = 2;
			while (options.outWidth / options.inSampleSize > width && options.outHeight / options.inSampleSize > height) {
				options.inSampleSize++;
			}
			options.inSampleSize--;

			bitmap = BitmapFactory.decodeFile(filename, options);
			if (bitmap != null && exact) {
				bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
			}
		}
		return bitmap;
	}

	void detachLocationListener() {
		if (locationListenerAttached) {
			getLocationManager().removeUpdates(locationListener);
			this.locationListenerAttached = false;
		}
	}

	LocationManager getLocationManager() {
		if (lm == null) {
			lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		}
		return lm;
	}

	private String getTypeName(Object o) {
		if (o == null)
			return "<null>";
		Class<? extends Object> type = o.getClass();
		if (type == null)
			return "<unknown>";
		else
			return type.getCanonicalName();
	}
}