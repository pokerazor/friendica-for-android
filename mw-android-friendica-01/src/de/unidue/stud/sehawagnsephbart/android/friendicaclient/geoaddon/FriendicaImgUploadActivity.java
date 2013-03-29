package de.unidue.stud.sehawagnsephbart.android.friendicaclient.geoaddon;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Tools;
import de.wikilab.android.friendica01.FileUploadService;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.R;

public class FriendicaImgUploadActivity extends Activity {
	private Uri fileToUpload = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		View btn_upload = (View) findViewById(R.id.btn_upload);
		btn_upload.setEnabled(false);

		Intent callingIntent = getIntent();

		if (callingIntent != null) {
			if (callingIntent.hasExtra(Intent.EXTRA_STREAM)) {
				fileToUpload = (Uri) callingIntent.getParcelableExtra(Intent.EXTRA_STREAM);
				String fileSpec = Max.getRealPathFromURI(FriendicaImgUploadActivity.this, fileToUpload);

				ImageView gallerypic = ((ImageView) findViewById(R.id.preview));

				gallerypic.setImageBitmap(Tools.loadResizedBitmap(fileSpec, 500, 300, false));

				btn_upload.setEnabled(true);
			}
		}

		btn_upload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {

				Intent uploadIntent = new Intent(getApplicationContext(), FileUploadService.class);
				Bundle b = new Bundle();
				
				b.putParcelable(Intent.EXTRA_STREAM, fileToUpload);

				uploadIntent.putExtras(b);

				Log.i("Andfrnd/UploadFile", "before startService");
				startService(uploadIntent);
				Log.i("Andfrnd/UploadFile", "after startService");

				finish();
			}
		});

	}
}