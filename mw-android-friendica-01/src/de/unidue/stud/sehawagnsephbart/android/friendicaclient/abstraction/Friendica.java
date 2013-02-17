package de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.Uri.Builder;
import android.text.Spannable;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import de.wikilab.android.friendica01.Max;
import de.wikilab.android.friendica01.TwAjax;

public class Friendica {
	private static final String TAG = "Friendica/MainAbstraction";

	final static String API_PATH = "/api/";
	final static String API_TYPE_JSON = ".json";
	final Integer ITEMS_PER_PAGE = 20;
	Integer curLoadPage = 1;
	private Context context = null;
	
	
	public Friendica(){
		
	}
	
	public Friendica(Context context){
		this();
		this.setContext(context);
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void testExecuteMethod() {
		String userId = new String();
		HashMap<String, String> queryElements = new HashMap<String, String>();
		queryElements.put("count", String.valueOf(ITEMS_PER_PAGE));
		queryElements.put("page", String.valueOf(curLoadPage));
		if (userId != null && !userId.equals("")) {
			queryElements.put("userId", userId);
		}

		JsonProcessor jsonProcessor = executeAjaxQuery("statuses/user_timeline", queryElements, new JsonFinishReaction<ArrayList<JSONObject>>() {
			@Override
			public void onFinished(ResultObject<ArrayList<JSONObject>> result) {
				ArrayList<JSONObject> jAr = result.getResult();
				for (JSONObject jsonObject : jAr) {
					System.out.println(jsonObject);
				}
			}
		});
		if (jsonProcessor.getIsFinished()) {
			System.out.println("Already finished");
		}
	}

	public JsonProcessor executeAjaxQuery(String command, JsonFinishReaction<ArrayList<JSONObject>> finishReaction) {
		return executeAjaxQuery(command, new HashMap<String, String>(), finishReaction);
	}

	public JsonProcessor executeAjaxQuery(String command, HashMap<String, String> queryElements, JsonFinishReaction<ArrayList<JSONObject>> finishReaction) {
		ResultObject<ArrayList<JSONObject>> result = new ResultObject<ArrayList<JSONObject>>();
		Uri uri = Uri.parse(Max.getServer(getContext()) + API_PATH + command + API_TYPE_JSON);
		Builder uriBuilder = uri.buildUpon();
		for (Entry<String, String> queryElement : queryElements.entrySet()) {
			uriBuilder.appendQueryParameter(queryElement.getKey(), queryElement.getValue());
		}
		final TwAjax t = new TwAjax(getContext(), true, true);
		JsonProcessor jsonProcessor = new JsonProcessor(t, finishReaction, result);
		t.getUrlContent(uriBuilder.build().toString(), jsonProcessor);
		return jsonProcessor;
	}

	public class ResultObject<TargetResultClass extends Object> {
		private TargetResultClass result = null;

		public TargetResultClass getResult() {
			return result;
		}

		public void setResult(TargetResultClass result) {
			this.result = result;
		}
	}

	public interface JsonFinishReaction<TargetResultClass> {
		public void onFinished(ResultObject<TargetResultClass> result);
	}

	public class JsonProcessor implements Runnable {
		private TwAjax t;
		private JsonFinishReaction<ArrayList<JSONObject>> jsonFinishReaction;
		private ResultObject<ArrayList<JSONObject>> result;
		private ArrayList<JSONObject> resultArray = new ArrayList<JSONObject>();
		private Boolean finished = false;
		private Boolean error = false;

		public JsonProcessor(TwAjax t, JsonFinishReaction<ArrayList<JSONObject>> finishReaction, ResultObject<ArrayList<JSONObject>> result) {
			this.t = t;
			this.jsonFinishReaction = finishReaction;
			this.result = result;
		}

		public Boolean getIsFinished() {
			return finished;
		}
		
		public Boolean getIsError() {
			return error;
		}

		@Override
		public void run() {
			Object jsonResult = t.getJsonResult();
			if(jsonResult instanceof JSONArray){
				JSONArray jAr = (JSONArray) jsonResult;
				for (int i = 0; i < jAr.length(); i++) {
					JSONObject jObj;
					try {
						jObj = jAr.getJSONObject(i);
						resultArray.add(jObj);
					} catch (JSONException e) {
						e.printStackTrace();
						
					}
				}
				this.result.setResult(resultArray);
				jsonFinishReaction.onFinished(this.result);
				finished = true;
			}else{
				error=true; //e.g. {"error":"not implemented"}
				
				this.result.setResult(resultArray);
				jsonFinishReaction.onFinished(this.result);
				System.err.println(jsonResult);
				finished = true;
			}
		}
	}

	public static void displayProfileImageFromPost(JSONObject post, final ImageView target, Context context) {
		try {
			final String piurl = post.getJSONObject("user").getString("profile_image_url");
			placeImageFromURI(piurl, target, context, "pi");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public static String getImageURIFromPost(Spannable htmlSpannable){
		return getImagesFromPost(htmlSpannable)[0].getSource();
	}
	
	public static ImageSpan[] getImagesFromPost(Spannable htmlSpannable){
		ImageSpan[] images=htmlSpannable.getSpans(0, htmlSpannable.length(), ImageSpan.class);
		return images;
	}
	
	public static String getScaledImageURI(String imageUri,Integer scaleLevel){
		Uri imageUriObject;
		if(scaleLevel==0 || scaleLevel==1 || scaleLevel==2){ //0=fullsize, 2=small
			imageUriObject=Uri.parse(imageUri);
			String filename=imageUriObject.getLastPathSegment();
			
			String path=imageUriObject.getPath();
			path=path.substring(0,path.length()-filename.length());
			
			String[] filenameParts = filename.split("\\.");
			filenameParts[0]=filenameParts[0].substring(0, filenameParts[0].length()-1);
			filenameParts[0]=filenameParts[0]+scaleLevel;

			return imageUriObject.buildUpon().path(path).appendPath(filenameParts[0]+"."+filenameParts[1]).build().toString(); //Build and return new URI with original path and new filename

		}
		return imageUri;
	}
	
	static public void placeImageFromURI(final String uri, final ImageView target, Context context, String prefix) {
		final TwAjax pidl = new TwAjax(context, true, false);
		pidl.ignoreSSLCerts = true;

		if (uri.startsWith("data:image")) {
			Log.i(TAG, "TRY Extracting embedded Img: " + uri);
			final int imgStart = uri.indexOf("base64,") + 7; // TODO SHOULD CHECK FOR FAILURE TO FIND base64,
			final String encodedImg = uri.substring(imgStart);
			final int imgHash = encodedImg.hashCode();
			final String imgHashString = Integer.toString(imgHash);

			final File pifile = new File(Max.IMG_CACHE_DIR + "/" + prefix + "_" + Max.cleanFilename(imgHashString));
			target.setTag(pifile.getAbsolutePath());
			if (pifile.isFile()) {
				Log.i(TAG, "OK  Load cached embedded Img: " + imgHashString);
				target.setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
				target.setVisibility(View.VISIBLE);
			} else {
				Log.i(TAG, "OK  Decoding embedded Img: " + Integer.toString(imgHash));
				final byte[] imgAsBytes = Base64.decode(encodedImg.getBytes(), Base64.DEFAULT);
				try {
					FileOutputStream pifileOut = new FileOutputStream(pifile.getAbsolutePath());
					pifileOut.write(imgAsBytes);
					pifileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				target.setImageDrawable(new BitmapDrawable(pifile.getAbsolutePath()));
				target.setVisibility(View.VISIBLE);
			}
		} else {
			Log.i(TAG, "TRY Downloading Img: " + uri);
			final File pifile = new File(Max.IMG_CACHE_DIR + "/" + prefix + "_" + Max.cleanFilename(uri));
			target.setTag(pifile.getAbsolutePath());
			if (pifile.isFile()) {
				Log.i(TAG, "OK  Load cached post Img: " + uri);
				BitmapDrawable bmp = new BitmapDrawable(pifile.getAbsolutePath());
				if (bmp.getBitmap() != null && bmp.getBitmap().getWidth() > 30) { // minWidth 30px to remove facebook's ugly icons
					target.setImageDrawable(bmp);
					target.setVisibility(View.VISIBLE);
				}
			} else {
				pidl.urlDownloadToFile(uri, pifile.getAbsolutePath(), new Runnable() {
					@Override
					public void run() {
						Log.i(TAG, "OK  Download Img: " + uri);
						BitmapDrawable bmp = new BitmapDrawable(pifile.getAbsolutePath());
						if (bmp.getBitmap() != null && bmp.getBitmap().getWidth() > 30) { // minWidth 30px to remove facebook's ugly icons
							target.setImageDrawable(bmp);
							target.setVisibility(View.VISIBLE);
						}
					}
				});
			}
		}
	}
}