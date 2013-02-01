package de.wikilab.android.friendica01;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.unidue.stud.sehawagnsephbart.android.friendicaclient.abstraction.Friendica;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PostListAdapter extends ArrayAdapter<JSONObject> {
	private static final String TAG = "Friendica/PostListAdapter";

	public boolean isPostDetails = false;

	interface OnUsernameClickListener {
		void OnUsernameClick(ViewHolder viewHolder);
	}

	public OnUsernameClickListener onUsernameClick;

	public static class ViewHolder {
		public static final Integer POST_TYPE_IMAGE = 1;
		public static final Integer POST_TYPE_COMMENT = 2;
		public static final Integer POST_TYPE_LIKE = 3;
		public static final Integer POST_TYPE_UNKNOWN = 0;

		int Type, position;
		ImageView profileImage;
		TextView userName, htmlContent, dateTime;
		ImageView[] picture = new ImageView[3];
		TextView coordinates;
	}

	public PostListAdapter(Context context, List<JSONObject> objects) {
		super(context, R.layout.pl_listitem, objects);

	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public long getItemId(int position) {
		try {
			return ((JSONObject) getItem(position)).getLong("id");
		} catch (JSONException e) {
			Log.e(TAG, "Item without ID!");
			return 0;
		}
	}

	@Override
	public int getItemViewType(int position) {
		try {
			JSONObject post = (JSONObject) getItem(position);

			if (post.getString("verb").equals("http://activitystrea.ms/schema/1.0/like")) {
				post.put("MW_TYPE", ViewHolder.POST_TYPE_LIKE);
			} else if (post.has("in_reply_to_status_id") && post.getString("in_reply_to_status_id").equals("0") == false) {
				post.put("MW_TYPE", ViewHolder.POST_TYPE_LIKE);
			} else if (post.getString("statusnet_html").contains("<img")) {
				post.put("MW_TYPE", ViewHolder.POST_TYPE_IMAGE);
			} else {
				post.put("MW_TYPE", ViewHolder.POST_TYPE_UNKNOWN);
			}

			return post.getInt("MW_TYPE");
		} catch (JSONException e) {
			return 0;
		}
	}

	@Override
	public int getViewTypeCount() {
		return 4;
	}

	private void navigateUserProfile(int position) {
		try {
			Intent inte = new Intent(getContext(), UserProfileActivity.class);
			inte.putExtra("userId", String.valueOf(((JSONObject) getItem(position)).getJSONObject("user").getInt("id")));
			getContext().startActivity(inte);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private OnClickListener postPictureOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent inte = new Intent(Intent.ACTION_VIEW);
			inte.setDataAndType(Uri.parse("file://" + v.getTag()), "image/jpeg");
			getContext().startActivity(inte);
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder H;
		if (convertView == null) {
			LayoutInflater inf = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			H = new ViewHolder();
			H.Type = getItemViewType(position);

			if (H.Type == ViewHolder.POST_TYPE_IMAGE) {
				convertView = inf.inflate(R.layout.pl_listitem_picture, null);
				H.userName = (TextView) convertView.findViewById(R.id.userName);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
				H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);

				H.picture[0] = (ImageView) convertView.findViewById(R.id.picture1);
				H.picture[0].setOnClickListener(postPictureOnClickListener);
				H.picture[1] = (ImageView) convertView.findViewById(R.id.picture2);
				H.picture[1].setOnClickListener(postPictureOnClickListener);
				H.picture[2] = (ImageView) convertView.findViewById(R.id.picture3);
				H.picture[2].setOnClickListener(postPictureOnClickListener);

			} else if (H.Type == ViewHolder.POST_TYPE_COMMENT) {
				convertView = inf.inflate(R.layout.pl_listitem_comment, null);
				H.userName = (TextView) convertView.findViewById(R.id.userName);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
				H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);

			} else if (H.Type == ViewHolder.POST_TYPE_LIKE) {
				convertView = inf.inflate(R.layout.pl_listitem_like, null);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);

			} else {
				convertView = inf.inflate(R.layout.pl_listitem, null);
				H.userName = (TextView) convertView.findViewById(R.id.userName);
				H.htmlContent = (TextView) convertView.findViewById(R.id.htmlContent);
				H.profileImage = (ImageView) convertView.findViewById(R.id.profileImage);
			}

			if (isPostDetails && H.Type != ViewHolder.POST_TYPE_LIKE) {
				if (H.Type <= ViewHolder.POST_TYPE_IMAGE) {
					H.userName.setTextSize(18);
					H.htmlContent.setTextSize(18);
				}

				View savedView = convertView;
				convertView = inf.inflate(R.layout.pd_listitemwrapper, null);
				((LinearLayout) convertView).addView(savedView, 0);
				H.htmlContent.setFocusable(true);
				H.htmlContent.setMovementMethod(LinkMovementMethod.getInstance());

				H.dateTime = (TextView) convertView.findViewById(R.id.date);
				H.coordinates = (TextView) convertView.findViewById(R.id.coordinates);

			}

			convertView.setTag(H);

			if (H.userName != null) {
				OnClickListener clk = new OnClickListener() {
					@Override
					public void onClick(View v) {
						navigateUserProfile(H.position);
					}
				};
				H.userName.setOnClickListener(clk);
				H.profileImage.setOnClickListener(clk);
			}

		} else {
			H = (ViewHolder) convertView.getTag();
		}

		JSONObject post = (JSONObject) getItem(position);
		H.position = position;

		if (H.profileImage != null) {
			H.profileImage.setImageResource(R.drawable.ic_launcher);

			Friendica.getProfileImageFromPost(post, H.profileImage, getContext());

		}

		if (H.userName != null) {
			try {
				String appendix = "";
				if (H.Type == ViewHolder.POST_TYPE_COMMENT && !isPostDetails)
					appendix = " replied to " + post.getString("in_reply_to_screen_name") + ":";
				H.userName.setText(post.getJSONObject("user").getString("name") + appendix);
			} catch (Exception e) {
				H.userName.setText("Invalid Dataset!");
			}
		}

		if (H.dateTime != null) {
			try {
				H.dateTime.setText(DateUtils.getRelativeDateTimeString(parent.getContext(), java.util.Date.parse(post.getString("published")), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_24HOUR));
			} catch (Exception e) {
				H.dateTime.setText("Invalid Dataset!");
			}
		}

		if (H.coordinates != null) {
			try {
				H.coordinates.setText(post.getString("coordinates"));
			} catch (Exception e) {
				H.coordinates.setText("Invalid Dataset!");
			}
		}

		try {

			// Max.setHtmlWithImages(event.htmlContent, post.getString("statusnet_html"));
			String filtered_html = post.getString("statusnet_html");
			filtered_html = filtered_html.replaceAll("(<br[^>]*>|</?div[^>]*>|</?p>)", "  ");
			// filtered_html = filtered_html.replaceAll("<img[^>]+src=[\"']([^>\"']+)[\"'][^>]*>", "<a href='$1'>Bild: $1</a>");
			Spanned spanned = Html.fromHtml(filtered_html);
			Spannable htmlSpannable;
			if (spanned instanceof SpannableStringBuilder) {
				htmlSpannable = (SpannableStringBuilder) spanned;
			} else {
				htmlSpannable = new SpannableStringBuilder(spanned);
			}
			if (H.Type == ViewHolder.POST_TYPE_IMAGE) {
				downloadPics(H, htmlSpannable);
			}

			H.htmlContent.setText(htmlSpannable);

		} catch (Exception e) {
			H.htmlContent.setText("Invalid Dataset!");
		}

		return convertView;
	}



	private void downloadPics(final ViewHolder H, Spannable htmlSpannable) {
		int pos = 0;
		for (int i = 0; i <= 2; i++) {
			H.picture[i].setVisibility(View.GONE);
		}
		ImageSpan[] images=Friendica.getImagesFromPost(htmlSpannable);

		for (ImageSpan img : images) {
			htmlSpannable.removeSpan(img);

			if (pos > 2) {
				break;
			}

			Friendica.placeImageFromURI(img.getSource(), H.picture[pos],getContext(),"pi");

			pos++;
		}
	}
}