package de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public class Tools {

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

}
